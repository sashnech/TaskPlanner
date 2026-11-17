package com.example.taskplanner.dto;

import com.example.taskplanner.model.Priority;
import com.example.taskplanner.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;


public record TaskRequest(

        @NotBlank(message = "Назва задачі є обов'язковою")
        @Size(max = 120, message = "Назва задачі не може перевищувати 120 символів")
        String title,

        @NotNull(message = "Дата виконання є обов'язковою")
        LocalDate dueDate,

        @NotNull(message = "Пріоритет є обов'язковим")
        Priority priority,

        Boolean completed
) {

    public static TaskRequest from(Task task) {
        return new TaskRequest(
                task.getTitle(),
                task.getDueDate(),
                task.getPriority(),
                task.isCompleted()
        );
    }

    public void applyTo(Task task) {
        task.setTitle(title.trim());
        task.setDueDate(dueDate);
        task.setPriority(priority);
        task.setCompleted(Boolean.TRUE.equals(completed));
    }
}
