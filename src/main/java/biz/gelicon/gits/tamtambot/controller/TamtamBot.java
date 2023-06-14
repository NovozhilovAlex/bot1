package biz.gelicon.gits.tamtambot.controller;

import chat.tamtam.bot.annotations.CommandHandler;
import chat.tamtam.bot.annotations.UpdateHandler;
import chat.tamtam.bot.longpolling.LongPollingBot;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.Message;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.model.MessageCreatedUpdate;
import chat.tamtam.botapi.queries.SendMessageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TamtamBot extends LongPollingBot {
    private final UpdateController updateController;

    public TamtamBot(@Value("${bot.token}") String accessToken,
                     @Autowired UpdateController updateController) {
        super(accessToken);
        this.updateController = updateController;
    }

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @CommandHandler(value = "/show")
    public void onShowCommandEntered(Message message) throws ClientException {
        updateController.processShowCommand(message);
    }

    @CommandHandler("/inbox")
    public void onInboxCommandEntered(Message message) throws ClientException {
        updateController.processInboxCommand(message);
    }

    @CommandHandler("/help")
    public void onHelpCommandEntered(Message message) throws ClientException {
        updateController.processHelpCommand(message);
    }

    @CommandHandler("/auth")
    public void onAuthCommandEntered(Message message) throws ClientException {
        updateController.processAuthCommand(message);
    }

    @CommandHandler("/logout")
    public void onLogoutCommandEntered(Message message) throws ClientException {
        updateController.processLogoutCommand(message);
    }

    @UpdateHandler
    public void onBotStarted(BotStartedUpdate update) throws ClientException {
        updateController.processBotStartedUpdate(update);
    }

    @UpdateHandler
    public void onButtonPressed(MessageCallbackUpdate update) throws ClientException {
        updateController.processButtonPressed(update);
    }

    @UpdateHandler
    public void onMessageCreated(MessageCreatedUpdate update) throws ClientException {
        updateController.processMessageCreatedUpdate(update);
    }

    public void sendAnswerMessage(SendMessageQuery query) throws ClientException {
        if (query != null) {
            query.enqueue();
        }
    }
}
