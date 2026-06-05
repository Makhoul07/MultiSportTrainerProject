package com.example.multisporttrainer.mqtt;

/**
 * MQTT connection + topic constants.
 *
 * Mirrors the IoT side (IoT/config.py). The broker and topic names MUST stay in
 * sync with the Raspberry Pi controllers, otherwise the app and cones won't see
 * each other's messages.
 */
public final class MqttConfig {

    private MqttConfig() {
        // constants only
    }

    // Public test broker used by the IoT rig (plaintext, port 1883).
    // Requires android:usesCleartextTraffic="true" (already set in the manifest).
    public static final String BROKER_URI = "tcp://broker.hivemq.com:1883";

    // App  -> simulator : start_training command
    public static final String TOPIC_COMMAND = "multisport/trainer/command";
    // Simulator -> all  : session lifecycle (training_started, ...)
    public static final String TOPIC_STATUS = "multisport/trainer/status";
    // Simulator <-> hardware : cone_activation (green/red) + cone_result (correct/wrong)
    public static final String TOPIC_EVENT = "multisport/trainer/event";
    // Simulator -> all  : final result (score / accuracy / mistakes / duration)
    public static final String TOPIC_RESULT = "multisport/trainer/result";
    // Reserved for future setup messages
    public static final String TOPIC_SETUP = "multisport/trainer/setup";

    // QoS 1 = at-least-once. Good balance for short, important control messages.
    public static final int QOS = 1;
}
