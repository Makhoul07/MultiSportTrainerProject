package com.example.multisporttrainer;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private TextView welcomeText;
    private PopupWindow currentPopupWindow;

    public HomeFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Welcome text with real logged-in user
        welcomeText = view.findViewById(R.id.welcomeText);
        updateWelcomeText();

        // Top-right profile icon opens sidebar
        view.findViewById(R.id.profileIcon).setOnClickListener(v -> showProfileSidebar(view));

        // Start training
        view.findViewById(R.id.startTrainingCard).setOnClickListener(v -> {
            openFragment(new TrainingFragment());
            selectBottomNavItem(R.id.nav_training);
        });

        view.findViewById(R.id.startTrainingButton).setOnClickListener(v -> {
            openFragment(new TrainingFragment());
            selectBottomNavItem(R.id.nav_training);
        });

        // Quick Access cards
        view.findViewById(R.id.profileCard).setOnClickListener(v -> {
            openFragment(new ProfileFragment());
            selectBottomNavItem(R.id.nav_profile);
        });

        view.findViewById(R.id.statsCard).setOnClickListener(v -> {
            openFragment(new StatisticsFragment());
            selectBottomNavItem(R.id.nav_stats);
        });

        view.findViewById(R.id.historyCard).setOnClickListener(v -> {
            openFragment(new HistoryFragment());
            selectBottomNavItem(R.id.nav_history);
        });

        return view;
    }

    private void updateWelcomeText() {
        String fullName = getLoggedInFullName();
        String firstName = getFirstName(fullName);

        welcomeText.setText("Welcome back, " + firstName);
    }

    private String getLoggedInFullName() {
        if (SessionManager.loggedInFullName != null && !SessionManager.loggedInFullName.trim().isEmpty()) {
            return SessionManager.loggedInFullName;
        }

        if (ProfileData.fullName != null && !ProfileData.fullName.trim().isEmpty()) {
            return ProfileData.fullName;
        }

        return "Player";
    }

    private String getFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Player";
        }

        return fullName.trim().split("\\s+")[0];
    }

    private void showProfileSidebar(View anchorView) {
        int sidebarWidth = dpToPx(285);

        LinearLayout sidebarLayout = new LinearLayout(requireContext());
        sidebarLayout.setOrientation(LinearLayout.VERTICAL);
        sidebarLayout.setPadding(dpToPx(18), dpToPx(22), dpToPx(18), dpToPx(18));

        GradientDrawable sidebarBackground = new GradientDrawable();
        sidebarBackground.setColor(Color.WHITE);
        sidebarBackground.setCornerRadii(new float[]{
                dpToPx(18), dpToPx(18),
                0, 0,
                0, 0,
                dpToPx(18), dpToPx(18)
        });
        sidebarLayout.setBackground(sidebarBackground);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sidebarLayout.setElevation(dpToPx(10));
        }

        TextView title = createText("Logged In User", 20, "#111827", true);
        sidebarLayout.addView(title);

        TextView subtitle = createText("Current athlete profile", 12, "#6B7280", false);
        subtitle.setPadding(0, dpToPx(4), 0, dpToPx(20));
        sidebarLayout.addView(subtitle);

        TextView avatar = new TextView(requireContext());
        avatar.setText("👤");
        avatar.setTextSize(34);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTextColor(Color.parseColor("#0066D9"));

        LinearLayout.LayoutParams avatarParams =
                new LinearLayout.LayoutParams(dpToPx(76), dpToPx(76));
        avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
        avatarParams.setMargins(0, 0, 0, dpToPx(14));

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#EAF3FF"));
        avatarBg.setStroke(dpToPx(2), Color.parseColor("#D8E3F5"));
        avatar.setBackground(avatarBg);

        sidebarLayout.addView(avatar, avatarParams);

        String fullName = getLoggedInFullName();
        String roleText = getLoggedInRole();
        String emailText = getLoggedInEmail();

        TextView name = createText(fullName, 18, "#111827", true);
        name.setGravity(Gravity.CENTER);
        sidebarLayout.addView(name);

        TextView role = createText(roleText, 13, "#6B7280", false);
        role.setGravity(Gravity.CENTER);
        role.setPadding(0, dpToPx(4), 0, dpToPx(18));
        sidebarLayout.addView(role);

        sidebarLayout.addView(createInfoBlock("Email", emailText));
        sidebarLayout.addView(createInfoBlock("Date of Birth", ProfileData.dob));
        sidebarLayout.addView(createInfoBlock("Sport Focus", "Football & Agility"));
        sidebarLayout.addView(createInfoBlock("Best Score", "920"));
        sidebarLayout.addView(createInfoBlock("Average Accuracy", "87%"));

        TextView viewProfileButton = createSidebarButton("VIEW PROFILE", "#0066D9", "#FFFFFF");
        viewProfileButton.setOnClickListener(v -> {
            if (currentPopupWindow != null) {
                currentPopupWindow.dismiss();
            }

            openFragment(new ProfileFragment());
            selectBottomNavItem(R.id.nav_profile);
        });

        LinearLayout.LayoutParams viewProfileParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                );
        viewProfileParams.setMargins(0, dpToPx(18), 0, dpToPx(10));
        sidebarLayout.addView(viewProfileButton, viewProfileParams);

        TextView closeButton = createSidebarButton("CLOSE", "#FFFFFF", "#0066D9");
        closeButton.setOnClickListener(v -> {
            if (currentPopupWindow != null) {
                currentPopupWindow.dismiss();
            }
        });

        sidebarLayout.addView(
                closeButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(48)
                )
        );

        currentPopupWindow = new PopupWindow(
                sidebarLayout,
                sidebarWidth,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );

        currentPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        currentPopupWindow.setOutsideTouchable(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            currentPopupWindow.setElevation(dpToPx(12));
        }

        currentPopupWindow.showAtLocation(anchorView, Gravity.END, 0, 0);
    }

    private String getLoggedInRole() {
        if (SessionManager.loggedInRole != null && !SessionManager.loggedInRole.trim().isEmpty()) {
            return SessionManager.loggedInRole;
        }

        if (ProfileData.role != null && !ProfileData.role.trim().isEmpty()) {
            return ProfileData.role;
        }

        return "Player";
    }

    private String getLoggedInEmail() {
        if (SessionManager.loggedInEmail != null && !SessionManager.loggedInEmail.trim().isEmpty()) {
            return SessionManager.loggedInEmail;
        }

        if (ProfileData.email != null && !ProfileData.email.trim().isEmpty()) {
            return ProfileData.email;
        }

        return "Not set";
    }

    private LinearLayout createInfoBlock(String label, String value) {
        LinearLayout block = new LinearLayout(requireContext());
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F3F8FF"));
        bg.setStroke(dpToPx(1), Color.parseColor("#D8E3F5"));
        bg.setCornerRadius(dpToPx(10));
        block.setBackground(bg);

        TextView labelText = createText(label, 10, "#6B7280", true);
        TextView valueText = createText(value, 13, "#111827", true);

        block.addView(labelText);
        block.addView(valueText);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        params.setMargins(0, 0, 0, dpToPx(8));
        block.setLayoutParams(params);

        return block;
    }

    private TextView createText(String text, int sizeSp, String colorHex, boolean bold) {
        TextView textView = new TextView(requireContext());

        if (text == null || text.trim().isEmpty()) {
            text = "Not set";
        }

        textView.setText(text);
        textView.setTextSize(sizeSp);
        textView.setTextColor(Color.parseColor(colorHex));

        if (bold) {
            textView.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return textView;
    }

    private TextView createSidebarButton(String text, String backgroundColor, String textColor) {
        TextView button = new TextView(requireContext());
        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(12);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setTextColor(Color.parseColor(textColor));
        button.setClickable(true);
        button.setFocusable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(backgroundColor));
        bg.setCornerRadius(dpToPx(12));
        bg.setStroke(dpToPx(1), Color.parseColor("#0066D9"));
        button.setBackground(bg);

        return button;
    }

    private void openFragment(Fragment fragment) {
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void selectBottomNavItem(int itemId) {
        BottomNavigationView bottomNavigationView =
                requireActivity().findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(itemId);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}