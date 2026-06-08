package com.example.multisporttrainer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multisporttrainer.mqtt.MqttClientManager;
import com.example.multisporttrainer.mqtt.MqttMessages;
import com.example.multisporttrainer.mqtt.MqttTrainingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Live training screen driven entirely by MQTT events from the IoT cone rig.
 *
 * Flow: connect -> publish start_training -> render cone activations / results in
 * real time -> on the final result, copy the real metrics into CurrentTrainingData
 * and navigate to ResultsFragment. There is no manual "Finish" button anymore.
 */
public class LiveTrainingFragment extends Fragment implements MqttTrainingListener {

    // Live-score estimate constants (mirror IoT/config.py). The authoritative score
    // arrives in onTrainingFinished and overwrites this before we navigate away.
    private static final int CORRECT_SCORE = 10;
    private static final int WRONG_SCORE_PENALTY = 3;

    private TextView txtStatus;
    private TextView txtConeInstruction;
    private TextView txtAvoidCone;
    private TextView txtTimer;
    private TextView txtScore;

    private MqttClientManager mqtt;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private int elapsedSeconds = 0;

    private int liveScore = 0;

    // Guards: avoid re-publishing start on auto-reconnect, and ignore duplicate starts.
    private boolean startCommandSent = false;
    private boolean trainingStarted = false;

    public LiveTrainingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_live_training, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtStatus = view.findViewById(R.id.txt_status);
        txtConeInstruction = view.findViewById(R.id.txt_cone_instruction);
        txtAvoidCone = view.findViewById(R.id.txt_avoid_cone);
        txtTimer = view.findViewById(R.id.txt_timer);
        txtScore = view.findViewById(R.id.txt_score);

        txtStatus.setText("Connecting...");
        txtScore.setText("0");
        txtTimer.setText("00:00");

        mqtt = MqttClientManager.getInstance(requireContext());
        mqtt.setListener(this);
        mqtt.connect();
    }

    // =========================================================
    // MQTT CALLBACKS (delivered off the UI thread -> marshal)
    // =========================================================

    @Override
    public void onConnected() {
        runOnUi(() -> {
            // No backend session => nothing valid to start. Bail out safely.
            if (CurrentTrainingData.sessionId == -1) {
                String message = "No active session. Please start a training session first.";
                txtStatus.setText(message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                mqtt.disconnect();
                navigateToTraining();
                return;
            }

            if (startCommandSent) {
                return; // reconnect after we already started; don't restart the session
            }
            startCommandSent = true;

            txtStatus.setText("Connected. Starting...");

            String mode = MqttMessages.modeFromRouteType(CurrentTrainingData.routeType);
            String player = SessionManager.loggedInFullName;

            try {
                JSONObject command = MqttMessages.startTraining(
                        CurrentTrainingData.sessionId,
                        SessionManager.loggedInUserId,
                        SessionManager.loggedInEmail,
                        player,
                        mode,
                        CurrentTrainingData.difficulty == null
                                ? "medium"
                                : CurrentTrainingData.difficulty.toLowerCase(),
                        CurrentTrainingData.rounds,
                        CurrentTrainingData.coneSequence
                );
                mqtt.sendStartCommand(command);
            } catch (JSONException e) {
                txtStatus.setText("Failed to build start command");
            }
        });
    }

    @Override
    public void onConnectionLost(Throwable cause) {
        runOnUi(() -> txtStatus.setText("Connection lost. Reconnecting..."));
    }

    @Override
    public void onTrainingStarted(JSONObject payload) {
        runOnUi(() -> {
            trainingStarted = true;
            txtStatus.setText("Training Started! Get ready...");
            txtStatus.setTextColor(getColorCompat(R.color.text_dark));
            startTimer();
        });
    }

    @Override
    public void onConeActivation(JSONObject payload) {
        final int targetCone = payload.optInt("target_cone", payload.optInt("green_light", -1));
        final boolean hasRed = payload.has("red_light") && !payload.isNull("red_light");
        final int redCone = hasRed ? payload.optInt("red_light", -1) : -1;

        runOnUi(() -> {
            if (targetCone > 0) {
                txtConeInstruction.setText("Hit Cone " + targetCone + "!");
            }
            if (hasRed && redCone > 0) {
                txtAvoidCone.setText("Avoid Cone " + redCone + "!");
                txtAvoidCone.setVisibility(View.VISIBLE);
            } else {
                txtAvoidCone.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onConeResult(JSONObject payload) {
        final String result = payload.optString("result", "");
        // The hardware's cone_result carries no score, so estimate locally.
        final boolean correct = "correct".equals(result);

        runOnUi(() -> {
            if (correct) {
                liveScore += CORRECT_SCORE;
                txtStatus.setText("✓ Correct!");
                txtStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else {
                liveScore -= WRONG_SCORE_PENALTY;
                txtStatus.setText("✗ Wrong!");
                txtStatus.setTextColor(Color.parseColor("#D62828"));
            }
            txtScore.setText(String.valueOf(liveScore));
        });
    }

    @Override
    public void onTrainingFinished(JSONObject payload) {
        // Parse off the UI thread; the authoritative values come from the IoT result.
        final int score = payload.optInt("score", liveScore);
        final double accuracy = payload.optDouble("accuracy", 0);
        final int mistakes = payload.optInt("mistakes", 0);
        final int durationSeconds = (int) payload.optDouble("duration_seconds", elapsedSeconds);

        runOnUi(() -> {
            stopTimer();

            CurrentTrainingData.score = score;
            CurrentTrainingData.accuracy = accuracy;
            CurrentTrainingData.mistakes = mistakes;
            CurrentTrainingData.durationSeconds = durationSeconds;

            // Easy = no distractions; Medium/Hard = distractions on.
            String difficulty = CurrentTrainingData.difficulty == null
                    ? ""
                    : CurrentTrainingData.difficulty.toLowerCase();
            CurrentTrainingData.distractionsEnabled = !difficulty.equals("easy");

            txtStatus.setText("Training Complete!");
            txtStatus.setTextColor(getColorCompat(R.color.text_dark));

            navigateToResults();
        });
    }

    @Override
    public void onError(String message, Throwable cause) {
        runOnUi(() -> txtStatus.setText(message));
    }

    // =========================================================
    // TIMER
    // =========================================================

    private void startTimer() {
        stopTimer();
        elapsedSeconds = 0;
        updateTimerText();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                elapsedSeconds++;
                updateTimerText();
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
            timerRunnable = null;
        }
    }

    private void updateTimerText() {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        txtTimer.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void navigateToResults() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ResultsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToTraining() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new TrainingFragment())
                .commit();
    }

    /** Run on the UI thread only if the fragment is still attached. */
    private void runOnUi(Runnable action) {
        if (!isAdded()) {
            return;
        }
        requireActivity().runOnUiThread(() -> {
            if (isAdded()) {
                action.run();
            }
        });
    }

    private int getColorCompat(int colorRes) {
        return requireContext().getColor(colorRes);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
        // Detach this screen but keep the MQTT connection alive for reuse.
        if (mqtt != null) {
            mqtt.clearListener();
        }
    }
}
