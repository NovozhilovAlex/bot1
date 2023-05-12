package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueTransit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueTransitRepository extends JpaRepository<IssueTransit, Integer> {
    List<IssueTransit> findAllByIssue(Issue issue);
}
