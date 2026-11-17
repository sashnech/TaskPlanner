package com.example.taskplanner.service;

import com.example.taskplanner.dto.PageResponse;
import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.dto.TaskRequest;
import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.Task;
import com.example.taskplanner.model.TaskHistory;
import com.example.taskplanner.patch.PatchException;
import com.example.taskplanner.repository.TaskHistoryRepository;
import com.example.taskplanner.repository.TaskRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;
    private final ObjectProvider<TaskOperation> operations;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    private TaskSorter taskSorter;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            TaskHistoryRepository historyRepository,
            ObjectProvider<TaskOperation> operations,
            JsonMapper jsonMapper,
            Validator validator) {

        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
        this.operations = operations;
        this.jsonMapper = jsonMapper;
        this.validator = validator;
    }

    @Autowired
    public void setTaskSorter(TaskSorter taskSorter) {
        this.taskSorter = taskSorter;
    }


    @Override
    public List<Task> getAllTasks(String sortBy) {
        return taskSorter.sort(taskRepository.findAll(), sortBy);
    }

    @Override
    public PageResponse<Task> findTasks(TaskFilter filter, String sortBy, int page, int size) {
        if (filter.dueFrom() != null && filter.dueTo() != null
                && filter.dueFrom().isAfter(filter.dueTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Параметр dueFrom не може бути пізніше за dueTo");
        }

        long total = taskRepository.countByFilter(filter);
        List<Task> content = taskRepository.findByFilter(filter, sortBy, size, (long) page * size);

        return PageResponse.of(content, page, size, total);
    }

    @Override
    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Override
    public List<TaskHistory> getTaskHistory(Long taskId) {
        getTask(taskId);
        return historyRepository.findByTaskId(taskId);
    }

    @Override
    public List<TaskHistory> getHistory(HistoryAction action) {
        return action == null
                ? historyRepository.findAll()
                : historyRepository.findByAction(action);
    }


    @Override
    @Transactional
    public void saveTask(Task task) {
        if (task.getId() == null) {
            insert(task);
        } else {
            update(task);
        }
    }

    @Override
    @Transactional
    public Task createTask(TaskRequest request) {
        Task task = new Task();
        request.applyTo(task);
        insert(task);
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(Long id, TaskRequest request) {
        Task task = getTask(id);
        request.applyTo(task);
        update(task);
        return task;
    }

    @Override
    @Transactional
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
        update(task);
        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.deleteById(id)) {
            throw notFound(id);
        }

        operations.getObject().log("Видалено задачу №" + id);
    }

    @Override
    @Transactional
    public void completeTask(Long id) {
        Task task = getTask(id);

        if (!task.isCompleted()) {
            markCompleted(task);
        }
    }

    @Override
    @Transactional
    public List<Task> completeTasks(List<Long> ids) {
        List<Task> completed = new ArrayList<>();

        for (Long id : new LinkedHashSet<>(ids)) {
            Task task = getTask(id);

            if (task.isCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Задача №" + id + " вже виконана");
            }

            markCompleted(task);
            completed.add(task);
        }

        operations.getObject().log("Масово виконано задачі " + ids);
        return completed;
    }


    private void insert(Task task) {
        Long id = taskRepository.create(task);
        historyRepository.create(TaskHistory.of(id, HistoryAction.CREATED,
                "Створено задачу: " + task.getTitle()));

        operations.getObject().log("Створено задачу №" + id);
    }

    private void update(Task task) {
        if (!taskRepository.update(task)) {
            throw notFound(task.getId());
        }

        historyRepository.create(TaskHistory.of(task.getId(), HistoryAction.UPDATED,
                "Оновлено: " + task.getTitle() + ", " + task.getDueDate()
                        + ", " + task.getPriority() + ", виконана: " + task.isCompleted()));

        operations.getObject().log("Оновлено задачу №" + task.getId());
    }

    private void markCompleted(Task task) {
        task.setCompleted(true);
        taskRepository.update(task);
        historyRepository.create(TaskHistory.of(task.getId(), HistoryAction.COMPLETED,
                "Задачу позначено як виконану"));

        operations.getObject().log("Виконано задачу №" + task.getId());
    }

    private static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Задачу №" + id + " не знайдено");
    }
}
