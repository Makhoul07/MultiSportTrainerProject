package com.example.multisporttrainer;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;

public class SetupTestFragment extends Fragment {

    public SetupTestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setup_test, container, false);

        LinearLayout backButton = view.findViewById(R.id.btn_back_training);
        MaterialButton startTrainingButton = view.findViewById(R.id.btn_start_training);

        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .commit();
        });

        startTrainingButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LiveTrainingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}