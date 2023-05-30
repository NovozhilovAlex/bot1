package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.Proguser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProguserRepository extends JpaRepository<Proguser, Integer> {
    Optional<Proguser> findByProguserName(String name);
}
