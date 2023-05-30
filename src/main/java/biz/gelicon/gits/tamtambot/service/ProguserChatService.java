package biz.gelicon.gits.tamtambot.service;

import biz.gelicon.gits.tamtambot.entity.Proguser;

public interface ProguserChatService {
    boolean isProguserChatFindByChatId(String chatId);
    void insertProguserChat(String name, String chatId);
}
