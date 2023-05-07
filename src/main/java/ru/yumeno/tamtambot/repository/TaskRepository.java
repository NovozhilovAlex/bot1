package ru.yumeno.tamtambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yumeno.tamtambot.entity.Task;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    Optional<Task> findByTaskId(int id);
    List<Task> findAllByWorkerIdAndErrorStatus(int id, int errorStatus);
}
