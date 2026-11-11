package com.example.taskplanner.service;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskOperation {

    private final String id = UUID.randomUUID()
            .toString()
            .substring(0, 8);

    public void log(String message) {
        System.out.println("Операція [" + id + "]: " + message);
    }
}
