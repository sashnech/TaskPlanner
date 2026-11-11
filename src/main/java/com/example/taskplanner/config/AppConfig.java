package com.example.taskplanner.config;

import com.example.taskplanner.service.TaskSorter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {

    @Bean
    @Scope("singleton")
    public TaskSorter taskSorter() {
        return new TaskSorter();
    }
}
