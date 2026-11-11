
package com.example.taskplanner.controller;

import com.example.taskplanner.model.Priority;
import com.example.taskplanner.model.Task;
import com.example.taskplanner.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    public String showTasks(
            @RequestParam(defaultValue = "date") String sort,
            Model model) {

        model.addAttribute("tasks", taskService.getAllTasks(sort));
        model.addAttribute("sort", sort);

        return "tasks";
    }

    @GetMapping("/tasks/new")
    public String newTask(Model model) {
        Task task = new Task();
        task.setDueDate(LocalDate.now());
        task.setPriority(Priority.MEDIUM);

        model.addAttribute("task", task);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("pageTitle", "Створення задачі");

        return "task-form";
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTask(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.getTask(id));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("pageTitle", "Редагування задачі");

        return "task-form";
    }

    @PostMapping("/tasks/save")
    public String saveTask(@ModelAttribute Task task) {
        taskService.saveTask(task);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/complete/{id}")
    public String completeTask(@PathVariable Long id) {
        taskService.completeTask(id);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }
}
