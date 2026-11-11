
package com.example.taskplanner.service;

import com.example.taskplanner.model.Task;
import com.example.taskplanner.repository.TaskRepository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final ObjectProvider<TaskOperation> operations;

    private TaskSorter taskSorter;

    public TaskService(
            TaskRepository repository,
            ObjectProvider<TaskOperation> operations) {

        this.repository = repository;
        this.operations = operations;
    }

    @Autowired
    public void setTaskSorter(TaskSorter taskSorter) {
        this.taskSorter = taskSorter;
    }

    public List<Task> getAllTasks(String sortBy) {
        return taskSorter.sort(repository.findAll(), sortBy);
    }

    public Task getTask(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Задачу не знайдено"
                ));
    }

    public void saveTask(Task task) {
        boolean isNew = task.getId() == null;

        repository.save(task);

        String message = isNew
                ? "Створено задачу №" + task.getId()
                : "Оновлено задачу №" + task.getId();

        operations.getObject().log(message);
    }

    public void deleteTask(Long id) {
        repository.deleteById(id);
        operations.getObject().log("Видалено задачу №" + id);
    }

    public void completeTask(Long id) {
        Task task = getTask(id);
        task.setCompleted(true);

        repository.save(task);
        operations.getObject().log("Виконано задачу №" + id);
    }
}
