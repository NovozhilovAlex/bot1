package biz.gelicon.gits.tamtambot.service;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;

public interface IssueService {
    Issue getIssueById(int id);

    IssueStatus getIssueStatusByIssueId(int issueId);
}
