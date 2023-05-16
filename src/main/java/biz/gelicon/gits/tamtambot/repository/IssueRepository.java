package biz.gelicon.gits.tamtambot.repository;

import biz.gelicon.gits.tamtambot.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Integer> {
    Optional<Issue> findByIssueId(int id);

    @Query(value = "SELECT e2.ERROR_ID, MAX(e2.ERROR_TEXT) AS ERROR_TEXT, MAX(e2.WORKER_ID) AS WORKER_ID," +
            " MAX(e2.ERROR_STATUS) AS ERROR_STATUS, MAX(e2.ERROR_DATE) AS ERROR_DATE," +
            " MAX(e2.ERROR_DATENEED) AS ERROR_DATENEED, MAX(e2.ERROR_PRIORITY) AS ERROR_PRIORITY FROM ERRORTRANSIT" +
            " e INNER JOIN ERROR e2 ON e.ERROR_ID = e2.ERROR_ID INNER JOIN ERRORSTATUS e3 ON" +
            " e2.ERROR_ID = e3.ERROR_ID WHERE e.WORKER_ID = ?1 AND e2.ERROR_STATUS = 0 AND" +
            " e.ERRORTRANSIT_ID = e3.ERRORTRANSIT_ID GROUP BY ERROR_ID ORDER BY ERROR_DATE DESC",
            nativeQuery = true)
    List<Issue> findAllIssues(int workerId);
}
