package com.example.taskplanner.model;

public enum Priority {
    LOW("Низький"),
    MEDIUM("Середній"),
    HIGH("Високий");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
