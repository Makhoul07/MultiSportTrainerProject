package com.example.multisporttrainer.mqtt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Builds the JSON payloads the IoT simulator expects.
 *
 * Contract comes from IoT/main_controller.py / training_simulator.py:
 *  - mode "custom_route"    -> requires "rounds"
 *  - mode "generated_route" -> requires "difficulty" (easy/medium/hard)
 */
public final class MqttMessages {

    public static final String MODE_CUSTOM = "custom_route";
    public static final String MODE_GENERATED = "generated_route";

    private MqttMessages() {
        // factory only
    }

    /**
     * Maps the app's route type to the simulator's mode string.
     * The app uses "Custom"/"Generated"; the simulator uses the snake_case modes.
     */
    public static String modeFromRouteType(String routeType) {
        if (routeType != null && routeType.toLowerCase().startsWith("gen")) {
            return MODE_GENERATED;
        }
        return MODE_CUSTOM;
    }

    /**
     * start_training command published to {@link MqttConfig#TOPIC_COMMAND}.
     *
     * The IoT simulator (training_simulator.py) reads {@code player}, {@code mode},
     * {@code rounds}, and {@code difficulty}. The {@code session_id}, {@code user_id},
     * and {@code route} fields are carried for the future MQTT->backend bridge and are
     * ignored by the current simulator. {@code player} is required by the simulator.
     *
     * @param sessionId  backend TrainingSession id (CurrentTrainingData.sessionId)
     * @param userId     backend user id (SessionManager.loggedInUserId)
     * @param player     display name; required by the simulator (never empty)
     * @param mode       MODE_CUSTOM or MODE_GENERATED
     * @param difficulty easy/medium/hard
     * @param rounds     number of rounds
     * @param route      cone sequence (CurrentTrainingData.coneSequence)
     */
    public static JSONObject startTraining(int sessionId,
                                           int userId,
                                           String player,
                                           String mode,
                                           String difficulty,
                                           int rounds,
                                           List<Integer> route) throws JSONException {
        JSONObject cmd = new JSONObject();
        cmd.put("command", "start_training");
        cmd.put("session_id", sessionId);
        cmd.put("user_id", userId);
        cmd.put("player", (player == null || player.trim().isEmpty()) ? "Guest" : player);
        cmd.put("mode", mode);
        cmd.put("difficulty", difficulty == null ? "medium" : difficulty.toLowerCase());
        cmd.put("rounds", rounds);

        JSONArray routeArray = new JSONArray();
        if (route != null) {
            for (Integer cone : route) {
                routeArray.put((int) cone);
            }
        }
        cmd.put("route", routeArray);

        return cmd;
    }
}
