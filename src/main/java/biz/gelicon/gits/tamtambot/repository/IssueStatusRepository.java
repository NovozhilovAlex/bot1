package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssueStatusRepository extends JpaRepository<IssueStatus, Integer> {
    Optional<IssueStatus> findByIssueId(int id);
}
