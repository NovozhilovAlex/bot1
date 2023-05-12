package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
    Optional<Issue> findByIssueId(int id);

    List<Issue> findAllByWorkerIdAndIssueStatusOrderByIssueDateDesc(int id, int errorStatus);
}
