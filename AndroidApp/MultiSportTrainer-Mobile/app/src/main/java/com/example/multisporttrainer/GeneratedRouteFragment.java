package com.example.multisporttrainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.GenerateRouteRequest;
import com.example.multisporttrainer.models.GenerateRouteResponse;
import com.example.multisporttrainer.models.SaveRouteRequest;
import com.example.multisporttrainer.models.SaveRouteResponse;
import com.example.multisporttrainer.models.StartTrainingRequest;
import com.example.multisporttrainer.models.StartTrainingResponse;
import com.example.multisporttrainer.mqtt.MqttMessages;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Generated route screen. The player picks a difficulty; on continue the app
 * asks the backend (which calls the Claude API server-side) for a personalized
 * cone sequence tailored to the player's stats. The route is run as a "Custom"
 * route so the Pi follows the explicit sequence. If generation fails for any
 * reason we fall back to a default sequence so the session can still start.
 */
public class GeneratedRouteFragment extends Fragment {

    /** Default route used when AI generation fails. */
    private static final List<Integer> FALLBACK_SEQUENCE = Arrays.asList(1, 2, 3);

    private MaterialButton continueButton;
    private RadioGroup difficultyRadioGroup;

    public GeneratedRouteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_generated_route, container, false);

        LinearLayout backButton = view.findViewById(R.id.btn_back_training);
        continueButton = view.findViewById(R.id.btn_generated_to_setup);
        difficultyRadioGroup = view.findViewById(R.id.difficultyRadioGroup);
        applyDifficultyFromCurrent();

        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .commit();
        });

        continueButton.setOnClickListener(v -> validateAndStart());

        return view;
    }

    /** Default the difficulty selector to CurrentTrainingData (e.g. a Retry preset). */
    private void applyDifficultyFromCurrent() {
        String difficulty = CurrentTrainingData.difficulty;
        if (difficulty == null) {
            return;
        }
        switch (difficulty.toLowerCase(java.util.Locale.US)) {
            case "easy":
                difficultyRadioGroup.check(R.id.radioEasy);
                break;
            case "hard":
                difficultyRadioGroup.check(R.id.radioHard);
                break;
            default:
                difficultyRadioGroup.check(R.id.radioMedium);
                break;
        }
    }

    private String selectedDifficulty() {
        int checkedId = difficultyRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioEasy) {
            return "Easy";
        } else if (checkedId == R.id.radioHard) {
            return "Hard";
        }
        return "Medium";
    }

    private void validateAndStart() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true, "Generating your personalized route...");
        requestGeneratedRoute(selectedDifficulty());
    }

    /**
     * Ask the backend for an AI-generated cone sequence. On any failure we fall
     * back to {@link #FALLBACK_SEQUENCE} so the player can still train.
     */
    private void requestGeneratedRoute(String difficulty) {
        GenerateRouteRequest request =
                new GenerateRouteRequest(SessionManager.loggedInUserId, difficulty);

        api().generateRoute(request).enqueue(new Callback<GenerateRouteResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<GenerateRouteResponse> call,
                    @NonNull Response<GenerateRouteResponse> response
            ) {
                GenerateRouteResponse body = response.body();
                if (response.isSuccessful()
                        && body != null
                        && body.getConeSequence() != null
                        && !body.getConeSequence().isEmpty()) {
                    startWithRoute(difficulty, body.getConeSequence());
                } else {
                    Toast.makeText(
                            getContext(),
                            "Couldn't generate a route, using a default one",
                            Toast.LENGTH_SHORT
                    ).show();
                    startWithRoute(difficulty, new ArrayList<>(FALLBACK_SEQUENCE));
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<GenerateRouteResponse> call,
                    @NonNull Throwable t
            ) {
                Toast.makeText(
                        getContext(),
                        "Connection error, using a default route",
                        Toast.LENGTH_SHORT
                ).show();
                startWithRoute(difficulty, new ArrayList<>(FALLBACK_SEQUENCE));
            }
        });
    }

    /**
     * Commit the resolved cone sequence to CurrentTrainingData and kick off the
     * session. Run as a "Custom" route so the Pi follows the explicit sequence.
     */
    private void startWithRoute(String difficulty, List<Integer> sequence) {
        CurrentTrainingData.routeType = "Custom";
        // Sent to the Pi as "Custom" (explicit sequence), but the user's real
        // choice was the AI-generated route — preserve that for results/history.
        CurrentTrainingData.originalRouteType = MqttMessages.MODE_GENERATED;
        CurrentTrainingData.difficulty = difficulty;
        CurrentTrainingData.conesCount = 3;
        CurrentTrainingData.rounds = sequence.size();
        CurrentTrainingData.distractionsEnabled = !difficulty.equalsIgnoreCase("Easy");

        CurrentTrainingData.coneSequence.clear();
        CurrentTrainingData.coneSequence.addAll(sequence);

        createSessionThenSaveRoute();
    }

    private void createSessionThenSaveRoute() {
        setBusy(true, "Starting...");

        // The Pi still runs this as a "Custom" route (explicit sequence), but the
        // backend session should record the user's real choice. Store "Generated"
        // for AI routes so history/stats reflect it; "Custom" otherwise.
        String backendRouteType =
                MqttMessages.MODE_GENERATED.equals(CurrentTrainingData.originalRouteType)
                        ? "Generated"
                        : CurrentTrainingData.routeType;

        StartTrainingRequest request = new StartTrainingRequest(
                SessionManager.loggedInUserId,
                backendRouteType,
                CurrentTrainingData.difficulty,
                CurrentTrainingData.trainingType,
                CurrentTrainingData.conesCount,
                CurrentTrainingData.rounds,
                CurrentTrainingData.distractionsEnabled
        );

        api().startTraining(request).enqueue(new Callback<StartTrainingResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<StartTrainingResponse> call,
                    @NonNull Response<StartTrainingResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.sessionId = response.body().getSessionId();
                    saveRouteToBackend();
                } else {
                    setBusy(false, "Continue");
                    Toast.makeText(getContext(), "Failed to start training", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<StartTrainingResponse> call,
                    @NonNull Throwable t
            ) {
                setBusy(false, "Continue");
                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void saveRouteToBackend() {
        setBusy(true, "Saving...");

        SaveRouteRequest request = new SaveRouteRequest(
                SessionManager.loggedInUserId,
                CurrentTrainingData.sessionId,
                CurrentTrainingData.routeType,
                new ArrayList<>(CurrentTrainingData.coneSequence)
        );

        api().saveRoute(request).enqueue(new Callback<SaveRouteResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<SaveRouteResponse> call,
                    @NonNull Response<SaveRouteResponse> response
            ) {
                setBusy(false, "Continue");

                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.routeSaved = true;
                    openLiveTraining();
                } else {
                    Toast.makeText(getContext(), "Failed to save route", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<SaveRouteResponse> call,
                    @NonNull Throwable t
            ) {
                setBusy(false, "Continue");
                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private ApiService api() {
        return RetrofitClient.getInstance().create(ApiService.class);
    }

    private void openLiveTraining() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LiveTrainingFragment())
                .addToBackStack(null)
                .commit();
    }

    private void setBusy(boolean busy, String label) {
        if (continueButton == null) {
            return;
        }
        continueButton.setEnabled(!busy);
        continueButton.setText(label);
    }
}
