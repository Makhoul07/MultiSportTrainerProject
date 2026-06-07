package com.example.multisporttrainer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class ResultsFragment extends Fragment {

    private TextView finalScoreText;
    private TextView resultTimeText;
    private TextView resultAccuracyText;
    private TextView resultMistakesText;
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
        resultInsightText = view.findViewById(R.id.resultInsightText);

        newTrainingButton = view.findViewById(R.id.btn_new_training);
        viewStatsButton = view.findViewById(R.id.btn_view_stats);
        viewHistoryButton = view.findViewById(R.id.btn_view_history);

        showCurrentResultData();

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

        String insight = "You completed a "
                + CurrentTrainingData.routeType.toLowerCase()
                + " route with "
                + CurrentTrainingData.coneSequence.size()
                + " cones. Keep training to improve accuracy and reduce mistakes.";

        resultInsightText.setText(insight);
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}