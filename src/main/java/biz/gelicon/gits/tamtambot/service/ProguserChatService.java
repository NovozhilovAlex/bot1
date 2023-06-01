package biz.gelicon.gits.tamtambot.service;

public interface ProguserChatService {
    boolean isProguserChatFindByChatId(String chatId);

    void insertProguserChat(String name, String chatId);
}
