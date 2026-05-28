package com.example.multisporttrainer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.SaveResultRequest;
import com.example.multisporttrainer.models.SaveResultResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsFragment extends Fragment {

    private TextView finalScoreText;
    private TextView resultTimeText;
    private TextView resultAccuracyText;
    private TextView resultMistakesText;
    private TextView resultReactionText;
    private TextView resultInsightText;

    private MaterialButton newTrainingButton;
    private MaterialButton viewStatsButton;
    private MaterialButton viewHistoryButton;

    public ResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        finalScoreText = view.findViewById(R.id.resultFinalScoreText);
        resultTimeText = view.findViewById(R.id.resultTimeText);
        resultAccuracyText = view.findViewById(R.id.resultAccuracyText);
        resultMistakesText = view.findViewById(R.id.resultMistakesText);
        resultReactionText = view.findViewById(R.id.resultReactionText);
        resultInsightText = view.findViewById(R.id.resultInsightText);

        newTrainingButton = view.findViewById(R.id.btn_new_training);
        viewStatsButton = view.findViewById(R.id.btn_view_stats);
        viewHistoryButton = view.findViewById(R.id.btn_view_history);

        showCurrentResultData();
        saveResultAutomatically();

        newTrainingButton.setOnClickListener(v -> {
            CurrentTrainingData.clear();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewStatsButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PerformanceStatisticsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewHistoryButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void showCurrentResultData() {
        finalScoreText.setText(String.valueOf(CurrentTrainingData.score));
        resultTimeText.setText(formatDuration(CurrentTrainingData.durationSeconds));
        resultAccuracyText.setText(String.format("%.0f%%", CurrentTrainingData.accuracy));
        resultMistakesText.setText(String.valueOf(CurrentTrainingData.mistakes));
        resultReactionText.setText(String.format("%.1fs", CurrentTrainingData.averageReactionSeconds));

        String insight = "You completed a "
                + CurrentTrainingData.routeType.toLowerCase()
                + " route with "
                + CurrentTrainingData.coneSequence.size()
                + " cones. Keep training to improve reaction speed and reduce mistakes.";

        resultInsightText.setText(insight);
    }

    private void saveResultAutomatically() {
        if (CurrentTrainingData.resultSaved) {
            return;
        }

        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CurrentTrainingData.sessionId == -1) {
            Toast.makeText(getContext(), "No active training session found", Toast.LENGTH_SHORT).show();
            return;
        }

        SaveResultRequest request = new SaveResultRequest(
                CurrentTrainingData.sessionId,
                SessionManager.loggedInUserId,
                CurrentTrainingData.score,
                CurrentTrainingData.accuracy,
                CurrentTrainingData.mistakes,
                CurrentTrainingData.durationSeconds,
                CurrentTrainingData.averageReactionSeconds,
                CurrentTrainingData.bestReactionSeconds
        );

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.saveResult(request).enqueue(new Callback<SaveResultResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<SaveResultResponse> call,
                    @NonNull Response<SaveResultResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    CurrentTrainingData.resultSaved = true;

                    Toast.makeText(
                            getContext(),
                            "Result saved successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {
                    Toast.makeText(
                            getContext(),
                            "Failed to save result",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<SaveResultResponse> call,
                    @NonNull Throwable t
            ) {
                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}