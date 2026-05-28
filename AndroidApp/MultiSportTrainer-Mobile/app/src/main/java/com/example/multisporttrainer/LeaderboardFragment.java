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
import com.example.multisporttrainer.models.LeaderboardResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardFragment extends Fragment {

    private TextView firstNameText, firstScoreText;
    private TextView secondNameText, secondScoreText;
    private TextView thirdNameText, thirdScoreText;

    private TextView fourthRankText, fourthNameText, fourthAccText, fourthScoreText;
    private TextView fifthRankText, fifthNameText, fifthAccText, fifthScoreText;
    private TextView sixthRankText, sixthNameText, sixthAccText, sixthScoreText;

    private TextView currentUserRankText, currentUserNameText, currentUserScoreText, currentUserSubText;

    public LeaderboardFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        firstNameText = view.findViewById(R.id.firstNameText);
        firstScoreText = view.findViewById(R.id.firstScoreText);

        secondNameText = view.findViewById(R.id.secondNameText);
        secondScoreText = view.findViewById(R.id.secondScoreText);

        thirdNameText = view.findViewById(R.id.thirdNameText);
        thirdScoreText = view.findViewById(R.id.thirdScoreText);

        fourthRankText = view.findViewById(R.id.fourthRankText);
        fourthNameText = view.findViewById(R.id.fourthNameText);
        fourthAccText = view.findViewById(R.id.fourthAccText);
        fourthScoreText = view.findViewById(R.id.fourthScoreText);

        fifthRankText = view.findViewById(R.id.fifthRankText);
        fifthNameText = view.findViewById(R.id.fifthNameText);
        fifthAccText = view.findViewById(R.id.fifthAccText);
        fifthScoreText = view.findViewById(R.id.fifthScoreText);

        sixthRankText = view.findViewById(R.id.sixthRankText);
        sixthNameText = view.findViewById(R.id.sixthNameText);
        sixthAccText = view.findViewById(R.id.sixthAccText);
        sixthScoreText = view.findViewById(R.id.sixthScoreText);

        currentUserRankText = view.findViewById(R.id.currentUserRankText);
        currentUserNameText = view.findViewById(R.id.currentUserNameText);
        currentUserScoreText = view.findViewById(R.id.currentUserScoreText);
        currentUserSubText = view.findViewById(R.id.currentUserSubText);

        loadLeaderboard();

        return view;
    }

    private void loadLeaderboard() {
        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.getLeaderboard().enqueue(new Callback<List<LeaderboardResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<LeaderboardResponse>> call,
                    @NonNull Response<List<LeaderboardResponse>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LeaderboardResponse> players = response.body();

                    if (players.isEmpty()) {
                        showEmptyLeaderboard();
                        return;
                    }

                    updateLeaderboard(players);
                } else {
                    Toast.makeText(getContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<LeaderboardResponse>> call,
                    @NonNull Throwable t
            ) {
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateLeaderboard(List<LeaderboardResponse> players) {
        if (players.size() > 0) {
            LeaderboardResponse first = players.get(0);
            firstNameText.setText(shortName(first.getFullName()));
            firstScoreText.setText(String.valueOf(first.getBestScore()));
        }

        if (players.size() > 1) {
            LeaderboardResponse second = players.get(1);
            secondNameText.setText(shortName(second.getFullName()));
            secondScoreText.setText(String.valueOf(second.getBestScore()));
        } else {
            secondNameText.setText("-");
            secondScoreText.setText("0");
        }

        if (players.size() > 2) {
            LeaderboardResponse third = players.get(2);
            thirdNameText.setText(shortName(third.getFullName()));
            thirdScoreText.setText(String.valueOf(third.getBestScore()));
        } else {
            thirdNameText.setText("-");
            thirdScoreText.setText("0");
        }

        bindRankedRow(players, 3, fourthRankText, fourthNameText, fourthAccText, fourthScoreText);
        bindRankedRow(players, 4, fifthRankText, fifthNameText, fifthAccText, fifthScoreText);
        bindRankedRow(players, 5, sixthRankText, sixthNameText, sixthAccText, sixthScoreText);

        updateCurrentUserCard(players);
    }

    private void bindRankedRow(
            List<LeaderboardResponse> players,
            int index,
            TextView rankText,
            TextView nameText,
            TextView accText,
            TextView scoreText
    ) {
        int rank = index + 1;

        rankText.setText(String.valueOf(rank));

        if (players.size() > index) {
            LeaderboardResponse player = players.get(index);

            nameText.setText(player.getFullName());
            accText.setText(String.format("AVG ACC: %.0f%%", player.getAvgAccuracy()));
            scoreText.setText(player.getBestScore() + " pts");
        } else {
            nameText.setText("-");
            accText.setText("AVG ACC: 0%");
            scoreText.setText("0 pts");
        }
    }

    private void updateCurrentUserCard(List<LeaderboardResponse> players) {
        if (!SessionManager.isLoggedIn()) {
            currentUserRankText.setText("-");
            currentUserNameText.setText("You");
            currentUserScoreText.setText("0\npts");
            currentUserSubText.setText("LOGIN TO VIEW\nYOUR RANK");
            return;
        }

        for (int i = 0; i < players.size(); i++) {
            LeaderboardResponse player = players.get(i);

            if (player.getUserId() == SessionManager.loggedInUserId) {
                int rank = i + 1;

                currentUserRankText.setText(String.valueOf(rank));
                currentUserNameText.setText("You (" + shortName(player.getFullName()) + ")");
                currentUserScoreText.setText(player.getBestScore() + "\npts");
                currentUserSubText.setText("TOTAL TRAININGS\n" + player.getTotalTrainings());

                return;
            }
        }

        currentUserRankText.setText("-");
        currentUserNameText.setText("You");
        currentUserScoreText.setText("0\npts");
        currentUserSubText.setText("NO RESULT\nYET");
    }

    private void showEmptyLeaderboard() {
        firstNameText.setText("-");
        firstScoreText.setText("0");

        secondNameText.setText("-");
        secondScoreText.setText("0");

        thirdNameText.setText("-");
        thirdScoreText.setText("0");

        fourthNameText.setText("-");
        fourthAccText.setText("AVG ACC: 0%");
        fourthScoreText.setText("0 pts");

        fifthNameText.setText("-");
        fifthAccText.setText("AVG ACC: 0%");
        fifthScoreText.setText("0 pts");

        sixthNameText.setText("-");
        sixthAccText.setText("AVG ACC: 0%");
        sixthScoreText.setText("0 pts");

        currentUserRankText.setText("-");
        currentUserNameText.setText("You");
        currentUserScoreText.setText("0\npts");
        currentUserSubText.setText("NO RESULT\nYET");
    }

    private String shortName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown";
        }

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0];
        }

        return parts[0] + " " + parts[1].charAt(0) + ".";
    }
}