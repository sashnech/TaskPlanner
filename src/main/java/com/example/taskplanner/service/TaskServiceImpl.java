package com.example.taskplanner.service;

import com.example.taskplanner.dto.PageResponse;
import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.dto.TaskHistoryResponse;
import com.example.taskplanner.dto.TaskRequest;
import com.example.taskplanner.dto.TaskResponse;
import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.Task;
import com.example.taskplanner.patch.PatchException;
import com.example.taskplanner.repository.TaskHistoryRepository;
import com.example.taskplanner.repository.TaskRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private static final Sort SORT_BY_DATE = Sort.by(
            Sort.Order.asc("dueDate"), Sort.Order.asc("id"));

    private static final Sort SORT_BY_PRIORITY = Sort.by(
            Sort.Order.desc("priority"), Sort.Order.asc("dueDate"), Sort.Order.asc("id"));

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
        return taskSorter.sort(new ArrayList<>(taskRepository.findAllByOrderByIdAsc()), sortBy);
    }

    @Override
    public PageResponse<TaskResponse> findTasks(TaskFilter filter, String sortBy, int page, int size) {
        if (filter.dueFrom() != null && filter.dueTo() != null
                && filter.dueFrom().isAfter(filter.dueTo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Параметр dueFrom не може бути пізніше за dueTo");
        }

        Sort sort = "priority".equals(sortBy) ? SORT_BY_PRIORITY : SORT_BY_DATE;
        String title = filter.title() == null || filter.title().isBlank() ? null : filter.title().trim();

        Page<Task> result = taskRepository.search(
                title,
                filter.priority(),
                filter.completed(),
                filter.dueFrom(),
                filter.dueTo(),
                PageRequest.of(page, size, sort));

        return PageResponse.from(result, TaskResponse::from);
    }

    @Override
    public List<TaskResponse> getOverdueTasks(LocalDate date) {
        LocalDate reference = date == null ? LocalDate.now() : date;

        return taskRepository.findOverdue(reference).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public List<TaskResponse> getUpcomingTasks(int days) {
        LocalDate today = LocalDate.now();

        return taskRepository.findByCompletedFalseAndDueDateBetweenOrderByDueDateAscIdAsc(
                        today, today.plusDays(days)).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Override
    public List<TaskHistoryResponse> getTaskHistory(Long taskId) {
        getTask(taskId);

        return historyRepository.findByTaskIdOrderByCreatedAtAscIdAsc(taskId).stream()
                .map(TaskHistoryResponse::from)
                .toList();
    }

    @Override
    public List<TaskHistoryResponse> getHistory(HistoryAction action) {
        var history = action == null
                ? historyRepository.findAllWithTask()
                : historyRepository.findByActionOrderByCreatedAtAscIdAsc(action);

        return history.stream()
                .map(TaskHistoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void saveTask(Task form) {
        if (form.getId() == null) {
            Task task = new Task();
            copyFields(form, task);
            insert(task);
        } else {
            Task task = getTask(form.getId());
            copyFields(form, task);
            registerUpdate(task);
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
        registerUpdate(task);
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
        registerUpdate(task);
        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.delete(getTask(id));
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
    public List<TaskResponse> completeTasks(List<Long> ids) {
        List<TaskResponse> completed = new ArrayList<>();

        for (Long id : new LinkedHashSet<>(ids)) {
            Task task = getTask(id);

            if (task.isCompleted()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Задача №" + id + " вже виконана");
            }

            markCompleted(task);
            completed.add(TaskResponse.from(task));
        }

        operations.getObject().log("Масово виконано задачі " + ids);
        return completed;
    }

    private void insert(Task task) {
        task.addHistory(HistoryAction.CREATED, "Створено задачу: " + task.getTitle());
        taskRepository.save(task);
        operations.getObject().log("Створено задачу №" + task.getId());
    }

    private void registerUpdate(Task task) {
        task.addHistory(HistoryAction.UPDATED,
                "Оновлено: " + task.getTitle() + ", " + task.getDueDate()
                        + ", " + task.getPriority() + ", виконана: " + task.isCompleted());

        operations.getObject().log("Оновлено задачу №" + task.getId());
    }

    private void markCompleted(Task task) {
        task.setCompleted(true);
        task.addHistory(HistoryAction.COMPLETED, "Задачу позначено як виконану");
        operations.getObject().log("Виконано задачу №" + task.getId());
    }

    private static void copyFields(Task source, Task target) {
        target.setTitle(source.getTitle() == null ? null : source.getTitle().trim());
        target.setDueDate(source.getDueDate());
        target.setPriority(source.getPriority());
        target.setCompleted(source.isCompleted());
    }

    private static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Задачу №" + id + " не знайдено");
    }
}
