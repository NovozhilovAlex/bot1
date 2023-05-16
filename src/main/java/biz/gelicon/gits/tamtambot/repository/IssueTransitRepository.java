package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueTransit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueTransitRepository extends JpaRepository<IssueTransit, Integer> {
    List<IssueTransit> findAllByIssue(Issue issue);
}
