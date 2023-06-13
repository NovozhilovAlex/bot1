package biz.gelicon.gits.tamtambot.service;

public interface ProguserChatService {
    boolean isProguserChatFindByUserId(String userId);

    void insertProguserChat(String name, String chatId);
}
