package com.example.taskplanner.repository;

import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.TaskHistory;

import java.util.List;
import java.util.Optional;


public interface TaskHistoryRepository {

    Long create(TaskHistory history);

    Optional<TaskHistory> findById(Long id);

    List<TaskHistory> findAll();

    boolean update(TaskHistory history);

    boolean deleteById(Long id);

    List<TaskHistory> findByTaskId(Long taskId);

    List<TaskHistory> findByAction(HistoryAction action);
}
