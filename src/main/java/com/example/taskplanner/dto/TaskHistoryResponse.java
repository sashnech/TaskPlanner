package com.example.taskplanner.dto;

import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.TaskHistory;

import java.time.LocalDateTime;

public record TaskHistoryResponse(
        Long id,
        Long taskId,
        HistoryAction action,
        String details,
        LocalDateTime createdAt
) {

    public static TaskHistoryResponse from(TaskHistory history) {
        return new TaskHistoryResponse(
                history.getId(),
                history.getTask().getId(),
                history.getAction(),
                history.getDetails(),
                history.getCreatedAt()
        );
    }
}
