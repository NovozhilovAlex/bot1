package biz.gelicon.gits.tamtambot.controller;

import chat.tamtam.bot.annotations.CommandHandler;
import chat.tamtam.bot.annotations.UpdateHandler;
import chat.tamtam.bot.longpolling.LongPollingBot;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.BotStartedUpdate;
import chat.tamtam.botapi.model.Message;
import chat.tamtam.botapi.model.MessageCallbackUpdate;
import chat.tamtam.botapi.queries.SendMessageQuery;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @CommandHandler(value = "/find")
    public void onFindCommandEntered(Message message) throws ClientException {
        updateController.processFindCommand(message);
    }

    @CommandHandler("/inbox")
    public void onInboxCommandEntered(Message message) throws ClientException {
        updateController.processInboxCommand(message);
    }

    @CommandHandler("/help")
    public void onHelpCommandEntered(Message message) throws ClientException {
        updateController.processHelpCommand(message);
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
