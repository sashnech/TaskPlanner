package com.example.taskplanner.repository;

import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.model.Priority;
import com.example.taskplanner.model.Task;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Repository
public class JdbcTemplateTaskRepository implements TaskRepository {

    private static final String COLUMNS = "id, title, due_date, priority, completed";

    private static final RowMapper<Task> TASK_MAPPER = (rs, rowNum) -> {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDueDate(rs.getObject("due_date", LocalDate.class));
        task.setPriority(Priority.valueOf(rs.getString("priority")));
        task.setCompleted(rs.getBoolean("completed"));
        return task;
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long create(Task task) {
        String sql = "INSERT INTO tasks (title, due_date, priority, completed) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, task.getTitle());
            ps.setObject(2, task.getDueDate());
            ps.setString(3, task.getPriority().name());
            ps.setBoolean(4, task.isCompleted());
            return ps;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey(), "БД не повернула ключ").longValue();
        task.setId(id);
        return id;
    }

    @Override
    public Optional<Task> findById(Long id) {
        List<Task> result = jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM tasks WHERE id = ?", TASK_MAPPER, id);

        return result.stream().findFirst();
    }

    @Override
    public List<Task> findAll() {
        return jdbcTemplate.query("SELECT " + COLUMNS + " FROM tasks ORDER BY id", TASK_MAPPER);
    }

    @Override
    public boolean update(Task task) {
        int rows = jdbcTemplate.update(
                "UPDATE tasks SET title = ?, due_date = ?, priority = ?, completed = ? WHERE id = ?",
                task.getTitle(),
                task.getDueDate(),
                task.getPriority().name(),
                task.isCompleted(),
                task.getId());

        return rows > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM tasks WHERE id = ?", id) > 0;
    }

    @Override
    public List<Task> findByFilter(TaskFilter filter, String sort, int limit, long offset) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(filter, params);

        String orderBy = "priority".equals(sort)
                ? "priority DESC, due_date, id"
                : "due_date, id";

        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(
                "SELECT " + COLUMNS + " FROM tasks" + where + " ORDER BY " + orderBy + " LIMIT ? OFFSET ?",
                TASK_MAPPER,
                params.toArray());
    }

    @Override
    public long countByFilter(TaskFilter filter) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(filter, params);

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tasks" + where, Long.class, params.toArray());

        return count == null ? 0 : count;
    }


    private static String buildWhere(TaskFilter filter, List<Object> params) {
        List<String> conditions = new ArrayList<>();

        if (filter.title() != null && !filter.title().isBlank()) {
            conditions.add("title LIKE ?");
            params.add("%" + escapeLike(filter.title().trim()) + "%");
        }

        if (filter.priority() != null) {
            conditions.add("priority = ?");
            params.add(filter.priority().name());
        }

        if (filter.completed() != null) {
            conditions.add("completed = ?");
            params.add(filter.completed());
        }

        if (filter.dueFrom() != null) {
            conditions.add("due_date >= ?");
            params.add(filter.dueFrom());
        }

        if (filter.dueTo() != null) {
            conditions.add("due_date <= ?");
            params.add(filter.dueTo());
        }

        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
