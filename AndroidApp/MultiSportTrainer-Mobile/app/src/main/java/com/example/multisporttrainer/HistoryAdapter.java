package com.example.multisporttrainer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.multisporttrainer.models.HistoryResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

/**
 * Lists every training session for the logged-in user. Each row shows the
 * session summary and a Retry button that hands the item back to the fragment
 * (to re-run a session with the same settings).
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnRetryClickListener {
        void onRetry(HistoryResponse item);
    }

    private final List<HistoryResponse> items;
    private final OnRetryClickListener retryListener;

    public HistoryAdapter(List<HistoryResponse> items, OnRetryClickListener retryListener) {
        this.items = items;
        this.retryListener = retryListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(items.get(position), retryListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView trainingType;
        private final TextView date;
        private final TextView difficulty;
        private final TextView score;
        private final TextView accuracy;
        private final TextView duration;
        private final MaterialButton retryButton;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            trainingType = itemView.findViewById(R.id.item_training_type);
            date = itemView.findViewById(R.id.item_date);
            difficulty = itemView.findViewById(R.id.item_difficulty);
            score = itemView.findViewById(R.id.item_score);
            accuracy = itemView.findViewById(R.id.item_accuracy);
            duration = itemView.findViewById(R.id.item_duration);
            retryButton = itemView.findViewById(R.id.item_retry_button);
        }

        void bind(HistoryResponse item, OnRetryClickListener retryListener) {
            trainingType.setText(item.getTrainingType());
            date.setText("▣ " + formatDate(item.getCreatedAt()));
            difficulty.setText(safeUpper(item.getDifficulty()));
            score.setText(String.valueOf(item.getScore()));
            accuracy.setText(String.format(Locale.US, "%.0f%%", item.getAccuracy()));
            duration.setText(formatDuration(item.getDurationSeconds()));

            retryButton.setOnClickListener(v -> {
                if (retryListener != null) {
                    retryListener.onRetry(item);
                }
            });
        }

        private String safeUpper(String value) {
            return value == null ? "" : value.toUpperCase(Locale.US);
        }

        private String formatDate(String createdAt) {
            if (createdAt == null || createdAt.length() < 10) {
                return "Unknown date";
            }
            return createdAt.substring(0, 10);
        }

        private String formatDuration(int seconds) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds);
        }
    }
}
