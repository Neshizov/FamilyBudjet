package com.example.familybudget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SavingGoalsAdapter extends RecyclerView.Adapter<SavingGoalsAdapter.ViewHolder> {

    private List<SavingGoal> savingGoals;

    public SavingGoalsAdapter(List<SavingGoal> savingGoals) {
        this.savingGoals = savingGoals;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saving_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingGoal goal = savingGoals.get(position);
        holder.goalTextView.setText(goal.getGoal());
        holder.targetAmountTextView.setText("Цель: " + goal.getTargetAmount());
        holder.currentAmountTextView.setText("Текущее: " + goal.getCurrentAmount());
    }

    @Override
    public int getItemCount() {
        return savingGoals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView goalTextView;
        public TextView targetAmountTextView;
        public TextView currentAmountTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            goalTextView = itemView.findViewById(R.id.textViewGoal);
            targetAmountTextView = itemView.findViewById(R.id.textViewTargetAmount);
            currentAmountTextView = itemView.findViewById(R.id.textViewCurrentAmount);
        }
    }
}

