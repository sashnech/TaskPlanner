package com.example.taskplanner.repository;

import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.TaskHistory;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskHistoryRepository extends CrudRepository<TaskHistory, Long> {

    List<TaskHistory> findByTaskIdOrderByCreatedAtAscIdAsc(Long taskId);

    List<TaskHistory> findByActionOrderByCreatedAtAscIdAsc(HistoryAction action);

    @Query("SELECT h FROM TaskHistory h JOIN FETCH h.task ORDER BY h.createdAt, h.id")
    List<TaskHistory> findAllWithTask();
}
