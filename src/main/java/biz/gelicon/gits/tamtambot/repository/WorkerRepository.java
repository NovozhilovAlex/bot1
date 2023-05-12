package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkerRepository extends JpaRepository<Worker, Integer> {
    Optional<Worker> findByWorkerEmail(String email);
}
