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
import com.example.multisporttrainer.models.StartTrainingRequest;
import com.example.multisporttrainer.models.StartTrainingResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Generated route screen. The player only picks a difficulty; the cone rig (Pi)
 * generates the actual route at runtime, so there is no local route to build or
 * save. On continue we create the backend session with the chosen difficulty
 * and go straight to the live MQTT-driven session.
 */
public class GeneratedRouteFragment extends Fragment {

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

    private String selectedDifficulty() {
        int checkedId = difficultyRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioEasy) {
            return "Easy";
        } else if (checkedId == R.id.radioHard) {
            return "Hard";
        }
        return "Medium";
    }

    /** Mirrors training_simulator.get_rounds_from_difficulty so the UI matches the rig. */
    private int roundsForDifficulty(String difficulty) {
        if (difficulty.equalsIgnoreCase("Easy")) {
            return 4;
        } else if (difficulty.equalsIgnoreCase("Hard")) {
            return 8;
        }
        return 6;
    }

    private void validateAndStart() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String difficulty = selectedDifficulty();

        // The Pi owns the route, so the app sends no cone sequence.
        CurrentTrainingData.routeType = "Generated";
        CurrentTrainingData.difficulty = difficulty;
        CurrentTrainingData.conesCount = 3;
        CurrentTrainingData.rounds = roundsForDifficulty(difficulty);
        CurrentTrainingData.distractionsEnabled = !difficulty.equalsIgnoreCase("Easy");
        CurrentTrainingData.coneSequence.clear();

        createSession();
    }

    private void createSession() {
        setBusy(true, "Starting...");

        StartTrainingRequest request = new StartTrainingRequest(
                SessionManager.loggedInUserId,
                CurrentTrainingData.routeType,
                CurrentTrainingData.difficulty,
                CurrentTrainingData.trainingType,
                CurrentTrainingData.conesCount,
                CurrentTrainingData.rounds,
                CurrentTrainingData.distractionsEnabled
        );

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.startTraining(request).enqueue(new Callback<StartTrainingResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<StartTrainingResponse> call,
                    @NonNull Response<StartTrainingResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.sessionId = response.body().getSessionId();
                    openLiveTraining();
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
