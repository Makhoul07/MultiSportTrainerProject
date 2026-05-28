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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneratedRouteFragment extends Fragment {

    private final List<Integer> generatedRoute = new ArrayList<>();

    private TextView routeText;
    private TextView visualTopCone;
    private TextView visualLeftCone;
    private TextView visualCenterCone;
    private TextView visualRightCone;
    private TextView visualBottomCone;

    private MaterialButton continueButton;
    private MaterialButton acceptRouteButton;

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
        MaterialButton regenerateButton = view.findViewById(R.id.btn_regenerate_route);
        acceptRouteButton = view.findViewById(R.id.btn_accept_route);
        continueButton = view.findViewById(R.id.btn_generated_to_setup);

        routeText = view.findViewById(R.id.txt_generated_route);
        visualTopCone = view.findViewById(R.id.visual_top_cone);
        visualLeftCone = view.findViewById(R.id.visual_left_cone);
        visualCenterCone = view.findViewById(R.id.visual_center_cone);
        visualRightCone = view.findViewById(R.id.visual_right_cone);
        visualBottomCone = view.findViewById(R.id.visual_bottom_cone);

        generateFourConeRoute();

        regenerateButton.setOnClickListener(v -> {
            generateFourConeRoute();
            Toast.makeText(getContext(), "New 4-cone route generated", Toast.LENGTH_SHORT).show();
        });

        acceptRouteButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Route accepted", Toast.LENGTH_SHORT).show();
            saveGeneratedRouteToMemory();
        });

        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .commit();
        });

        continueButton.setOnClickListener(v -> validateAndSaveRoute());

        return view;
    }

    private void generateFourConeRoute() {
        List<Integer> cones = new ArrayList<>();
        cones.add(1);
        cones.add(2);
        cones.add(3);
        cones.add(4);

        Collections.shuffle(cones);

        generatedRoute.clear();
        generatedRoute.addAll(cones);

        updateRouteUI();
        saveGeneratedRouteToMemory();
    }

    private void saveGeneratedRouteToMemory() {
        CurrentTrainingData.coneSequence.clear();
        CurrentTrainingData.coneSequence.addAll(generatedRoute);
        CurrentTrainingData.routeType = "Generated";
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

        if (generatedRoute.isEmpty()) {
            Toast.makeText(getContext(), "No generated route found", Toast.LENGTH_SHORT).show();
            return;
        }

        saveGeneratedRouteToMemory();
        saveRouteToBackend();
    }

    private void saveRouteToBackend() {
        continueButton.setEnabled(false);
        acceptRouteButton.setEnabled(false);
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
                acceptRouteButton.setEnabled(true);
                continueButton.setText("Continue to Setup Test");

                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.routeSaved = true;

                    Toast.makeText(
                            getContext(),
                            "Generated route saved",
                            Toast.LENGTH_SHORT
                    ).show();

                    openSetupTest();

                } else {
                    Toast.makeText(
                            getContext(),
                            "Failed to save generated route",
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
                acceptRouteButton.setEnabled(true);
                continueButton.setText("Continue to Setup Test");

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

    private void updateRouteUI() {
        StringBuilder routeBuilder = new StringBuilder();

        for (int i = 0; i < generatedRoute.size(); i++) {
            routeBuilder.append(generatedRoute.get(i));

            if (i < generatedRoute.size() - 1) {
                routeBuilder.append(" → ");
            }
        }

        routeText.setText(routeBuilder.toString());

        visualTopCone.setText(String.valueOf(generatedRoute.get(0)));
        visualLeftCone.setText(String.valueOf(generatedRoute.get(1)));
        visualCenterCone.setText(String.valueOf(generatedRoute.get(2)));
        visualRightCone.setText(String.valueOf(generatedRoute.get(3)));

        visualBottomCone.setText("");
        visualBottomCone.setVisibility(View.INVISIBLE);
    }
}