package com.example.multisporttrainer;

import android.content.Intent;
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
import com.example.multisporttrainer.models.UserProfileResponse;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView profileNameText;
    private TextView detailsFullNameText;
    private TextView detailsEmailText;
    private TextView detailsDobText;
    private TextView detailsRoleText;
    private TextView statTotalTrainingsText;
    private TextView statBestScoreText;
    private TextView statAvgAccuracyText;

    public ProfileFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileNameText = view.findViewById(R.id.profileNameText);
        detailsFullNameText = view.findViewById(R.id.detailsFullNameText);
        detailsEmailText = view.findViewById(R.id.detailsEmailText);
        detailsDobText = view.findViewById(R.id.detailsDobText);
        detailsRoleText = view.findViewById(R.id.detailsRoleText);
        statTotalTrainingsText = view.findViewById(R.id.statTotalTrainingsText);
        statBestScoreText = view.findViewById(R.id.statBestScoreText);
        statAvgAccuracyText = view.findViewById(R.id.statAvgAccuracyText);

        // Show a loading state until the backend responds (replaces the static
        // placeholder values baked into the layout).
        statTotalTrainingsText.setText("…");
        statBestScoreText.setText("…");
        statAvgAccuracyText.setText("…");

        loadProfileFromBackend();

        view.findViewById(R.id.editProfileButton).setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        view.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            SessionManager.clearSession(requireContext());

            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    /** Reset the stat cards to zero when the profile/stats can't be loaded. */
    private void showStatsUnavailable() {
        statTotalTrainingsText.setText("0");
        statBestScoreText.setText("0");
        statAvgAccuracyText.setText("0%");
    }

    private void loadProfileFromBackend() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "No logged in user", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.getUserProfile(SessionManager.loggedInUserId)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<UserProfileResponse> call,
                            @NonNull Response<UserProfileResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse user = response.body();

                            profileNameText.setText(user.getFullName());
                            detailsFullNameText.setText(user.getFullName());
                            detailsEmailText.setText(user.getEmail());

                            if (user.getDateOfBirth() != null) {
                                detailsDobText.setText(user.getDateOfBirth().substring(0, 10));
                            } else {
                                detailsDobText.setText("Not set");
                            }

                            detailsRoleText.setText(user.getRole());

                            // Real stats come from the same response (backend returns
                            // 0 for each when the user has no results yet).
                            statTotalTrainingsText.setText(String.valueOf(user.getTotalTrainings()));
                            statBestScoreText.setText(String.valueOf(user.getBestScore()));
                            statAvgAccuracyText.setText(
                                    String.format(Locale.US, "%.0f%%", user.getAvgAccuracy()));

                            ProfileData.fullName = user.getFullName();
                            ProfileData.email = user.getEmail();
                            ProfileData.dob = detailsDobText.getText().toString();
                            ProfileData.role = user.getRole();

                        } else {
                            showStatsUnavailable();
                            Toast.makeText(
                                    getContext(),
                                    "Failed to load profile",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<UserProfileResponse> call,
                            @NonNull Throwable t
                    ) {
                        showStatsUnavailable();
                        Toast.makeText(
                                getContext(),
                                "Connection error: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}