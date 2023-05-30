package biz.gelicon.gits.tamtambot.service.impl;

import biz.gelicon.gits.tamtambot.entity.Proguser;
import biz.gelicon.gits.tamtambot.entity.ProguserChat;
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
    public boolean isProguserChatFindByChatId(String chatId) {
        Optional<ProguserChat> optional = proguserChatRepository.findByChatId(chatId);
        return optional.isPresent();
    }

    @Override
    public void insertProguserChat(String name, String chatId) {
        if (isProguserChatFindByChatId(chatId)) {
            return;
        }
        Proguser proguser = findProguserByName(name);
        proguserChatRepository.insert(2, proguser.getProguserId(), 1061, chatId);
    }

    private Proguser findProguserByName(String name) {
        Optional<Proguser> optional = proguserRepository.findByProguserName(name);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new ResourceNotFoundException("Proguser not exist with name: " + name);
        }
    }
}
