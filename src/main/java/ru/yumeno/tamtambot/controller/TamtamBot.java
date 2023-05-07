package ru.yumeno.tamtambot.controller;

import chat.tamtam.bot.annotations.CommandHandler;
import chat.tamtam.bot.annotations.UpdateHandler;
import chat.tamtam.bot.exceptions.TamTamBotException;
import chat.tamtam.bot.longpolling.LongPollingBot;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.Message;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.queries.SendMessageQuery;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TamtamBot extends LongPollingBot {
    private final UpdateController updateController;
    public TamtamBot(@Value("${bot.token}") String accessToken,
                     @Autowired UpdateController updateController) throws TamTamBotException {
        super(accessToken);
        this.updateController = updateController;
        this.start();
    }

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @UpdateHandler
    public void onMessageCreated(MessageCreatedUpdate update) throws ClientException {
        updateController.processMessageCreatedUpdate(update);
    }

    @CommandHandler("/all_tasks")
    public void onAllTasksCommandEntered(Message message) throws ClientException {
        updateController.processAllTasksCommand(message);
    }

    @UpdateHandler
    public void onBotStarted(BotStartedUpdate update) throws ClientException {
        updateController.processBotStartedUpdate(update);
    }

    @UpdateHandler
    public void onButtonPressed(MessageCallbackUpdate update) throws ClientException {
        updateController.processButtonPressed(update);
    }

    public void sendAnswerMessage(SendMessageQuery query) throws ClientException {
        if (query != null) {
            query.enqueue();
        }
    }
}
