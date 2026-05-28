package com.example.multisporttrainer;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.StartTrainingRequest;
import com.example.multisporttrainer.models.StartTrainingResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrainingFragment extends Fragment {

    private boolean isCustomSelected = true;
    private String selectedDifficulty = "Medium";

    private MaterialCardView customCard;
    private MaterialCardView generatedCard;
    private ImageView customRouteIcon;
    private ImageView generatedRouteIcon;
    private MaterialButton continueButton;

    public TrainingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_training, container, false);

        customCard = view.findViewById(R.id.card_custom_route);
        generatedCard = view.findViewById(R.id.card_generated_route);

        customRouteIcon = view.findViewById(R.id.icon_custom_route);
        generatedRouteIcon = view.findViewById(R.id.icon_generated_route);

        continueButton = view.findViewById(R.id.btn_continue_setup);

        RadioGroup difficultyRadioGroup = view.findViewById(R.id.difficultyRadioGroup);

        selectCustomRoute();

        difficultyRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEasy) {
                selectedDifficulty = "Easy";
            } else if (checkedId == R.id.radioMedium) {
                selectedDifficulty = "Medium";
            } else if (checkedId == R.id.radioHard) {
                selectedDifficulty = "Hard";
            } else {
                selectedDifficulty = "Medium";
            }
        });

        customCard.setOnClickListener(v -> selectCustomRoute());

        generatedCard.setOnClickListener(v -> selectGeneratedRoute());

        continueButton.setOnClickListener(v -> startTrainingSession());

        return view;
    }

    private void startTrainingSession() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        CurrentTrainingData.clear();

        CurrentTrainingData.routeType = isCustomSelected ? "Custom" : "Generated";
        CurrentTrainingData.difficulty = selectedDifficulty;
        CurrentTrainingData.trainingType = "Football Dribbling";
        CurrentTrainingData.conesCount = 4;
        CurrentTrainingData.rounds = 3;
        CurrentTrainingData.distractionsEnabled = true;

        continueButton.setEnabled(false);
        continueButton.setText("Starting...");

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
                continueButton.setEnabled(true);
                continueButton.setText("CONTINUE");

                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.sessionId = response.body().getSessionId();

                    if (isCustomSelected) {
                        openFragment(new CustomRouteFragment());
                    } else {
                        openFragment(new GeneratedRouteFragment());
                    }

                } else {
                    Toast.makeText(
                            getContext(),
                            "Failed to start training",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<StartTrainingResponse> call,
                    @NonNull Throwable t
            ) {
                continueButton.setEnabled(true);
                continueButton.setText("CONTINUE");

                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void openFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void selectCustomRoute() {
        isCustomSelected = true;

        int primaryBlue = ContextCompat.getColor(requireContext(), R.color.primary_blue);
        int textGray = ContextCompat.getColor(requireContext(), R.color.text_gray);
        int cardBorder = ContextCompat.getColor(requireContext(), R.color.card_border);

        customCard.setStrokeColor(primaryBlue);
        customCard.setStrokeWidth(dpToPx(2));
        customCard.setCardElevation(dpToPx(4));

        generatedCard.setStrokeColor(cardBorder);
        generatedCard.setStrokeWidth(dpToPx(1));
        generatedCard.setCardElevation(dpToPx(2));

        customRouteIcon.setImageTintList(ColorStateList.valueOf(primaryBlue));
        generatedRouteIcon.setImageTintList(ColorStateList.valueOf(textGray));
    }

    private void selectGeneratedRoute() {
        isCustomSelected = false;

        int primaryBlue = ContextCompat.getColor(requireContext(), R.color.primary_blue);
        int textGray = ContextCompat.getColor(requireContext(), R.color.text_gray);
        int cardBorder = ContextCompat.getColor(requireContext(), R.color.card_border);

        generatedCard.setStrokeColor(primaryBlue);
        generatedCard.setStrokeWidth(dpToPx(2));
        generatedCard.setCardElevation(dpToPx(4));

        customCard.setStrokeColor(cardBorder);
        customCard.setStrokeWidth(dpToPx(1));
        customCard.setCardElevation(dpToPx(2));

        generatedRouteIcon.setImageTintList(ColorStateList.valueOf(primaryBlue));
        customRouteIcon.setImageTintList(ColorStateList.valueOf(textGray));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}