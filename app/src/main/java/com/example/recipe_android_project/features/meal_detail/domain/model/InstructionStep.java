package com.example.recipe_android_project.features.meal_detail.domain.model;

public class InstructionStep {
    private int stepNumber;
    private String instruction;

    public InstructionStep(int stepNumber, String instruction) {
        this.stepNumber = stepNumber;
        this.instruction = instruction;
    }

    public int getStepNumber() { return stepNumber; }
    public String getInstruction() { return instruction; }
}
