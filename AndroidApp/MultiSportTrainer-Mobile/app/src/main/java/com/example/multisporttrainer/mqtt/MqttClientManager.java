package com.example.multisporttrainer.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Singleton wrapper around the Paho {@link MqttAndroidClient}.
 *
 * Connects to the IoT broker, subscribes to the training topics, and dispatches
 * parsed messages to a single {@link MqttTrainingListener} (typically the active
 * training screen). Mirrors the singleton style of {@code api.RetrofitClient}.
 *
 * Usage:
 * <pre>
 *   MqttClientManager mqtt = MqttClientManager.getInstance(requireContext());
 *   mqtt.setListener(this);   // implement MqttTrainingListener
 *   mqtt.connect();
 *   mqtt.sendStartCommand(MqttMessages.startTraining(player, mode, diff, rounds));
 *   // onDestroyView -> mqtt.clearListener();  (keep connection alive across screens if desired)
 * </pre>
 */
public class MqttClientManager {

    private static final String TAG = "MqttClientManager";

    private static MqttClientManager instance;

    private final MqttAndroidClient client;
    private MqttTrainingListener listener;

    private MqttClientManager(Context context) {
        String clientId = "mst-android-" + System.currentTimeMillis();
        client = new MqttAndroidClient(
                context.getApplicationContext(),
                MqttConfig.BROKER_URI,
                clientId
        );
        client.setCallback(callback);
    }

    /** Process-wide singleton; always pass an application context internally. */
    public static synchronized MqttClientManager getInstance(Context context) {
        if (instance == null) {
            instance = new MqttClientManager(context);
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
    // CONNECT / DISCONNECT
    // =========================================================

    public void connect() {
        if (isConnected()) {
            notifyConnected();
            return;
        }

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);

        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Connected to " + MqttConfig.BROKER_URI);
                    subscribeToTrainingTopics();
                    notifyConnected();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Connect failed", exception);
                    notifyError("Connection failed", exception);
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, "Connect threw", e);
            notifyError("Connection error", e);
        }
    }

    public void disconnect() {
        try {
            if (client.isConnected()) {
                client.disconnect();
                Log.d(TAG, "Disconnected");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Disconnect failed", e);
        }
    }

    // =========================================================
    // SUBSCRIBE / PUBLISH
    // =========================================================

    private void subscribeToTrainingTopics() {
        subscribe(MqttConfig.TOPIC_STATUS);
        subscribe(MqttConfig.TOPIC_EVENT);
        subscribe(MqttConfig.TOPIC_RESULT);
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic, MqttConfig.QOS);
            Log.d(TAG, "Subscribed to " + topic);
        } catch (MqttException e) {
            Log.e(TAG, "Subscribe failed: " + topic, e);
            notifyError("Subscribe failed: " + topic, e);
        }
    }

    public void publish(String topic, JSONObject payload) {
        try {
            MqttMessage message = new MqttMessage(payload.toString().getBytes(StandardCharsets.UTF_8));
            message.setQos(MqttConfig.QOS);
            client.publish(topic, message);
            Log.d(TAG, "Published to " + topic + ": " + payload);
        } catch (MqttException e) {
            Log.e(TAG, "Publish failed: " + topic, e);
            notifyError("Publish failed: " + topic, e);
        }
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
                subscribeToTrainingTopics();
                notifyConnected();
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
