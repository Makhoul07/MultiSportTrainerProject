package com.example.multisporttrainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.mqtt.MqttMessages;
import com.example.multisporttrainer.models.SaveRouteRequest;
import com.example.multisporttrainer.models.SaveRouteResponse;
import com.example.multisporttrainer.models.StartTrainingRequest;
import com.example.multisporttrainer.models.StartTrainingResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Custom route builder. The player taps cones 1-3 in any order to build a
 * sequence, picks a difficulty, then continues. On continue we create the
 * backend training session (with the chosen difficulty + tap count), save the
 * route, and move to the live MQTT-driven session.
 */
public class CustomRouteFragment extends Fragment {

    private static final int MIN_TAPS = 3;

    private final List<Integer> selectedRoute = new ArrayList<>();

    private TextView selectedRouteText;
    private MaterialButton undoLastButton;
    private MaterialButton clearRouteButton;
    private MaterialButton continueButton;
    private RadioGroup difficultyRadioGroup;

    public CustomRouteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_custom_route, container, false);

        LinearLayout backButton = view.findViewById(R.id.btn_back_training);
        continueButton = view.findViewById(R.id.btn_custom_to_setup);

        selectedRouteText = view.findViewById(R.id.txt_selected_route);
        undoLastButton = view.findViewById(R.id.btn_undo_last);
        clearRouteButton = view.findViewById(R.id.btn_clear_route);
        difficultyRadioGroup = view.findViewById(R.id.difficultyRadioGroup);
        applyDifficultyFromCurrent();
        applyRouteFromCurrent();

        MaterialCardView cone1 = view.findViewById(R.id.card_cone_1);
        MaterialCardView cone2 = view.findViewById(R.id.card_cone_2);
        MaterialCardView cone3 = view.findViewById(R.id.card_cone_3);

        cone1.setOnClickListener(v -> addConeToRoute(1));
        cone2.setOnClickListener(v -> addConeToRoute(2));
        cone3.setOnClickListener(v -> addConeToRoute(3));

        undoLastButton.setOnClickListener(v -> undoLastCone());
        clearRouteButton.setOnClickListener(v -> clearRoute());

        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .commit();
        });

        continueButton.setOnClickListener(v -> validateAndStart());

        updateRouteText();
        updateActionButtons();

        return view;
    }

    private void addConeToRoute(int coneNumber) {
        selectedRoute.add(coneNumber);
        updateRouteText();
        updateActionButtons();
    }

    private void undoLastCone() {
        if (selectedRoute.isEmpty()) {
            Toast.makeText(getContext(), "No cone to undo", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedRoute.remove(selectedRoute.size() - 1);
        updateRouteText();
        updateActionButtons();
    }

    private void clearRoute() {
        if (selectedRoute.isEmpty()) {
            Toast.makeText(getContext(), "Route is already empty", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedRoute.clear();
        updateRouteText();
        updateActionButtons();
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

    /**
     * Pre-fill the cone sequence from CurrentTrainingData (e.g. a Retry preset).
     * The route text and action buttons are refreshed by the updateRouteText()
     * / updateActionButtons() calls at the end of onCreateView.
     */
    private void applyRouteFromCurrent() {
        if (CurrentTrainingData.coneSequence.isEmpty()) {
            return;
        }
        selectedRoute.clear();
        selectedRoute.addAll(CurrentTrainingData.coneSequence);
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

        if (selectedRoute.size() < MIN_TAPS) {
            Toast.makeText(
                    getContext(),
                    "Tap at least " + MIN_TAPS + " cones to build a route",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String difficulty = selectedDifficulty();

        // Everything the live MQTT session needs is fixed here, before we navigate.
        CurrentTrainingData.routeType = "Custom";
        CurrentTrainingData.originalRouteType = MqttMessages.MODE_CUSTOM;
        CurrentTrainingData.difficulty = difficulty;
        CurrentTrainingData.conesCount = 3;
        CurrentTrainingData.rounds = selectedRoute.size();
        CurrentTrainingData.distractionsEnabled = !difficulty.equalsIgnoreCase("Easy");

        CurrentTrainingData.coneSequence.clear();
        CurrentTrainingData.coneSequence.addAll(selectedRoute);

        createSessionThenSaveRoute();
    }

    private void createSessionThenSaveRoute() {
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
                    Toast.makeText(getContext(), "Route saved", Toast.LENGTH_SHORT).show();
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

    private void updateRouteText() {
        if (selectedRoute.isEmpty()) {
            selectedRouteText.setText("Selected Route: none");
            return;
        }

        StringBuilder routeBuilder = new StringBuilder();

        for (int i = 0; i < selectedRoute.size(); i++) {
            routeBuilder.append(selectedRoute.get(i));

            if (i < selectedRoute.size() - 1) {
                routeBuilder.append(" → ");
            }
        }

        selectedRouteText.setText("Selected Route: " + routeBuilder);
    }

    private void updateActionButtons() {
        boolean hasRoute = !selectedRoute.isEmpty();

        undoLastButton.setEnabled(hasRoute);
        clearRouteButton.setEnabled(hasRoute);

        undoLastButton.setAlpha(hasRoute ? 1f : 0.45f);
        clearRouteButton.setAlpha(hasRoute ? 1f : 0.45f);
    }
}
