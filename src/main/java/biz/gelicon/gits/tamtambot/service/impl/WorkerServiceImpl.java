package biz.gelicon.gits.tamtambot.service.impl;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.Worker;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.repository.IssueRepository;
import biz.gelicon.gits.tamtambot.repository.WorkerRepository;
import biz.gelicon.gits.tamtambot.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkerServiceImpl implements WorkerService {

    private final WorkerRepository workerRepository;
    private final IssueRepository issueRepository;

    @Autowired
    public WorkerServiceImpl(WorkerRepository workerRepository, IssueRepository issueRepository) {
        this.workerRepository = workerRepository;
        this.issueRepository = issueRepository;
    }

    @Override
    public List<Issue> getIssuesByWorkerEmail(String email) {
        Optional<Worker> optional = workerRepository.findByWorkerEmail(email);
        if (optional.isPresent()) {
            Worker worker = optional.get();
            List<Issue> issues = issueRepository.findAllIssues(worker.getWorkerId());
            if (!issues.isEmpty()) {
                return issues;
            } else {
                throw new ResourceNotFoundException("No issues with worker id = " + worker.getWorkerId());
            }
        } else {
            throw new ResourceNotFoundException("Worker not exist with email: " + email);
        }
    }
}
