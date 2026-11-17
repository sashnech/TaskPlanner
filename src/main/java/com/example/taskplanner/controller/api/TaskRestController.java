package com.example.taskplanner.controller.api;

import com.example.taskplanner.dto.BulkCompleteRequest;
import com.example.taskplanner.dto.PageResponse;
import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.dto.TaskHistoryResponse;
import com.example.taskplanner.dto.TaskRequest;
import com.example.taskplanner.dto.TaskResponse;
import com.example.taskplanner.model.Priority;
import com.example.taskplanner.patch.JsonMergePatch;
import com.example.taskplanner.patch.JsonPatch;
import com.example.taskplanner.patch.JsonPatchOperation;
import com.example.taskplanner.service.TaskService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskRestController {

    public static final String JSON_PATCH = "application/json-patch+json";
    public static final String MERGE_PATCH = "application/merge-patch+json";

    private final TaskService taskService;

    public TaskRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public PageResponse<TaskResponse> listTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,

            @RequestParam(defaultValue = "date")
            @Pattern(regexp = "date|priority", message = "Допустимі значення: date, priority") String sort,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Номер сторінки не може бути від'ємним") int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Розмір сторінки має бути не менше 1")
            @Max(value = 100, message = "Розмір сторінки має бути не більше 100") int size) {

        TaskFilter filter = new TaskFilter(title, priority, completed, dueFrom, dueTo);
        return taskService.findTasks(filter, sort, page, size);
    }

    @GetMapping("/overdue")
    public List<TaskResponse> getOverdueTasks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return taskService.getOverdueTasks(date);
    }

    @GetMapping("/upcoming")
    public List<TaskResponse> getUpcomingTasks(
            @RequestParam(defaultValue = "7")
            @Min(value = 0, message = "Кількість днів не може бути від'ємною")
            @Max(value = 365, message = "Кількість днів має бути не більше 365") int days) {

        return taskService.getUpcomingTasks(days);
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable Long id) {
        return TaskResponse.from(taskService.getTask(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = TaskResponse.from(taskService.createTask(request));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TaskResponse replaceTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return TaskResponse.from(taskService.updateTask(id, request));
    }

    @PatchMapping(path = "/{id}", consumes = JSON_PATCH)
    public TaskResponse jsonPatchTask(@PathVariable Long id, @RequestBody List<JsonPatchOperation> patch) {
        return TaskResponse.from(taskService.patchTask(id, current -> JsonPatch.apply(patch, current)));
    }

    @PatchMapping(path = "/{id}", consumes = MERGE_PATCH)
    public TaskResponse mergePatchTask(@PathVariable Long id, @RequestBody JsonNode patch) {
        return TaskResponse.from(taskService.patchTask(id, current -> JsonMergePatch.apply(patch, current)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/complete")
    public List<TaskResponse> completeTasks(@Valid @RequestBody BulkCompleteRequest request) {
        return taskService.completeTasks(request.ids());
    }

    @GetMapping("/{id}/history")
    public List<TaskHistoryResponse> getTaskHistory(@PathVariable Long id) {
        return taskService.getTaskHistory(id);
    }
}
