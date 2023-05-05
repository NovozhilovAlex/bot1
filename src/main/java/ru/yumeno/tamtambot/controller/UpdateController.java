package ru.yumeno.tamtambot.controller;

import chat.tamtam.bot.builders.NewMessageBodyBuilder;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.model.NewMessageBody;
import chat.tamtam.botapi.queries.SendMessageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.yumeno.tamtambot.entity.Task;
import ru.yumeno.tamtambot.exceptions.ResourceNotFoundException;
import ru.yumeno.tamtambot.service.TaskService;

@Controller
@Slf4j
public class UpdateController {
    private TamtamBot tamtamBot;
    private final TaskService taskService;

    @Autowired
    public UpdateController(TaskService taskService) {
        this.taskService = taskService;
    }

    public void registerBot(TamtamBot tamtamBot) {
        this.tamtamBot = tamtamBot;
    }

    public void processMessageCreatedUpdate(MessageCreatedUpdate update) throws ClientException {
        if (!isUpdateValid(update)) {
            return;
        }
        String messageText = update.getMessage().getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
        }
        else if (!isDigit(messageText)) {
            log.debug("Message text is not digit");
        } else {
            log.info("Try to get task text by id");
            try {
                Task task = taskService.getTaskById(Integer.parseInt(messageText));
                String taskText = task.getTaskText();
                NewMessageBody replyMessage = NewMessageBodyBuilder
                        .ofText("Текст задачи: " + taskText).build();
                Long chatId = update.getMessage().getRecipient().getChatId();
                SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), replyMessage).chatId(chatId);
                tamtamBot.sendAnswerMessage(query);
            } catch (ResourceNotFoundException e) {
                log.debug(e.getMessage());
                NewMessageBody answer = NewMessageBodyBuilder
                        .ofText("Задачи с id = " + messageText + " не существует").build();
                Long chatId = update.getMessage().getRecipient().getChatId();
                SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer).chatId(chatId);
                tamtamBot.sendAnswerMessage(query);
            }
        }
    }

    public void processBotStartedUpdate(BotStartedUpdate update) throws ClientException {
        NewMessageBody answer = NewMessageBodyBuilder
                .ofText("Для отображения текста задачи введите ее id").build();
        Long chatId = update.getChatId();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer).chatId(chatId);
        tamtamBot.sendAnswerMessage(query);
    }

    private boolean isUpdateValid(MessageCreatedUpdate update) {
        if (update == null) {
            log.debug("Received update is null");
            return false;
        }
        if (update.getMessage().getBody().getAttachments() != null) {
            log.debug("Received update has attachments");
            return false;
        }
        return true;
    }

    private boolean isDigit(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
