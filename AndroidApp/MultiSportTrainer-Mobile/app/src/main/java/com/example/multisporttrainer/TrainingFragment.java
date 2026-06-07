package com.example.multisporttrainer;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Entry point of the training flow. Only chooses the route mode (Custom vs
 * Generated) and navigates to the matching route screen. Difficulty and the
 * backend training session are now owned by the route screens, since both
 * depend on choices the player makes there.
 */
public class TrainingFragment extends Fragment {

    private boolean isCustomSelected = true;

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

        selectCustomRoute();

        customCard.setOnClickListener(v -> selectCustomRoute());
        generatedCard.setOnClickListener(v -> selectGeneratedRoute());

        continueButton.setOnClickListener(v -> proceedToRouteScreen());

        return view;
    }

    private void proceedToRouteScreen() {
        if (!SessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset all training state; difficulty/route/session are set on the next screen.
        CurrentTrainingData.clear();
        CurrentTrainingData.routeType = isCustomSelected ? "Custom" : "Generated";
        CurrentTrainingData.trainingType = "Football Dribbling";

        if (isCustomSelected) {
            openFragment(new CustomRouteFragment());
        } else {
            openFragment(new GeneratedRouteFragment());
        }
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
