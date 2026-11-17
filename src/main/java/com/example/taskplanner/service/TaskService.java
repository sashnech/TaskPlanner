
package com.example.taskplanner.service;

import com.example.taskplanner.dto.PageResponse;
import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.dto.TaskRequest;
import com.example.taskplanner.model.Task;
import com.example.taskplanner.patch.PatchException;
import com.example.taskplanner.repository.TaskRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository repository;
    private final ObjectProvider<TaskOperation> operations;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    private TaskSorter taskSorter;

    public TaskService(
            TaskRepository repository,
            ObjectProvider<TaskOperation> operations,
            JsonMapper jsonMapper,
            Validator validator) {

        this.repository = repository;
        this.operations = operations;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
    }

    @Autowired
    public void setTaskSorter(TaskSorter taskSorter) {
        this.taskSorter = taskSorter;
    }

    public List<Task> getAllTasks(String sortBy) {
        return taskSorter.sort(repository.findAll(), sortBy);
    }

    public PageResponse<Task> findTasks(TaskFilter filter, String sortBy, int page, int size) {
        if (filter.dueFrom() != null && filter.dueTo() != null
                && filter.dueFrom().isAfter(filter.dueTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Параметр dueFrom не може бути пізніше за dueTo");
        }

        List<Task> filtered = repository.findAll().stream()
                .filter(filter::matches)
                .collect(Collectors.toList());

        return PageResponse.of(taskSorter.sort(filtered, sortBy), page, size);
    }

    public Task getTask(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Задачу №" + id + " не знайдено"
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

    public Task createTask(TaskRequest request) {
        Task task = new Task();
        request.applyTo(task);
        saveTask(task);
        return task;
    }

    public Task updateTask(Long id, TaskRequest request) {
        Task task = getTask(id);
        request.applyTo(task);
        saveTask(task);
        return task;
    }


    public Task patchTask(Long id, UnaryOperator<JsonNode> patch) {
        Task task = getTask(id);

        JsonNode current = jsonMapper.valueToTree(TaskRequest.from(task));
        JsonNode patched = patch.apply(current);

        TaskRequest request;

        try {
            request = jsonMapper.readerFor(TaskRequest.class)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(patched);
        } catch (JacksonException e) {
            throw PatchException.unprocessable(
                    "Результат патча не відповідає моделі задачі: " + e.getOriginalMessage());
        }

        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String details = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));

            throw PatchException.unprocessable("Результат патча невалідний: " + details);
        }

        request.applyTo(task);
        saveTask(task);
        return task;
    }

    public void deleteTask(Long id) {
        getTask(id);
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
