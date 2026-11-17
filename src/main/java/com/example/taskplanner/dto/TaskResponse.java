package com.example.taskplanner.dto;

import com.example.taskplanner.model.Priority;
import com.example.taskplanner.model.Task;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        LocalDate dueDate,
        Priority priority,
        boolean completed
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDueDate(),
                task.getPriority(),
                task.isCompleted()
        );
    }
}
