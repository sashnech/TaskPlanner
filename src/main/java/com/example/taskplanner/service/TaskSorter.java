package com.example.taskplanner.service;

import com.example.taskplanner.model.Task;

import java.util.Comparator;
import java.util.List;

public class TaskSorter {

    public List<Task> sort(List<Task> tasks, String sortBy) {

        Comparator<Task> byDate = Comparator.comparing(
                Task::getDueDate,
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        if ("priority".equals(sortBy)) {
            tasks.sort(
                    Comparator.comparing(
                            Task::getPriority,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ).thenComparing(byDate)
            );
        } else {
            tasks.sort(byDate);
        }

        return tasks;
    }
}
