package com.example.taskplanner.repository;

import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.TaskHistory;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Repository
public class JdbcClientTaskHistoryRepository implements TaskHistoryRepository {

    private static final String SELECT =
            "SELECT id, task_id, action, details, created_at FROM task_history";

    private final JdbcClient jdbcClient;

    public JdbcClientTaskHistoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Long create(TaskHistory history) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql("""
                        INSERT INTO task_history (task_id, action, details)
                        VALUES (:taskId, :action, :details)""")
                .param("taskId", history.taskId())
                .param("action", history.action().name())
                .param("details", history.details())
                .update(keyHolder);

        return Objects.requireNonNull(keyHolder.getKey(), "БД не повернула ключ").longValue();
    }

    @Override
    public Optional<TaskHistory> findById(Long id) {
        return jdbcClient.sql(SELECT + " WHERE id = :id")
                .param("id", id)
                .query(TaskHistory.class)
                .optional();
    }

    @Override
    public List<TaskHistory> findAll() {
        return jdbcClient.sql(SELECT + " ORDER BY id")
                .query(TaskHistory.class)
                .list();
    }

    @Override
    public boolean update(TaskHistory history) {
        int rows = jdbcClient.sql("UPDATE task_history SET details = :details WHERE id = :id")
                .param("details", history.details())
                .param("id", history.id())
                .update();

        return rows > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return jdbcClient.sql("DELETE FROM task_history WHERE id = :id")
                .param("id", id)
                .update() > 0;
    }

    @Override
    public List<TaskHistory> findByTaskId(Long taskId) {
        return jdbcClient.sql(SELECT + " WHERE task_id = :taskId ORDER BY created_at, id")
                .param("taskId", taskId)
                .query(TaskHistory.class)
                .list();
    }

    @Override
    public List<TaskHistory> findByAction(HistoryAction action) {
        return jdbcClient.sql(SELECT + " WHERE action = :action ORDER BY created_at, id")
                .param("action", action.name())
                .query(TaskHistory.class)
                .list();
    }
}
