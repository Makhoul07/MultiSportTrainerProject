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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.HistoryResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private RecyclerView historyRecyclerView;
    private TextView historyEmptyText;
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

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        historyEmptyText = view.findViewById(R.id.historyEmptyText);
        weeklyGainText = view.findViewById(R.id.weeklyGainText);
        totalTimeText = view.findViewById(R.id.totalTimeText);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
                        if (!isAdded()) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            List<HistoryResponse> historyList = response.body();

                            if (historyList.isEmpty()) {
                                showEmptyHistory();
                            } else {
                                showHistory(historyList);
                            }

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
                        if (!isAdded()) {
                            return;
                        }
                        Toast.makeText(
                                getContext(),
                                "Connection error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void showHistory(List<HistoryResponse> historyList) {
        historyEmptyText.setVisibility(View.GONE);
        historyRecyclerView.setVisibility(View.VISIBLE);

        historyRecyclerView.setAdapter(new HistoryAdapter(historyList, this::onRetry));

        updateSummaryCards(historyList);
    }

    private void showEmptyHistory() {
        historyRecyclerView.setVisibility(View.GONE);
        historyEmptyText.setVisibility(View.VISIBLE);

        weeklyGainText.setText("+0%");
        totalTimeText.setText("0h 0m");
    }

    /**
     * Retry a past session: carry its route type, difficulty and training type into
     * a fresh session, then jump straight to the matching route screen — skipping
     * TrainingFragment's mode picker. The route screen creates the backend session
     * (POST /api/Training/start) on Continue and pre-fills difficulty from
     * CurrentTrainingData.
     */
    private void onRetry(HistoryResponse item) {
        boolean isGenerated = "Generated".equalsIgnoreCase(item.getRouteType());

        CurrentTrainingData.clear();
        CurrentTrainingData.routeType = isGenerated ? "Generated" : "Custom";
        CurrentTrainingData.difficulty = item.getDifficulty();
        CurrentTrainingData.trainingType = item.getTrainingType();

        Fragment routeScreen = isGenerated
                ? new GeneratedRouteFragment()
                : new CustomRouteFragment();

        // Reflect the training flow in the bottom nav without re-firing the nav
        // listener (which would swap in a throwaway TrainingFragment).
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_training).setChecked(true);
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, routeScreen)
                .addToBackStack(null)
                .commit();
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

    private String formatTotalTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;

        return hours + "h " + minutes + "m";
    }
}
