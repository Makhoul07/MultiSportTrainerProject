package com.example.multisporttrainer;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class LiveTrainingFragment extends Fragment {

    public LiveTrainingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_live_training, container, false);

        MaterialButton finishButton = view.findViewById(R.id.btn_finish_training);

        finishButton.setOnClickListener(v -> {
            prepareTemporaryResultData();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ResultsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void prepareTemporaryResultData() {
        int routeLength = CurrentTrainingData.coneSequence.size();

        if (routeLength == 0) {
            routeLength = 4;
        }

        CurrentTrainingData.durationSeconds = routeLength * 25;
        CurrentTrainingData.mistakes = 1;

        CurrentTrainingData.accuracy = 90.0;
        CurrentTrainingData.averageReactionSeconds = 1.3;
        CurrentTrainingData.bestReactionSeconds = 0.8;

        CurrentTrainingData.score = 900 + (routeLength * 5) - (CurrentTrainingData.mistakes * 10);

        Toast.makeText(getContext(), "Training completed", Toast.LENGTH_SHORT).show();
    }
}