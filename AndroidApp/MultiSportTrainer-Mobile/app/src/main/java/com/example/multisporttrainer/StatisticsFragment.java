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
import com.example.multisporttrainer.models.LatestResultResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {

    private TextView scoreText;
    private TextView durationText;
    private TextView accuracyText;
    private TextView mistakesText;

    public StatisticsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        scoreText = view.findViewById(R.id.latestScoreText);
        durationText = view.findViewById(R.id.latestDurationText);
        accuracyText = view.findViewById(R.id.latestAccuracyText);
        mistakesText = view.findViewById(R.id.latestMistakesText);

        loadLatestResult();

        view.findViewById(R.id.viewHistoryButton).setOnClickListener(v ->
                openScreen(new HistoryFragment()));

        view.findViewById(R.id.viewLeaderboardButton).setOnClickListener(v ->
                openScreen(new LeaderboardFragment()));

        view.findViewById(R.id.viewStatisticsButton).setOnClickListener(v ->
                openScreen(new PerformanceStatisticsFragment()));

        return view;
    }

    private void openScreen(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadLatestResult() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "No logged in user", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.getLatestResult(SessionManager.loggedInUserId)
                .enqueue(new Callback<LatestResultResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<LatestResultResponse> call,
                            @NonNull Response<LatestResultResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            LatestResultResponse result = response.body();

                            scoreText.setText(String.valueOf(result.getScore()));
                            durationText.setText(formatDuration(result.getDurationSeconds()));
                            accuracyText.setText(String.format("%.0f%%", result.getAccuracy()));
                            mistakesText.setText(String.valueOf(result.getMistakes()));

                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "No latest result found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<LatestResultResponse> call,
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