package biz.gelicon.gits.tamtambot.service.impl;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.repository.IssueRepository;
import biz.gelicon.gits.tamtambot.repository.IssueTransitRepository;
import biz.gelicon.gits.tamtambot.repository.WorkerRepository;
import biz.gelicon.gits.tamtambot.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final IssueTransitRepository issueTransitRepository;
    private final WorkerRepository workerRepository;

    @Autowired
    public IssueServiceImpl(IssueRepository issueRepository, IssueTransitRepository issueTransitRepository, WorkerRepository workerRepository) {
        this.issueRepository = issueRepository;
        this.issueTransitRepository = issueTransitRepository;
        this.workerRepository = workerRepository;
    }

    @Override
    public Issue getIssueById(int id) {
        Optional<Issue> optional = issueRepository.findByIssueId(id);
        if (optional.isPresent()) {
            Issue issue = optional.get();
            return issue;
//            List<IssueTransit> issueTransits = issueTransitRepository.findAllByIssue(issue);
//            if (!issueTransits.isEmpty()) {
//                issue.setIssueTransits(issueTransits);
//                return issue;
//            } else {
//                throw new ResourceNotFoundException("No issue transits with issue id = " + id);
//            }
        } else {
            throw new ResourceNotFoundException("Issue not exist with id = " + id);
        }
    }
}
