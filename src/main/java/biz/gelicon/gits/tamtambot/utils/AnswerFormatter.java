package biz.gelicon.gits.tamtambot.utils;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import biz.gelicon.gits.tamtambot.entity.IssueTransit;
import biz.gelicon.gits.tamtambot.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;

@Component
public class AnswerFormatter {
    private final String SEPARATOR = "----------------------------------------------------------------" +
            "------------------------------------------\n";
    private final IssueService issueService;

    @Autowired
    public AnswerFormatter(IssueService issueService) {
        this.issueService = issueService;
    }

    public String getAnswerForShowCommand(Issue issue) {
        StringBuilder answer = new StringBuilder("<h1>" + issue.getIssueText() + "</h1>" + "\n");
        List<IssueTransit> issueTransits = issue.getIssueTransits();
        IssueStatus issueStatus = issueService.getIssueStatusByIssueId(issue.getIssueId());
        for (IssueTransit issueTransit : issueTransits) {
            String text = "";
            if (issueTransit.getIssueTransitText() != null) {
                Charset w1251 = Charset.forName("Windows-1251");
                text = new String(issueTransit.getIssueTransitText(), w1251);
            }
            if (issueStatus.getIssueTransitId() == issueTransit.getIssueTransitId()) {
                answer.append("<b>").append(SEPARATOR).append(issueTransit.getIssueTransitType()
                                .getIssueTransitTypeName().trim())
                        .append(", От кого: ").append(issueTransit.getFromWorker().getWorkerFamily().trim())
                        .append(", Кому: ").append(issueTransit.getWorker().getWorkerFamily().trim()).append(", ")
                        .append(issueTransit.getIssueTransitDate()).append("\n");
                if (issueTransit.getIssueTransitDateNeed() != null) {
                    answer.append("Исправить до: ").append(issueTransit.getIssueTransitDateNeed()).append("\n");
                }
                answer.append(SEPARATOR).append("</b>").append(text).append("\n");
            } else {
                answer.append(SEPARATOR).append(issueTransit.getIssueTransitType().getIssueTransitTypeName().trim())
                        .append(", От кого: ").append(issueTransit.getFromWorker().getWorkerFamily().trim())
                        .append(", Кому: ").append(issueTransit.getWorker().getWorkerFamily().trim()).append(", ")
                        .append(issueTransit.getIssueTransitDate()).append("\n");
                if (issueTransit.getIssueTransitDateNeed() != null) {
                    answer.append("Исправить до: ").append(issueTransit.getIssueTransitDateNeed()).append("\n");
                }
                answer.append(SEPARATOR).append(text).append("\n");
            }
        }
        return answer.toString();
    }

    public String getAnswerForShortShowCommand(IssueStatus issueStatus, Issue issue) {
        List<IssueTransit> transits = issue.getIssueTransits();
        IssueTransit transit = transits.get(transits.size() - 1);
        for (IssueTransit t : transits) {
            if (t.getIssueTransitId() == issueStatus.getIssueTransitId()) {
                transit = t;
            }
        }
        String answer = "<h1>" + issue.getIssueText() + "</h1>" + "\n";
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
        StringBuilder answer = new StringBuilder(SEPARATOR);
        for (Issue issue : Issues) {
            answer.append("№: ").append(issue.getIssueId()).append(" | Наим-ие: ").append(issue.getIssueText())
                    .append(" | ").append(issue.getIssueDate()).append(" | Пр-т: ").append(issue.getIssuePriority())
                    .append(" | Испр. до: ").append(issue.getIssueDateNeed()).append("\n").append(SEPARATOR);
        }
        return answer.toString();
    }

    public String getLinkAnswer(String uncPath) {
        return "*\\" + uncPath + "*";
    }

    public String getAnswerOnError(Exception e) {
        return "Ой, что-то мне поплохело. Причина: " + e.getMessage();
    }
}
