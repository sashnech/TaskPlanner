package com.example.taskplanner.dto;

import com.example.taskplanner.model.Priority;
import com.example.taskplanner.model.Task;

import java.time.LocalDate;
import java.util.Locale;


public record TaskFilter(
        String title,
        Priority priority,
        Boolean completed,
        LocalDate dueFrom,
        LocalDate dueTo
) {

    public boolean matches(Task task) {
        if (title != null && !title.isBlank()) {
            String taskTitle = task.getTitle() == null ? "" : task.getTitle();
            if (!taskTitle.toLowerCase(Locale.ROOT)
                    .contains(title.trim().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }

        if (priority != null && priority != task.getPriority()) {
            return false;
        }

        if (completed != null && completed != task.isCompleted()) {
            return false;
        }

        LocalDate due = task.getDueDate();

        if (dueFrom != null && (due == null || due.isBefore(dueFrom))) {
            return false;
        }

        return dueTo == null || (due != null && !due.isAfter(dueTo));
    }
}
