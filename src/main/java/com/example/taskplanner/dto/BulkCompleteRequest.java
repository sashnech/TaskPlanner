package com.example.taskplanner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public record BulkCompleteRequest(

        @NotEmpty(message = "Список ідентифікаторів не може бути порожнім")
        @Size(max = 100, message = "За один запит можна завершити не більше 100 задач")
        List<@NotNull(message = "Ідентифікатор не може бути null") Long> ids
) {
}
