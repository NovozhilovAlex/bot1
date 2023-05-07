package ru.yumeno.tamtambot.controller;

import chat.tamtam.bot.builders.NewMessageBodyBuilder;
import chat.tamtam.bot.builders.attachments.AttachmentsBuilder;
import chat.tamtam.bot.builders.attachments.InlineKeyboardBuilder;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.*;
import chat.tamtam.botapi.queries.SendMessageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import ru.yumeno.tamtambot.entity.Task;
import ru.yumeno.tamtambot.exceptions.ResourceNotFoundException;
import ru.yumeno.tamtambot.service.TaskService;
import ru.yumeno.tamtambot.service.WorkerService;

import java.util.List;

@Controller
@Slf4j
public class UpdateController {
    private TamtamBot tamtamBot;
    private final TaskService taskService;
    private final WorkerService workerService;

    @Autowired
    public UpdateController(TaskService taskService, WorkerService workerService) {
        this.taskService = taskService;
        this.workerService = workerService;
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
                tamtamBot.sendAnswerMessage(createSendMessageQuery(
                        update.getMessage().getRecipient().getChatId(), taskAnswerFormatter(task)));
            } catch (ResourceNotFoundException e) {
                log.debug(e.getMessage());
                tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                        "Задачи с id = " + messageText + " не существует"));
            }
        }
    }

    public void processBotStartedUpdate(BotStartedUpdate update) throws ClientException {
        CallbackButton btn = new CallbackButton("btn pressed", "Все задачи");
        NewMessageBody answer = NewMessageBodyBuilder.ofText("Для отображения задачи введите ее id\n" +
                        "Для отображения всех задач введите команду /all_tasks или нажмите на кнопку")
                .withAttachments(AttachmentsBuilder
                        .inlineKeyboard(InlineKeyboardBuilder
                                .singleRow(btn)))
                .build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer).chatId(update.getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processAllTasksCommand(Message message) throws ClientException {
        log.info("Try to get all tasks");
        try {
            String username = message.getSender().getUsername();
            List<Task> tasks = workerService.getTasksByWorkerEmail(usernameToEmail(username));
            tamtamBot.sendAnswerMessage(createSendMessageQuery(
                    message.getRecipient().getChatId(), allTasksAnswerFormatter(tasks)));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "У Вас сейчас нет задач"));
        }
    }

    public void processButtonPressed(MessageCallbackUpdate update) throws ClientException {
        log.info("Try to get all tasks");
        try {
            String username = update.getCallback().getUser().getUsername();
            System.out.println(username);
            List<Task> tasks = workerService.getTasksByWorkerEmail(usernameToEmail(username));
            tamtamBot.sendAnswerMessage(createSendMessageQuery(
                    update.getMessage().getRecipient().getChatId(), allTasksAnswerFormatter(tasks)));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "У Вас сейчас нет задач"));
        }
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

    private SendMessageQuery createSendMessageQuery(Long chatId, String text) {
        NewMessageBody answer = NewMessageBodyBuilder.ofText(text).build();
        return new SendMessageQuery(tamtamBot.getClient(), answer).chatId(chatId);
    }

    private String allTasksAnswerFormatter(List<Task> tasks) {
        StringBuilder answer = new StringBuilder();
        for (Task task : tasks) {
            answer.append("Номер: ").append(task.getTaskId()).append(" | Наименование: ").append(task.getTaskText())
                    .append("\n");
        }
        return answer.toString();
    }

    private String taskAnswerFormatter(Task task) {
        return "Номер: " + task.getTaskId() + " | Наименование: " + task.getTaskText();
    }

    private String usernameToEmail(String username) {
        return username.substring(8) + "@gelicon.biz";
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
