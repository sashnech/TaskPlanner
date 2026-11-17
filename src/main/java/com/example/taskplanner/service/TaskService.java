package com.example.taskplanner.service;

import com.example.taskplanner.dto.PageResponse;
import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.dto.TaskHistoryResponse;
import com.example.taskplanner.dto.TaskRequest;
import com.example.taskplanner.dto.TaskResponse;
import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.Task;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.List;
import java.util.function.UnaryOperator;

public interface TaskService {

    List<Task> getAllTasks(String sortBy);

    PageResponse<TaskResponse> findTasks(TaskFilter filter, String sortBy, int page, int size);

    List<TaskResponse> getOverdueTasks(LocalDate date);

    List<TaskResponse> getUpcomingTasks(int days);

    Task getTask(Long id);

    void saveTask(Task task);

    Task createTask(TaskRequest request);

    Task updateTask(Long id, TaskRequest request);

    Task patchTask(Long id, UnaryOperator<JsonNode> patch);

    void deleteTask(Long id);

    void completeTask(Long id);

    List<TaskResponse> completeTasks(List<Long> ids);

    List<TaskHistoryResponse> getTaskHistory(Long taskId);

    List<TaskHistoryResponse> getHistory(HistoryAction action);
}
