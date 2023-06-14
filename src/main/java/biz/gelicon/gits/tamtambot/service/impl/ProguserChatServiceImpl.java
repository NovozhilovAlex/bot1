package biz.gelicon.gits.tamtambot.service.impl;

import biz.gelicon.gits.tamtambot.entity.Proguser;
import biz.gelicon.gits.tamtambot.entity.ProguserChat;
import biz.gelicon.gits.tamtambot.exceptions.AlreadyAuthException;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.repository.ProguserChatRepository;
import biz.gelicon.gits.tamtambot.repository.ProguserRepository;
import biz.gelicon.gits.tamtambot.service.ProguserChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProguserChatServiceImpl implements ProguserChatService {
    private final ProguserChatRepository proguserChatRepository;
    private final ProguserRepository proguserRepository;

    @Autowired
    public ProguserChatServiceImpl(ProguserChatRepository proguserChatRepository,
                                   ProguserRepository proguserRepository) {
        this.proguserChatRepository = proguserChatRepository;
        this.proguserRepository = proguserRepository;
    }

    @Override
    public boolean isProguserChatFindByUserId(String userId) {
        Optional<ProguserChat> optional = proguserChatRepository.findByUserId(userId);
        return optional.isPresent();
    }

    @Override
    public void insertProguserChat(String name, String userId) {
        if (isProguserChatFindByUserId(userId)) {
            throw new AlreadyAuthException("ProguserChat with userId = " + userId + "already authenticated");
        }
        Proguser proguser = findProguserByName(name);
        proguserChatRepository.insert(proguser.getProguserId(), 1061, userId);
    }

    @Override
    public void deleteProguserChat(String userId) {
        if (!isProguserChatFindByUserId(userId)) {
            throw new ResourceNotFoundException("ProguserChat not found with userId = " + userId);
        }
        proguserChatRepository.deleteByUserId(userId);
    }

    private Proguser findProguserByName(String username) {
        Optional<Proguser> optional = proguserRepository.findByProguserName(username);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("Proguser not found with name: " + username);
        }
    }
}
