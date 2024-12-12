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
    private String selectedGoal;

    public SavingGoalsAdapter(List<SavingGoal> savingGoals, String selectedGoal) {
        this.savingGoals = savingGoals;
        this.selectedGoal = selectedGoal;
        sortGoals();
    }

    private void sortGoals() {
        if (selectedGoal != null && !selectedGoal.isEmpty()) {
            // Перемещаем выбранную цель в начало списка
            for (int i = 0; i < savingGoals.size(); i++) {
                if (savingGoals.get(i).getGoal().equals(selectedGoal)) {
                    SavingGoal selected = savingGoals.get(i);
                    savingGoals.remove(i);
                    savingGoals.add(0, selected);  // Перемещаем в начало списка
                    break;
                }
            }
        }
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

        String targetAmount = String.format("Цель накопить: %d", (int) goal.getTargetAmount());
        holder.targetAmountTextView.setText(targetAmount);

        String currentAmount = String.format("Накоплено: %d", (int) goal.getCurrentAmount());
        holder.currentAmountTextView.setText(currentAmount);
    }


    @Override
    public int getItemCount() {
        return savingGoals.size();
    }

    public void updateGoals(List<SavingGoal> newGoals) {
        savingGoals = newGoals;
        sortGoals();
        notifyDataSetChanged();
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


