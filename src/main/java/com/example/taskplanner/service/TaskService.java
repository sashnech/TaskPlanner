package com.example.taskplanner.service;

import com.example.taskplanner.dto.PageResponse;
import com.example.taskplanner.dto.TaskFilter;
import com.example.taskplanner.dto.TaskRequest;
import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.Task;
import com.example.taskplanner.model.TaskHistory;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.UnaryOperator;


public interface TaskService {

    List<Task> getAllTasks(String sortBy);

    PageResponse<Task> findTasks(TaskFilter filter, String sortBy, int page, int size);

    Task getTask(Long id);

    void saveTask(Task task);

    Task createTask(TaskRequest request);

    Task updateTask(Long id, TaskRequest request);

    Task patchTask(Long id, UnaryOperator<JsonNode> patch);

    void deleteTask(Long id);

    void completeTask(Long id);


    List<Task> completeTasks(List<Long> ids);

    List<TaskHistory> getTaskHistory(Long taskId);

    List<TaskHistory> getHistory(HistoryAction action);
}
