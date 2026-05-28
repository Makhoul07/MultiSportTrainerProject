package com.example.multisporttrainer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.HistoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private TextView historyTrainingTypeText;
    private TextView historyDateText;
    private TextView historyDifficultyText;
    private TextView historyScoreText;
    private TextView historyAccuracyText;
    private TextView historyDurationText;
    private TextView weeklyGainText;
    private TextView totalTimeText;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyTrainingTypeText = view.findViewById(R.id.historyTrainingTypeText);
        historyDateText = view.findViewById(R.id.historyDateText);
        historyDifficultyText = view.findViewById(R.id.historyDifficultyText);
        historyScoreText = view.findViewById(R.id.historyScoreText);
        historyAccuracyText = view.findViewById(R.id.historyAccuracyText);
        historyDurationText = view.findViewById(R.id.historyDurationText);
        weeklyGainText = view.findViewById(R.id.weeklyGainText);
        totalTimeText = view.findViewById(R.id.totalTimeText);

        loadHistoryFromBackend();

        return view;
    }

    private void loadHistoryFromBackend() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "No logged in user", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.getHistory(SessionManager.loggedInUserId)
                .enqueue(new Callback<List<HistoryResponse>>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<List<HistoryResponse>> call,
                            @NonNull Response<List<HistoryResponse>> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<HistoryResponse> historyList = response.body();

                            if (historyList.isEmpty()) {
                                showEmptyHistory();
                                return;
                            }

                            HistoryResponse latestSession = historyList.get(0);

                            updateLatestHistoryCard(latestSession);
                            updateSummaryCards(historyList);

                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "Failed to load history",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<List<HistoryResponse>> call,
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

    private void updateLatestHistoryCard(HistoryResponse session) {
        historyTrainingTypeText.setText(session.getTrainingType());

        String dateText = formatDate(session.getCreatedAt());
        historyDateText.setText("▣ " + dateText);

        historyDifficultyText.setText(session.getDifficulty().toUpperCase());

        historyScoreText.setText(String.valueOf(session.getScore()));
        historyAccuracyText.setText(String.format("%.0f%%", session.getAccuracy()));
        historyDurationText.setText(formatDuration(session.getDurationSeconds()));
    }

    private void updateSummaryCards(List<HistoryResponse> historyList) {
        int totalSeconds = 0;

        for (HistoryResponse item : historyList) {
            totalSeconds += item.getDurationSeconds();
        }

        totalTimeText.setText(formatTotalTime(totalSeconds));

        if (historyList.size() >= 2) {
            int latestScore = historyList.get(0).getScore();
            int previousScore = historyList.get(1).getScore();

            if (previousScore > 0) {
                double gain = ((double) (latestScore - previousScore) / previousScore) * 100;
                weeklyGainText.setText(String.format("%+.0f%%", gain));
            } else {
                weeklyGainText.setText("+0%");
            }
        } else {
            weeklyGainText.setText("+0%");
        }
    }

    private void showEmptyHistory() {
        historyTrainingTypeText.setText("No training yet");
        historyDateText.setText("▣ Start your first session");
        historyDifficultyText.setText("NONE");
        historyScoreText.setText("0");
        historyAccuracyText.setText("0%");
        historyDurationText.setText("00:00");
        weeklyGainText.setText("+0%");
        totalTimeText.setText("0h 0m");
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private String formatTotalTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;

        return hours + "h " + minutes + "m";
    }

    private String formatDate(String createdAt) {
        if (createdAt == null || createdAt.length() < 10) {
            return "Unknown date";
        }

        return createdAt.substring(0, 10);
    }
}