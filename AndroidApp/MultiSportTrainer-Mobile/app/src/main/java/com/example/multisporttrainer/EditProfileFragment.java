package com.example.multisporttrainer;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.UpdateUserRequest;
import com.example.multisporttrainer.models.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private EditText editFullName;
    private EditText editEmail;
    private EditText editDob;
    private EditText editRole;

    public EditProfileFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editFullName = view.findViewById(R.id.editFullName);
        editEmail = view.findViewById(R.id.editEmail);
        editDob = view.findViewById(R.id.editDob);
        editRole = view.findViewById(R.id.editRole);

        loadCurrentProfileData();

        view.findViewById(R.id.saveProfileButton).setOnClickListener(v -> updateProfileInBackend());

        view.findViewById(R.id.cancelEditButton).setOnClickListener(v -> openProfilePage());

        view.findViewById(R.id.backButton).setOnClickListener(v -> openProfilePage());

        return view;
    }

    private void loadCurrentProfileData() {
        editFullName.setText(ProfileData.fullName);
        editEmail.setText(ProfileData.email);

        if (ProfileData.dob == null || ProfileData.dob.equals("Not set")) {
            editDob.setText("");
        } else {
            editDob.setText(ProfileData.dob);
        }

        editRole.setText(ProfileData.role);
    }

    private void updateProfileInBackend() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "No logged in user", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String dob = editDob.getText().toString().trim();
        String role = editRole.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            editFullName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(role)) {
            editRole.setError("Role is required");
            return;
        }

        String dateOfBirthForApi = null;

        if (!TextUtils.isEmpty(dob)) {
            dateOfBirthForApi = dob;
        }

        UpdateUserRequest request = new UpdateUserRequest(
                fullName,
                email,
                dateOfBirthForApi,
                role,
                "Football & Agility"
        );

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        apiService.updateUserProfile(SessionManager.loggedInUserId, request)
                .enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<UserProfileResponse> call,
                            @NonNull Response<UserProfileResponse> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileResponse user = response.body();

                            ProfileData.fullName = user.getFullName();
                            ProfileData.email = user.getEmail();
                            ProfileData.role = user.getRole();

                            if (user.getDateOfBirth() != null) {
                                ProfileData.dob = user.getDateOfBirth().substring(0, 10);
                            } else {
                                ProfileData.dob = "Not set";
                            }

                            SessionManager.loggedInFullName = user.getFullName();
                            SessionManager.loggedInEmail = user.getEmail();
                            SessionManager.loggedInRole = user.getRole();
                            SessionManager.loggedInSportFocus = user.getSportFocus();

                            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            openProfilePage();

                        } else {
                            Toast.makeText(
                                    getContext(),
                                    "Failed to update profile. Email may already exist.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<UserProfileResponse> call,
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

    private void openProfilePage() {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .commit();
    }
}