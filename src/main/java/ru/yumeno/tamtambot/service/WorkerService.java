package ru.yumeno.tamtambot.service;

import ru.yumeno.tamtambot.entity.Task;

import java.util.List;

public interface WorkerService {
    List<Task> getTasksByWorkerEmail(String email);
}
