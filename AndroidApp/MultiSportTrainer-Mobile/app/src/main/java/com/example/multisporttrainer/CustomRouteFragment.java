package com.example.multisporttrainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.SaveRouteRequest;
import com.example.multisporttrainer.models.SaveRouteResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomRouteFragment extends Fragment {

    private final List<Integer> selectedRoute = new ArrayList<>();

    private TextView selectedRouteText;
    private MaterialButton undoLastButton;
    private MaterialButton clearRouteButton;
    private MaterialButton continueButton;

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

        MaterialCardView cone1 = view.findViewById(R.id.card_cone_1);
        MaterialCardView cone2 = view.findViewById(R.id.card_cone_2);
        MaterialCardView cone3 = view.findViewById(R.id.card_cone_3);
        MaterialCardView cone4 = view.findViewById(R.id.card_cone_4);
        MaterialCardView cone5 = view.findViewById(R.id.card_cone_5);

        cone1.setOnClickListener(v -> addConeToRoute(1));
        cone2.setOnClickListener(v -> addConeToRoute(2));
        cone3.setOnClickListener(v -> addConeToRoute(3));
        cone4.setOnClickListener(v -> addConeToRoute(4));
        cone5.setOnClickListener(v -> addConeToRoute(5));

        undoLastButton.setOnClickListener(v -> undoLastCone());
        clearRouteButton.setOnClickListener(v -> clearRoute());

        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .commit();
        });

        continueButton.setOnClickListener(v -> validateAndSaveRoute());

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

    private void validateAndSaveRoute() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CurrentTrainingData.sessionId == -1) {
            Toast.makeText(getContext(), "Training session was not created", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRoute.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one cone", Toast.LENGTH_SHORT).show();
            return;
        }

        CurrentTrainingData.coneSequence.clear();
        CurrentTrainingData.coneSequence.addAll(selectedRoute);
        CurrentTrainingData.routeType = "Custom";

        saveRouteToBackend();
    }

    private void saveRouteToBackend() {
        continueButton.setEnabled(false);
        continueButton.setText("Saving...");

        SaveRouteRequest request = new SaveRouteRequest(
                SessionManager.loggedInUserId,
                CurrentTrainingData.sessionId,
                CurrentTrainingData.routeType,
                new ArrayList<>(CurrentTrainingData.coneSequence)
        );

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.saveRoute(request).enqueue(new Callback<SaveRouteResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<SaveRouteResponse> call,
                    @NonNull Response<SaveRouteResponse> response
            ) {
                continueButton.setEnabled(true);
                continueButton.setText("Continue");

                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.routeSaved = true;

                    Toast.makeText(
                            getContext(),
                            "Route saved successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    openSetupTest();

                } else {
                    Toast.makeText(
                            getContext(),
                            "Failed to save route",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<SaveRouteResponse> call,
                    @NonNull Throwable t
            ) {
                continueButton.setEnabled(true);
                continueButton.setText("Continue");

                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void openSetupTest() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SetupTestFragment())
                .addToBackStack(null)
                .commit();
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