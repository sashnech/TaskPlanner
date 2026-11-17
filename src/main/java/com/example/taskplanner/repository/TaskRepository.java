package com.example.taskplanner.repository;

import com.example.taskplanner.model.Priority;
import com.example.taskplanner.model.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Long> {

    @Query("""
            SELECT t FROM Task t
            WHERE (:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:dueFrom IS NULL OR t.dueDate >= :dueFrom)
              AND (:dueTo IS NULL OR t.dueDate <= :dueTo)
            """)
    Page<Task> search(
            @Param("title") String title,
            @Param("priority") Priority priority,
            @Param("completed") Boolean completed,
            @Param("dueFrom") LocalDate dueFrom,
            @Param("dueTo") LocalDate dueTo,
            Pageable pageable);

    List<Task> findOverdue(@Param("date") LocalDate date);

    List<Task> findByCompletedFalseAndDueDateBetweenOrderByDueDateAscIdAsc(LocalDate from, LocalDate to);

    List<Task> findAllByOrderByIdAsc();
}
