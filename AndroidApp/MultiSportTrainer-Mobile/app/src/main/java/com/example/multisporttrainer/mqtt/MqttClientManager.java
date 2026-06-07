package com.example.multisporttrainer.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Singleton wrapper around the plain Paho {@link MqttClient} (mqttv3).
 *
 * Connects to the IoT broker, subscribes to the training topics, and dispatches
 * parsed messages to a single {@link MqttTrainingListener} (typically the active
 * training screen). Mirrors the singleton style of {@code api.RetrofitClient}.
 *
 * <p>The plain {@code MqttClient} is synchronous: {@code connect/subscribe/publish/
 * disconnect} block. They are routed through a single-thread executor so callers
 * (e.g. a fragment's {@code onViewCreated}) never block the UI thread. This avoids
 * the old {@code paho.android.service} {@code MqttAndroidClient}, which pulled in
 * the legacy {@code android.support} {@code LocalBroadcastManager} and crashed on
 * AndroidX.
 *
 * <p>Callbacks ({@code messageArrived}, {@code connectionLost}) arrive on Paho's
 * own thread; listeners are expected to marshal to the UI thread themselves.
 *
 * Usage:
 * <pre>
 *   MqttClientManager mqtt = MqttClientManager.getInstance(requireContext());
 *   mqtt.setListener(this);   // implement MqttTrainingListener
 *   mqtt.connect();
 *   mqtt.sendStartCommand(MqttMessages.startTraining(...));
 *   // onDestroyView -> mqtt.clearListener();  (connection stays alive for reuse)
 * </pre>
 */
public class MqttClientManager {

    private static final String TAG = "MqttClientManager";

    private static MqttClientManager instance;

    private final MqttClient client;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private MqttTrainingListener listener;

    private MqttClientManager() {
        String clientId = "mst-android-" + System.currentTimeMillis();
        try {
            // MemoryPersistence: no filesystem persistence dir needed on Android.
            client = new MqttClient(MqttConfig.BROKER_URI, clientId, new MemoryPersistence());
            client.setCallback(callback);
        } catch (MqttException e) {
            // BROKER_URI is a constant, valid URI; failure here is unrecoverable.
            throw new IllegalStateException("Failed to create MQTT client", e);
        }
    }

    /**
     * Process-wide singleton. The {@code context} is accepted for call-site
     * compatibility (and future use) but the plain client doesn't require it.
     */
    public static synchronized MqttClientManager getInstance(Context context) {
        if (instance == null) {
            instance = new MqttClientManager();
        }
        return instance;
    }

    public void setListener(MqttTrainingListener listener) {
        this.listener = listener;
    }

    /** Detach the current listener (e.g. in a fragment's onDestroyView). */
    public void clearListener() {
        this.listener = null;
    }

    public boolean isConnected() {
        try {
            return client.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // CONNECT / DISCONNECT  (blocking calls -> run on io thread)
    // =========================================================

    public void connect() {
        io.execute(() -> {
            if (isConnected()) {
                subscribeToTrainingTopics();
                notifyConnected();
                return;
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30);

            try {
                client.connect(options);
                Log.d(TAG, "Connected to " + MqttConfig.BROKER_URI);
                subscribeToTrainingTopics();
                notifyConnected();
            } catch (MqttException e) {
                Log.e(TAG, "Connect failed", e);
                notifyError("Connection failed", e);
            }
        });
    }

    public void disconnect() {
        io.execute(() -> {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                    Log.d(TAG, "Disconnected");
                }
            } catch (MqttException e) {
                Log.e(TAG, "Disconnect failed", e);
            }
        });
    }

    // =========================================================
    // SUBSCRIBE / PUBLISH
    // =========================================================

    private void subscribeToTrainingTopics() {
        subscribeBlocking(MqttConfig.TOPIC_STATUS);
        subscribeBlocking(MqttConfig.TOPIC_EVENT);
        subscribeBlocking(MqttConfig.TOPIC_RESULT);
    }

    /** Blocking subscribe; only call from the io thread or a Paho callback thread. */
    private void subscribeBlocking(String topic) {
        try {
            client.subscribe(topic, MqttConfig.QOS);
            Log.d(TAG, "Subscribed to " + topic);
        } catch (MqttException e) {
            Log.e(TAG, "Subscribe failed: " + topic, e);
            notifyError("Subscribe failed: " + topic, e);
        }
    }

    public void subscribe(String topic) {
        io.execute(() -> subscribeBlocking(topic));
    }

    public void publish(String topic, JSONObject payload) {
        io.execute(() -> {
            try {
                MqttMessage message = new MqttMessage(payload.toString().getBytes(StandardCharsets.UTF_8));
                message.setQos(MqttConfig.QOS);
                client.publish(topic, message);
                Log.d(TAG, "Published to " + topic + ": " + payload);
            } catch (MqttException e) {
                Log.e(TAG, "Publish failed: " + topic, e);
                notifyError("Publish failed: " + topic, e);
            }
        });
    }

    /** Convenience: publish a start_training command (see {@link MqttMessages}). */
    public void sendStartCommand(JSONObject command) {
        publish(MqttConfig.TOPIC_COMMAND, command);
    }

    // =========================================================
    // CALLBACK / DISPATCH
    // =========================================================

    private final MqttCallbackExtended callback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.d(TAG, "connectComplete reconnect=" + reconnect);
            // Re-subscribe after an automatic reconnect (clean session drops subs).
            if (reconnect) {
                io.execute(() -> {
                    subscribeToTrainingTopics();
                    notifyConnected();
                });
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.w(TAG, "Connection lost", cause);
            if (listener != null) {
                listener.onConnectionLost(cause);
            }
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            String body = new String(message.getPayload(), StandardCharsets.UTF_8);
            Log.d(TAG, "messageArrived [" + topic + "]: " + body);
            dispatch(topic, body);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // not needed for QoS handling here
        }
    };

    private void dispatch(String topic, String body) {
        if (listener == null) {
            return;
        }

        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            Log.e(TAG, "Malformed JSON on " + topic + ": " + body, e);
            listener.onError("Malformed message on " + topic, e);
            return;
        }

        if (MqttConfig.TOPIC_STATUS.equals(topic)) {
            listener.onTrainingStarted(json);

        } else if (MqttConfig.TOPIC_EVENT.equals(topic)) {
            String type = json.optString("type", "");
            if ("cone_activation".equals(type)) {
                listener.onConeActivation(json);
            } else if ("cone_result".equals(type)) {
                listener.onConeResult(json);
            }

        } else if (MqttConfig.TOPIC_RESULT.equals(topic)) {
            listener.onTrainingFinished(json);
        }
    }

    private void notifyConnected() {
        if (listener != null) {
            listener.onConnected();
        }
    }

    private void notifyError(String message, Throwable cause) {
        if (listener != null) {
            listener.onError(message, cause);
        }
    }
}
