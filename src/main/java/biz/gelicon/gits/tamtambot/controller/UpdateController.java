package biz.gelicon.gits.tamtambot.controller;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueAppendix;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.service.IssueService;
import biz.gelicon.gits.tamtambot.service.ProguserChatService;
import biz.gelicon.gits.tamtambot.service.WorkerService;
import biz.gelicon.gits.tamtambot.utils.*;
import chat.tamtam.bot.builders.NewMessageBodyBuilder;
import chat.tamtam.bot.builders.attachments.AttachmentsBuilder;
import chat.tamtam.bot.builders.attachments.InlineKeyboardBuilder;
import chat.tamtam.botapi.TamTamBotAPI;
import chat.tamtam.botapi.TamTamUploadAPI;
import chat.tamtam.botapi.exceptions.APIException;
import chat.tamtam.botapi.exceptions.ClientException;
import chat.tamtam.botapi.model.*;
import chat.tamtam.botapi.queries.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static java.util.Map.entry;

@Controller
@Slf4j
public class UpdateController {
    private final IssueService issueService;
    private final WorkerService workerService;
    private final ProguserChatService proguserChatService;
    private final CommandParser commandParser;
    private final AnswerFormatter answerFormatter;
    private final ZlibCompressor compressor;
    private TamtamBot tamtamBot;

    @Autowired
    public UpdateController(IssueService issueService, WorkerService workerService, ProguserChatService proguserChatService, CommandParser commandParser,
                            AnswerFormatter answerFormatter, ZlibCompressor compressor) {
        this.issueService = issueService;
        this.workerService = workerService;
        this.proguserChatService = proguserChatService;
        this.commandParser = commandParser;
        this.answerFormatter = answerFormatter;
        this.compressor = compressor;
    }

    public void registerBot(TamtamBot tamtamBot) {
        this.tamtamBot = tamtamBot;
    }

    public void processShowCommand(Message message) throws ClientException {
        String chatId = String.valueOf(message.getRecipient().getChatId());
        if (!isChatIdAuthorized(chatId)) {
            return;
        }

        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }

        ParsedCommand parsedCommand = commandParser.getParsedCommand(messageText);
        if (parsedCommand.getText() == null) {
            log.debug("Show command argument is null");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Команда /show должна иметь аргумент"));
            processHelpCommand(message);
            return;
        }

        String[] commandArgs = parsedCommand.getText().split(" ");
        if (commandArgs[0].charAt(0) == '#' && isDigit(commandArgs[0].substring(1))) {
            String IssueId = commandArgs[0].substring(1);
            log.info("Try to get issue info by id = " + IssueId);
            try {
                String answerText;
                Issue issue = issueService.getIssueById(Integer.parseInt(IssueId));
                AttachmentsCarrier attachmentsCarrier = createAttachments(issue.getIssueAppendices());
                if (commandArgs.length == 1) {
                    answerText = answerFormatter.getAnswerForShowCommand(issue);
                } else if (commandArgs.length == 2 && Objects.equals(commandArgs[1].trim(), "short")) {
                    System.out.println("123");
                    IssueStatus issueStatus = issueService.getIssueStatusByIssueId(issue.getIssueId());
                    answerText = answerFormatter.getAnswerForShortShowCommand(issueStatus, issue);
                } else {
                    log.debug("show command argument not valid");
                    tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                            "Неверный синтаксис для команды /show"));
                    processHelpCommand(message);
                    return;
                }
                if (answerText.length() > 4000) {
                    List<String> answerTextList = answerTextToStringList(answerText);
                    for (int i = 0; i < answerTextList.size() - 1; i++) {
                        NewMessageBody answer = NewMessageBodyBuilder
                                .ofText(answerTextList.get(i))
                                .build();
                        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                                .chatId(message.getRecipient().getChatId());
                        createSendMessageWithAttachmentsQuery(query);
                    }
                    NewMessageBody answer = NewMessageBodyBuilder
                            .ofText(answerTextList.get(answerTextList.size() - 1))
                            .withAttachments(attachmentsCarrier.getImages())
                            .build();
                    SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                            .chatId(message.getRecipient().getChatId());
                    createSendMessageWithAttachmentsQuery(query);
                } else {
                    NewMessageBody answer = NewMessageBodyBuilder
                            .ofText(answerText)
                            .withAttachments(attachmentsCarrier.getImages())
                            .build();
                    SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                            .chatId(message.getRecipient().getChatId());
                    createSendMessageWithAttachmentsQuery(query);
                }

