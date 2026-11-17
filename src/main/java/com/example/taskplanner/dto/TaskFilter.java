package com.example.taskplanner.dto;

import com.example.taskplanner.model.Priority;

import java.time.LocalDate;


public record TaskFilter(
        String title,
        Priority priority,
        Boolean completed,
        LocalDate dueFrom,
        LocalDate dueTo
) {
}
