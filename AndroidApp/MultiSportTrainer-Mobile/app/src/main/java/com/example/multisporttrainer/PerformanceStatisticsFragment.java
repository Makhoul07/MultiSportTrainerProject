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
import com.example.multisporttrainer.models.StatisticsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerformanceStatisticsFragment extends Fragment {

    private TextView totalSessionsText;
    private TextView avgAccuracyText;
    private TextView avgScoreText;
    private TextView personalBestText;

    public PerformanceStatisticsFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_performance_statistics, container, false);

        totalSessionsText = view.findViewById(R.id.totalSessionsText);
        avgAccuracyText = view.findViewById(R.id.avgAccuracyText);
        avgScoreText = view.findViewById(R.id.avgScoreText);
        personalBestText = view.findViewById(R.id.personalBestText);

        loadStatistics();

        return view;
    }

    private void loadStatistics() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "No logged in user", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.getStatistics(SessionManager.loggedInUserId)
                .enqueue(new Callback<StatisticsResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<StatisticsResponse> call,
                            @NonNull Response<StatisticsResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            StatisticsResponse stats = response.body();

                            totalSessionsText.setText(String.valueOf(stats.getTotalTrainings()));
                            avgAccuracyText.setText(String.format("%.0f%% ↗", stats.getAvgAccuracy()));
                            avgScoreText.setText(String.valueOf((int) stats.getAvgScore()));
                            personalBestText.setText(String.valueOf(stats.getBestScore()));

                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "Failed to load statistics",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<StatisticsResponse> call,
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
}