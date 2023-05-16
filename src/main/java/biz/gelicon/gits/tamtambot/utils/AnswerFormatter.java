package biz.gelicon.gits.tamtambot.utils;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import biz.gelicon.gits.tamtambot.entity.IssueTransit;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;

@Component
public class AnswerFormatter {
    private final String SEPARATOR = "----------------------------------------------------------------" +
            "------------------------------------------\n";

    public String getAnswerForShowCommand(Issue issue) {
        String answer = issue.getIssueText() + "\n";
        List<IssueTransit> issueTransits = issue.getIssueTransits();
        for (IssueTransit transit : issueTransits) {
            String text = "";
            if (transit.getIssueTransitText() != null) {
                Charset w1251 = Charset.forName("Windows-1251");
                text = new String(transit.getIssueTransitText(), w1251);
            }
            answer += SEPARATOR +
                    transit.getIssueTransitType().getIssueTransitTypeName().trim() + ", От кого: " +
                    transit.getFromWorker().getWorkerFamily().trim() + ", Кому: " +
                    transit.getWorker().getWorkerFamily().trim() + ", " + transit.getIssueTransitDate() + "\n";
            if (transit.getIssueTransitDateNeed() != null) {
                answer += "Исправить до: " + transit.getIssueTransitDateNeed() + "\n";
            }
            answer += SEPARATOR + text + "\n";
        }
        return answer;
    }

    public String getAnswerForShortShowCommand(IssueStatus issueStatus, Issue issue) {
        List<IssueTransit> transits = issue.getIssueTransits();
        IssueTransit transit = transits.get(transits.size() - 1);
        for (IssueTransit t : transits) {
            if (t.getIssueTransitId() == issueStatus.getIssueTransitId()) {
                transit = t;
            }
        }
        String answer = issue.getIssueText() + "\n";
        String text = "";
        if (transit.getIssueTransitText() != null) {
            Charset w1251 = Charset.forName("Windows-1251");
            text = new String(transit.getIssueTransitText(), w1251);
        }
        answer += SEPARATOR +
                transit.getIssueTransitType().getIssueTransitTypeName().trim() + ", От кого: " +
                transit.getFromWorker().getWorkerFamily().trim() + ", Кому: " +
                transit.getWorker().getWorkerFamily().trim() + ", " + transit.getIssueTransitDate() + "\n";
        if (transit.getIssueTransitDateNeed() != null) {
            answer += "Исправить до: " + transit.getIssueTransitDateNeed() + "\n";
        }
        answer += SEPARATOR + text + "\n";
        return answer;
    }

    public String getAnswerForInboxCommand(List<Issue> Issues) {
        String answer = SEPARATOR + "\n";
        for (Issue issue : Issues) {
            answer += "№: " + issue.getIssueId() + " | Наим-ие: " + issue.getIssueText() + " | "
                    + issue.getIssueDate() + " | Пр-т: " + issue.getIssuePriority() + " | Испр. до: " +
                    issue.getIssueDateNeed() + "\n" + SEPARATOR + "\n";
        }
        return answer;
    }
}
