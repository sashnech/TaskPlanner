package com.example.taskplanner.repository;

import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.model.Task;

import java.util.List;
import java.util.Optional;


public interface TaskRepository {

    Long create(Task task);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    boolean update(Task task);

    boolean deleteById(Long id);


    List<Task> findByFilter(TaskFilter filter, String sort, int limit, long offset);

    long countByFilter(TaskFilter filter);
}
