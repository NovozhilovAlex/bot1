package biz.gelicon.gits.tamtambot.service.impl;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.repository.IssueRepository;
import biz.gelicon.gits.tamtambot.repository.IssueStatusRepository;
import biz.gelicon.gits.tamtambot.repository.IssueTransitRepository;
import biz.gelicon.gits.tamtambot.repository.WorkerRepository;
import biz.gelicon.gits.tamtambot.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final IssueStatusRepository issueStatusRepository;

    @Autowired
    public IssueServiceImpl(IssueRepository issueRepository, IssueStatusRepository issueStatusRepository) {
        this.issueRepository = issueRepository;
        this.issueStatusRepository = issueStatusRepository;
    }

    @Override
    public Issue getIssueById(int id) {
        Optional<Issue> optional = issueRepository.findByIssueId(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("Issue not exist with id = " + id);
        }
    }

    public IssueStatus getIssueStatusByIssueId(int issueId) {
        Optional<IssueStatus> optional = issueStatusRepository.findByIssueId(issueId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("IssueStatus not exist with issue id = " + issueId);
        }
    }
}
