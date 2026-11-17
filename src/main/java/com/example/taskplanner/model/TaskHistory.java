package com.example.taskplanner.model;

import java.time.LocalDateTime;


public record TaskHistory(
        Long id,
        Long taskId,
        HistoryAction action,
        String details,
        LocalDateTime createdAt
) {

    public static TaskHistory of(Long taskId, HistoryAction action, String details) {
        return new TaskHistory(null, taskId, action, details, null);
    }
}
