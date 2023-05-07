package ru.yumeno.tamtambot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yumeno.tamtambot.entity.Task;
import ru.yumeno.tamtambot.entity.Worker;
import ru.yumeno.tamtambot.exceptions.ResourceNotFoundException;
import ru.yumeno.tamtambot.repository.TaskRepository;
import ru.yumeno.tamtambot.repository.WorkerRepository;
import ru.yumeno.tamtambot.service.WorkerService;

import java.util.List;
import java.util.Optional;

@Service
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public WorkerServiceImpl(WorkerRepository workerRepository, TaskRepository taskRepository) {
        this.workerRepository = workerRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getTasksByWorkerEmail(String email) {
        Optional<Worker> optional = workerRepository.findByWorkerEmail(email);
        if (optional.isPresent()) {
            Worker worker = optional.get();
            List<Task> tasks = taskRepository.findAllByWorkerIdAndErrorStatus(worker.getWorkerId(), 0);
            if (!tasks.isEmpty()) {
                return tasks;
            } else {
                throw new ResourceNotFoundException("No tasks with worker id = : " + worker.getWorkerId());
            }
        } else {
            throw new ResourceNotFoundException("Worker not exist with email: " + email);
        }
    }
}
