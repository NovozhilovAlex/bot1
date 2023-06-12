package biz.gelicon.gits.tamtambot.utils;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import biz.gelicon.gits.tamtambot.entity.IssueTransit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class AnswerFormatter {
    private final String SEPARATOR = "----------------------------------------------------------------" +
            "------------------------------------------\n";

    public String getAnswerForShowCommand(Issue issue) {
        StringBuilder answer = new StringBuilder("<h1>" + issue.getIssueText() + "</h1>" + "\n");
        List<IssueTransit> issueTransits = issue.getIssueTransits();
        for (int i = 0; i < issueTransits.size(); i++) {
            String text = "";
            if (issueTransits.get(i).getIssueTransitText() != null) {
                Charset w1251 = Charset.forName("Windows-1251");
                text = new String(issueTransits.get(i).getIssueTransitText(), w1251);
            }
            if (i == issueTransits.size() - 1) {
                answer.append("<mark>").append(SEPARATOR).append(issueTransits.get(i).getIssueTransitType().getIssueTransitTypeName().trim())
                        .append(", От кого: ").append(issueTransits.get(i).getFromWorker().getWorkerFamily().trim())
                        .append(", Кому: ").append(issueTransits.get(i).getWorker().getWorkerFamily().trim()).append(", ")
                        .append(issueTransits.get(i).getIssueTransitDate()).append("\n");
                if (issueTransits.get(i).getIssueTransitDateNeed() != null) {
                    answer.append("Исправить до: ").append(issueTransits.get(i).getIssueTransitDateNeed()).append("\n");
                }
                answer.append(SEPARATOR).append("</mark>").append(text).append("\n");
            } else {
                answer.append(SEPARATOR).append(issueTransits.get(i).getIssueTransitType().getIssueTransitTypeName().trim())
                        .append(", От кого: ").append(issueTransits.get(i).getFromWorker().getWorkerFamily().trim())
                        .append(", Кому: ").append(issueTransits.get(i).getWorker().getWorkerFamily().trim()).append(", ")
                        .append(issueTransits.get(i).getIssueTransitDate()).append("\n");
                if (issueTransits.get(i).getIssueTransitDateNeed() != null) {
                    answer.append("Исправить до: ").append(issueTransits.get(i).getIssueTransitDateNeed()).append("\n");
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
        StringBuilder answer = new StringBuilder(SEPARATOR + "\n");
        for (Issue issue : Issues) {
            answer.append("№: ").append(issue.getIssueId()).append(" | Наим-ие: ").append(issue.getIssueText())
                    .append(" | ").append(issue.getIssueDate()).append(" | Пр-т: ").append(issue.getIssuePriority())
                    .append(" | Испр. до: ").append(issue.getIssueDateNeed()).append("\n").append(SEPARATOR)
                    .append("\n");
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
