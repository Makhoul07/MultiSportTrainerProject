package com.example.multisporttrainer.mqtt;

import org.json.JSONObject;

/**
 * Callback surface for training-related MQTT traffic.
 *
 * All methods have empty defaults so a screen (e.g. LiveTrainingFragment) only
 * overrides the events it cares about. Callbacks are delivered on the Paho
 * service thread — marshal to the UI thread (e.g. requireActivity().runOnUiThread)
 * before touching views.
 */
public interface MqttTrainingListener {

    /** Connected to the broker and subscribed to the training topics. */
    default void onConnected() { }

    /** Broker connection dropped (auto-reconnect is enabled and will retry). */
    default void onConnectionLost(Throwable cause) { }

    /** status topic: {"status":"training_started","route":[...],"difficulty":..,"rounds":..}. */
    default void onTrainingStarted(JSONObject payload) { }

    /** event topic, type=cone_activation: {"target_cone":N,"green_light":N,"red_light":N|null}. */
    default void onConeActivation(JSONObject payload) { }

    /** event topic, type=cone_result: {"cone":N,"result":"correct"|"wrong"}. */
    default void onConeResult(JSONObject payload) { }

    /** result topic: {"score":..,"accuracy":..,"mistakes":..,"duration_seconds":..,"route":[...]}. */
    default void onTrainingFinished(JSONObject payload) { }

    /** Connect / subscribe / publish / parse failure. */
    default void onError(String message, Throwable cause) { }
}
