package com.example.taskplanner.repository;

import com.example.taskplanner.model.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    List<Task> findAll();

    Optional<Task> findById(Long id);

    void save(Task task);

    void deleteById(Long id);
}