                for (UploadedInfo info : attachmentsCarrier.getFiles()) {
                    NewMessageBody answer = NewMessageBodyBuilder
                            .ofText("")
                            .withAttachments(AttachmentsBuilder.files(info))
                            .build();
                    SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                            .chatId(message.getRecipient().getChatId());
                    createSendMessageWithAttachmentsQuery(query);
                }
            } catch (ResourceNotFoundException e) {
                log.debug(e.getMessage());
                tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                        "Задачи с id = " + IssueId + " не существует"));
            } catch (APIException | IOException | InterruptedException | RuntimeException e) {
                log.warn(e.getMessage());
                tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                        answerFormatter.getAnswerOnError(e)));
            }
        } else {
            log.debug("show command argument not valid");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Неверный синтаксис для команды /show"));
            processHelpCommand(message);
        }
    }

    public void processBotStartedUpdate(BotStartedUpdate update) throws ClientException {
//        try {
//            String chatId = String.valueOf(update.getChatId());
//            String username = update.getUser().getUsername();
//            proguserChatService.insertProguserChat(username.substring(8).toUpperCase(), chatId);
//        } catch (ResourceNotFoundException e) {
//            log.debug(e.getMessage());
//            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getChatId(),
//                    "Proguser с именем " + update.getUser().getUsername().substring(8).toUpperCase() +
//                            "не найден"));
//        }

        NewMessageBody answer = NewMessageBodyBuilder
                .ofText("Введите команду /auth {пароль от gits} для авторизации")
                .build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer).chatId(update.getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processHelpCommand(Message message) throws ClientException {
        CallbackButton btn = new CallbackButton("btn pressed", "Все задачи");
        NewMessageBody answer = NewMessageBodyBuilder.ofText("Доступные команды:\n" +
                        "/help - Список команд\n" +
                        "/show #{номер_задачи} - Содержание задачи\n" +
                        "/show #{номер_задачи} short - Краткое содержание задачи\n" +
                        "/inbox - Список всех Ваших задач")
                .withAttachments(AttachmentsBuilder
                        .inlineKeyboard(InlineKeyboardBuilder
                                .singleRow(btn)))
                .build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                .chatId(message.getRecipient().getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processAuthCommand(Message message) throws ClientException {
        String chatId = String.valueOf(message.getRecipient().getChatId());
        String username = message.getSender().getUsername().substring(8).toUpperCase().trim();

        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }

        ParsedCommand parsedCommand = commandParser.getParsedCommand(messageText);
        if (parsedCommand.getText() == null) {
            log.debug("Auth command argument is null");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Команда /auth должна иметь аргумент (пароль)"));
            return;
        }

        log.info("Try to auth");
        try {
            DriverManager.getConnection ("jdbc:firebirdsql://10.15.3.43:3050/gits_test" +
                    "?authPlugins=Legacy_Auth", username, parsedCommand.getText());
            proguserChatService.insertProguserChat(username, chatId);
            log.debug("User with chatId = " + chatId + "has been authorized");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Вы успешно авторизовались"));
            processHelpCommand(message);
        } catch (SQLException e) {
            log.debug("Incorrect password");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Неверный пароль"));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(Long.valueOf(chatId),
                    "Proguser с именем " + username + "не найден"));
        }
    }

    public void processInboxCommand(Message message) throws ClientException {
        String chatId = String.valueOf(message.getRecipient().getChatId());
        if (!isChatIdAuthorized(chatId)) {
            return;
        }

        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }

        ParsedCommand parsedCommand = commandParser.getParsedCommand(messageText);
        if (parsedCommand.getText() != null) {
            log.debug("Inbox command has args");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Команда /inbox не должна содержать аргументы"));
            processHelpCommand(message);
            return;
        }

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
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    answerFormatter.getAnswerOnError(e)));
        }
    }

    public void processButtonPressed(MessageCallbackUpdate update) throws ClientException {
        String chatId = String.valueOf(update.getMessage().getRecipient().getChatId());
        if (!isChatIdAuthorized(chatId)) {
            return;
        }
        log.info("Try to get all issues");
        try {
            String username = update.getCallback().getUser().getUsername();
            List<Issue> Issues = workerService.getIssuesByWorkerEmail(usernameToEmail(username));
            tamtamBot.sendAnswerMessage(createSendMessageQuery(
                    update.getMessage().getRecipient().getChatId(), answerFormatter.getAnswerForInboxCommand(Issues)));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "У Вас сейчас нет задач"));
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    answerFormatter.getAnswerOnError(e)));
        }
    }

    public void processMessageCreatedUpdate(MessageCreatedUpdate update) throws ClientException {
        String chatId = String.valueOf(update.getMessage().getRecipient().getChatId());
        if (!isChatIdAuthorized(chatId)) {
            return;
        }
        processHelpCommand(update.getMessage());
    }

    private SendMessageQuery createSendMessageQuery(Long chatId, String text) {
        NewMessageBody answer = NewMessageBodyBuilder.ofText(text).build();
        return new SendMessageQuery(tamtamBot.getClient(), answer).chatId(chatId);
    }

    private void createSendMessageWithAttachmentsQuery(SendMessageQuery query) throws InterruptedException {
        boolean flag = true;
        while (flag) {
            log.info("Try to send message with attachment");
            Thread.sleep(100);
            try {
                query.execute();
                flag = false;
            } catch (ClientException | APIException e) {
                if (!e.getMessage().contains("errors.process.attachment.file.not.processed")) {
                    flag = false;
                }
                log.warn(e.getMessage());
            }
        }
    }

    private String usernameToEmail(String username) {
        return username.substring(8).trim() + "@gelicon.biz";
    }

    private AttachmentsCarrier createAttachments(List<IssueAppendix> appendices) throws IOException,
            ClientException, APIException {
        if (appendices == null) {
            return null;
        }
        TamTamBotAPI botAPI = new TamTamBotAPI(tamtamBot.getClient());
        TamTamUploadAPI uploadAPI = new TamTamUploadAPI(tamtamBot.getClient());
        List<String> tokens = new ArrayList<>();
        List<UploadedInfo> uploadedInfos = new ArrayList<>();
        for (IssueAppendix appendix : appendices) {
            byte[] content = appendix.getIssueAppendixContent();
            if (content == null) {
                continue;
            }
            String name = appendix.getIssueAppendixName();
            String format = cutFileFormat(name);
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
//            File file = new File(fileNameToLat(name));
            File file = File.createTempFile(fileNameToLat(cutFileName(name)), "." + format);
            compressor.decompressFile(bais, file);
            if (Set.of("png", "bmp", "jpg", "jpeg", "gif").contains(format.toLowerCase())) {
                UploadEndpoint endpoint = botAPI.getUploadUrl(UploadType.IMAGE).execute();
                String uploadUrl = endpoint.getUrl();
                PhotoTokens photoTokens = uploadAPI.uploadImage(uploadUrl, file).execute();
                Collection<PhotoToken> tokenSet = photoTokens.getPhotos().values();
                for (PhotoToken token : tokenSet) {
                    tokens.add(token.getToken());
                }
            } else {
                UploadEndpoint endpoint = botAPI.getUploadUrl(UploadType.FILE).execute();
                String uploadUrl = endpoint.getUrl();
                UploadedInfo uploadedInfo = uploadAPI.uploadFile(uploadUrl, file).execute();
                uploadedInfos.add(uploadedInfo);
            }
        }
        AttachmentsBuilder builder = AttachmentsBuilder.photos(tokens.toArray(new String[0]));
        return new AttachmentsCarrier(builder, uploadedInfos);
    }

    private boolean isChatIdAuthorized(String chatId) throws ClientException {
        if (proguserChatService.isProguserChatFindByChatId(chatId)) {
            return true;
        } else {
            log.debug("Unauthorized user, chatId = " + chatId);
            tamtamBot.sendAnswerMessage(createSendMessageQuery(Long.valueOf(chatId),
                    "У Вас нет доступа"));
            return false;
        }
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

    private String cutFileName(String fileName) {
        int dotIndex = fileName.indexOf('.');
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == '.') {
                dotIndex = i;
            }
        }
        return fileName.substring(0, dotIndex);
    }

    private List<String> answerTextToStringList(String answerText) {
        List<String> output = new ArrayList<>();
        for (int i = 0; i < answerText.length(); i += 4000) {
            if (i + 4000 > answerText.length()) {
                output.add(answerText.substring(i));
            } else {
                output.add(answerText.substring(i, i + 4000));
            }
        }
        return output;
    }

    private boolean isDigit(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String fileNameToLat(String fileName) {
        Map<String, String> map = Map.ofEntries(
                entry("а", "a"),
                entry("б", "b"),
                entry("в", "v"),
                entry("г", "g"),
                entry("д", "d"),
                entry("е", "e"),
                entry("ё", "yo"),
                entry("ж", "zh"),
                entry("з", "z"),
                entry("и", "i"),
                entry("й", "j"),
                entry("к", "k"),
                entry("л", "l"),
                entry("м", "m"),
                entry("н", "n"),
                entry("о", "o"),
                entry("п", "p"),
                entry("р", "r"),
                entry("с", "s"),
                entry("т", "t"),
                entry("у", "u"),
                entry("ф", "f"),
                entry("х", "h"),
                entry("ц", "ts"),
                entry("ч", "ch"),
                entry("ш", "sh"),
                entry("ъ", "'"),
                entry("ы", "i"),
                entry("ь", "'"),
                entry("э", "e"),
                entry("ю", "yu"),
                entry("я", "ya"),
                entry("№", "nomer"));
        String answer = "";
        fileName = fileName.toLowerCase();
        for (char c : fileName.toCharArray()) {
            if (map.containsKey(String.valueOf(c))) {
                answer += map.get(String.valueOf(c));
            } else {
                answer += String.valueOf(c);
            }
        }
        return answer;
    }
}
