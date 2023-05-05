package ru.yumeno.tamtambot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yumeno.tamtambot.entity.Task;
import ru.yumeno.tamtambot.exceptions.ResourceNotFoundException;
import ru.yumeno.tamtambot.repository.TaskRepository;
import ru.yumeno.tamtambot.service.TaskService;

import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task getTaskById(int id) {
        Optional<Task> optional = taskRepository.findByTaskId(id);
        return optional.orElseThrow(() -> new ResourceNotFoundException("Task not exist with id: " + id));
    }
}
