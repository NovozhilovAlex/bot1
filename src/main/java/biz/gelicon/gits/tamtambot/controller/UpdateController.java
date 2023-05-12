package biz.gelicon.gits.tamtambot.controller;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueAppendix;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.service.IssueService;
import biz.gelicon.gits.tamtambot.service.WorkerService;
import biz.gelicon.gits.tamtambot.utils.AnswerFormatter;
import biz.gelicon.gits.tamtambot.utils.CommandParser;
import biz.gelicon.gits.tamtambot.utils.ParsedCommand;
import biz.gelicon.gits.tamtambot.utils.ZlibCompressor;
import chat.tamtam.bot.builders.NewMessageBodyBuilder;
import chat.tamtam.bot.builders.attachments.AttachmentsBuilder;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.TamTamUploadAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.*;
import chat.tamtam.botapi.queries.SendMessageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Controller
@Slf4j
public class UpdateController {
    private final IssueService issueService;
    private final WorkerService workerService;
    private final CommandParser commandParser;
    private final AnswerFormatter answerFormatter;
    private final ZlibCompressor compressor;
    private TamtamBot tamtamBot;

    @Autowired
    public UpdateController(IssueService issueService, WorkerService workerService, CommandParser commandParser,
                            AnswerFormatter answerFormatter, ZlibCompressor compressor) {
        this.issueService = issueService;
        this.workerService = workerService;
        this.commandParser = commandParser;
        this.answerFormatter = answerFormatter;
        this.compressor = compressor;
    }

    public void registerBot(TamtamBot tamtamBot) {
        this.tamtamBot = tamtamBot;
    }

    public void processFindCommand(Message message) throws ClientException {
        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }
        ParsedCommand parsedCommand = commandParser.getParsedCommand(messageText);
        if (parsedCommand.getText() == null) {
            log.debug("Find command argument is null");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Команда /find должна иметь аргумент"));
            processHelpCommand(message);
            return;
        }
        if (parsedCommand.getText().charAt(0) == '#' && isDigit(parsedCommand.getText().substring(1))) {
            String IssueId = parsedCommand.getText().substring(1);
            log.info("Try to get issue info by id = " + IssueId);
            try {
                Issue issue = issueService.getIssueById(Integer.parseInt(IssueId));
                AttachmentsBuilder attachmentsBuilder = createAttachments(issue.getIssueAppendices());
                NewMessageBody answer = NewMessageBodyBuilder
                        .ofText(answerFormatter.getAnswerForFindCommand(issue))
                        .withAttachments(attachmentsBuilder)
                        .build();
                SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                        .chatId(message.getRecipient().getChatId());
                tamtamBot.sendAnswerMessage(query);
            } catch (ResourceNotFoundException e) {
                log.debug(e.getMessage());
                tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                        "Задачи с id = " + IssueId + " не существует"));
            } catch (RuntimeException | APIException | IOException | ClientException e) {
                log.warn(e.getMessage());
                tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                        "Ой, что то мне поплохело. Причина: " + e.getMessage()));
            }
        } else {
            log.debug("find command argument not valid");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Неверный синтаксис для команды /find"));
            processHelpCommand(message);
        }
    }

    public void processBotStartedUpdate(BotStartedUpdate update) throws ClientException {
//        CallbackButton btn = new CallbackButton("btn pressed", "Все задачи");
        NewMessageBody answer = NewMessageBodyBuilder.ofText("""
                        Доступные команды:

                        /help - Список команд
                        /find #{номер_задачи} - Содержание задачи""")
//                .withAttachments(AttachmentsBuilder
//                        .inlineKeyboard(InlineKeyboardBuilder
//                                .singleRow(btn)))
                .build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer).chatId(update.getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processHelpCommand(Message message) throws ClientException {
        NewMessageBody answer = NewMessageBodyBuilder.ofText("""
                Доступные команды:

                /help - Информация о боте
                /find #{номер_задачи} - Содержание задачи""").build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                .chatId(message.getRecipient().getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processInboxCommand(Message message) throws ClientException {
        log.info("Try to get all issues");
        try {
            String username = message.getSender().getUsername();
            List<Issue> Issues = workerService.getIssuesByWorkerEmail(usernameToEmail(username));
            tamtamBot.sendAnswerMessage(createSendMessageQuery(
                    message.getRecipient().getChatId(), answerFormatter.getAnswerForInboxCommand(Issues)));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "У Вас сейчас нет задач"));
        }
    }

    public void processButtonPressed(MessageCallbackUpdate update) throws ClientException {
        log.info("Try to get all issues");
        try {
            String username = update.getCallback().getUser().getUsername();
            System.out.println(username);
            List<Issue> Issues = workerService.getIssuesByWorkerEmail(usernameToEmail(username));
            tamtamBot.sendAnswerMessage(createSendMessageQuery(
                    update.getMessage().getRecipient().getChatId(), answerFormatter.getAnswerForInboxCommand(Issues)));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "У Вас сейчас нет задач"));
        }
    }

    private SendMessageQuery createSendMessageQuery(Long chatId, String text) {
        NewMessageBody answer = NewMessageBodyBuilder.ofText(text).build();
        return new SendMessageQuery(tamtamBot.getClient(), answer).chatId(chatId);
    }

    private String usernameToEmail(String username) {
        return username.substring(8) + "@gelicon.biz";
    }

    private AttachmentsBuilder createAttachments(List<IssueAppendix> appendices) throws IOException,
            ClientException, APIException {
        if (appendices == null) {
            return null;
        }
        TamTamBotAPI botAPI = new TamTamBotAPI(tamtamBot.getClient());
        TamTamUploadAPI uploadAPI = new TamTamUploadAPI(tamtamBot.getClient());
        List<String> tokens = new ArrayList<>();
        for (IssueAppendix appendix : appendices) {
            byte[] content = appendix.getIssueAppendixContent();
            if (content == null) {
                continue;
            }
            String name = appendix.getIssueAppendixName();
            String format = cutFileFormat(name);
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            compressor.decompressFile(bais, new File("tmp/file." + format));
            if (Objects.equals(format, "png") | Objects.equals(format, "bmp") |
                    Objects.equals(format, "jpg") | Objects.equals(format, "jpeg")) {
                UploadEndpoint endpoint = botAPI.getUploadUrl(UploadType.IMAGE).execute();
                String uploadUrl = endpoint.getUrl();
                PhotoTokens photoTokens = uploadAPI.uploadImage(uploadUrl, new File("tmp/file." + format)).execute();
                Collection<PhotoToken> tokenSet = photoTokens.getPhotos().values();
                for (PhotoToken token : tokenSet) {
                    tokens.add(token.getToken());
                }
            }
        }
        return AttachmentsBuilder.photos(tokens.toArray(new String[0]));
    }

    private String cutFileFormat(String fileName) {
        int dotIndex = fileName.indexOf('.');
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == '.') {
                dotIndex = i;
            }
        }
        return fileName.substring(dotIndex + 1);
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
