package com.example.taskplanner.controller.api;

import com.example.taskplanner.model.HistoryAction;
import com.example.taskplanner.model.TaskHistory;
import com.example.taskplanner.service.TaskService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/history")
public class HistoryRestController {

    private final TaskService taskService;

    public HistoryRestController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskHistory> getHistory(@RequestParam(required = false) HistoryAction action) {
        return taskService.getHistory(action);
    }
}
