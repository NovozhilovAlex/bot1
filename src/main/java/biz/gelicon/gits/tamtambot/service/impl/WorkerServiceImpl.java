package biz.gelicon.gits.tamtambot.service.impl;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.ProguserChat;
import biz.gelicon.gits.tamtambot.entity.ProguserWorker;
import biz.gelicon.gits.tamtambot.entity.Worker;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.repository.IssueRepository;
import biz.gelicon.gits.tamtambot.repository.ProguserChatRepository;
import biz.gelicon.gits.tamtambot.repository.ProguserWorkerRepository;
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
    private final ProguserChatRepository proguserChatRepository;
    private final ProguserWorkerRepository proguserWorkerRepository;

    @Autowired
    public WorkerServiceImpl(WorkerRepository workerRepository, IssueRepository issueRepository, ProguserChatRepository proguserChatRepository, ProguserWorkerRepository proguserWorkerRepository) {
        this.workerRepository = workerRepository;
        this.issueRepository = issueRepository;
        this.proguserChatRepository = proguserChatRepository;
        this.proguserWorkerRepository = proguserWorkerRepository;
    }

    @Override
    public List<Issue> getIssuesByWorkerEmail(String email) {
        Optional<Worker> optional = workerRepository.findByWorkerEmail(email);
        if (optional.isPresent()) {
            Worker worker = optional.get();
            return issueRepository.findAllIssues(worker.getWorkerId());
        } else {
            throw new ResourceNotFoundException("Worker not exist with email: " + email);
        }
    }

    @Override
    public List<Issue> getIssuesByChatId(String chatId) {
        Optional<ProguserChat> optional = proguserChatRepository.findByChatId(chatId);
        if (optional.isPresent()) {
            ProguserChat proguserChat = optional.get();
            int proguserId = proguserChat.getProguserId();
            Optional<ProguserWorker> optional2 = proguserWorkerRepository.findByProguserId(proguserId);
            if (optional2.isPresent()) {
                ProguserWorker proguserWorker = optional2.get();
                return issueRepository.findAllIssues(proguserWorker.getWorkerId());
            } else {
                throw new ResourceNotFoundException("ProguserWorker not exist with proguserId = " + proguserId);
            }
        } else {
            throw new ResourceNotFoundException("ProguserChat not exist with chatId = " + chatId);
        }
    }
}
