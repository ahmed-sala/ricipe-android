package com.example.recipe_android_project.core.utils;


import com.example.recipe_android_project.features.meal_detail.domain.model.InstructionStep;

import java.util.ArrayList;
import java.util.List;

public class InstructionParser {


    public static List<InstructionStep> parseInstructions(String rawInstructions) {
        List<InstructionStep> steps = new ArrayList<>();

        if (rawInstructions == null || rawInstructions.trim().isEmpty()) {
            return steps;
        }

        String[] parts = rawInstructions.split("\\r\\n|\\r|\\n");

        int stepNumber = 0;
        StringBuilder currentInstruction = new StringBuilder();

        for (String part : parts) {
            String trimmedPart = part.trim();

            if (trimmedPart.isEmpty()) {
                continue;
            }

            if (isStepNumber(trimmedPart)) {
                if (stepNumber > 0 && currentInstruction.length() > 0) {
                    steps.add(new InstructionStep(stepNumber, currentInstruction.toString().trim()));
                    currentInstruction = new StringBuilder();
                }
                stepNumber = Integer.parseInt(trimmedPart);
            } else {
                if (currentInstruction.length() > 0) {
                    currentInstruction.append(" ");
                }
                currentInstruction.append(trimmedPart);
            }
        }

        if (stepNumber > 0 && currentInstruction.length() > 0) {
            steps.add(new InstructionStep(stepNumber, currentInstruction.toString().trim()));
        }

        if (steps.isEmpty()) {
            steps = parseAlternativeFormat(rawInstructions);
        }

        return steps;
    }


    private static boolean isStepNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<InstructionStep> parseAlternativeFormat(String rawInstructions) {
        List<InstructionStep> steps = new ArrayList<>();

        String[] paragraphs = rawInstructions.split("\\r\\n\\r\\n|\\n\\n");

        if (paragraphs.length > 1) {
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i].trim();
                if (!paragraph.isEmpty()) {
                    paragraph = paragraph.replaceAll("\\r\\n|\\r|\\n", " ").trim();
                    steps.add(new InstructionStep(i + 1, paragraph));
                }
            }
        } else {
            String[] sentences = rawInstructions.split("(?<=\\.)\\s+(?=[A-Z])");

            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    sentence = sentence.replaceAll("\\r\\n|\\r|\\n", " ").trim();
                    if (!sentence.endsWith(".")) {
                        sentence += ".";
                    }
                    steps.add(new InstructionStep(i + 1, sentence));
                }
            }
        }

        return steps;
    }


    public static List<InstructionStep> parseStepFormat(String rawInstructions) {
        List<InstructionStep> steps = new ArrayList<>();

        if (rawInstructions == null || rawInstructions.trim().isEmpty()) {
            return steps;
        }

        String[] parts = rawInstructions.split("(?i)(?=STEP\\s*\\d+|(?<=\\n)\\d+\\.|(?<=\\n)\\d+\\))");

        int stepNumber = 1;
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;

            trimmed = trimmed.replaceAll("(?i)^STEP\\s*\\d+[:.\\s]*", "");
            trimmed = trimmed.replaceAll("^\\d+[.):]\\s*", "");
            trimmed = trimmed.replaceAll("\\r\\n|\\r|\\n", " ").trim();

            if (!trimmed.isEmpty()) {
                steps.add(new InstructionStep(stepNumber++, trimmed));
            }
        }

        return steps;
    }
}
