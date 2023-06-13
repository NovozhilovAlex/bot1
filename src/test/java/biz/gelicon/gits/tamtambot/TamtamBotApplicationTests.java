package biz.gelicon.gits.tamtambot;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.repository.IssueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class TamtamBotApplicationTests {
    @Autowired
    private IssueRepository issueRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void test1() {
        int id = 4;
        List<Issue> issueList = issueRepository.findAllIssues(id);
        System.out.println(issueList.size());
    }
}
