package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.ProguserWorker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProguserWorkerRepository extends JpaRepository<ProguserWorker, Integer> {
    Optional<ProguserWorker> findByProguserId(int proguserId);
}
