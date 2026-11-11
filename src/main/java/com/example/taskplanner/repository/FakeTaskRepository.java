package com.example.taskplanner.repository;

import com.example.taskplanner.model.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FakeTaskRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public synchronized List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public synchronized Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public synchronized void save(Task task) {
        if (task.getId() == null) {
            task.setId(nextId++);
        }

        tasks.put(task.getId(), task);
    }

    @Override
    public synchronized void deleteById(Long id) {
        tasks.remove(id);
    }
}
