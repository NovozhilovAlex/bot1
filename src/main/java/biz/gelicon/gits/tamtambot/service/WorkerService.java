package biz.gelicon.gits.tamtambot.service;

import biz.gelicon.gits.tamtambot.entity.Issue;

import java.util.List;

public interface WorkerService {
    List<Issue> getIssuesByUserId(String userId);
}
