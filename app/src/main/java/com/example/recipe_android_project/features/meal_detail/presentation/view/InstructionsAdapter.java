package com.example.recipe_android_project.features.meal_detail.presentation.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_android_project.R;
import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;

import java.util.ArrayList;
import java.util.List;

public class InstructionsAdapter extends RecyclerView.Adapter<InstructionsAdapter.ViewHolder> {

    private List<InstructionStep> allInstructions;
    private List<InstructionStep> displayedInstructions;
    private boolean isExpanded = false;
    private static final int INITIAL_COUNT = 3;

    public InstructionsAdapter(List<InstructionStep> instructions) {
        this.allInstructions = instructions != null ? instructions : new ArrayList<>();
        this.displayedInstructions = new ArrayList<>();
        updateDisplayedList();
    }

    private void updateDisplayedList() {
        displayedInstructions.clear();
        if (isExpanded || allInstructions.size() <= INITIAL_COUNT) {
            displayedInstructions.addAll(allInstructions);
        } else {
            displayedInstructions.addAll(allInstructions.subList(0, INITIAL_COUNT));
        }
    }

    public void updateData(List<InstructionStep> newInstructions) {
        this.allInstructions = newInstructions != null ? newInstructions : new ArrayList<>();
        this.isExpanded = false;
        updateDisplayedList();
        notifyDataSetChanged();
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
        updateDisplayedList();
        notifyDataSetChanged();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public boolean hasMore() {
        return allInstructions.size() > INITIAL_COUNT;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_instruction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InstructionStep step = displayedInstructions.get(position);
        holder.tvStepNumber.setText(String.valueOf(step.getStepNumber()));
        holder.tvInstruction.setText(step.getInstruction());
    }

    @Override
    public int getItemCount() {
        return displayedInstructions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber;
        TextView tvInstruction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);
        }
    }
}
