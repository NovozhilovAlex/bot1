package biz.gelicon.gits.tamtambot.controller;

import biz.gelicon.gits.tamtambot.dto.CommandDto;
import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueAppendix;
import biz.gelicon.gits.tamtambot.entity.IssueStatus;
import biz.gelicon.gits.tamtambot.exceptions.AlreadyAuthException;
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
import chat.tamtam.botapi.queries.SendMessageQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

@Controller
@Slf4j
public class UpdateController {
    private final IssueService issueService;
    private final WorkerService workerService;
    private final ProguserChatService proguserChatService;
    private final CommandParser commandParser;
    private final AnswerFormatter answerFormatter;
    private final FileCompressor compressor;
    private final Utils utils;
    private TamtamBot tamtamBot;
    @Value("${spring.datasource.url}")
    private String dbUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public UpdateController(IssueService issueService, WorkerService workerService,
                            ProguserChatService proguserChatService, CommandParser commandParser,
                            AnswerFormatter answerFormatter, FileCompressor compressor, Utils utils,
                            RestTemplateBuilder restTemplateBuilder) {
        this.issueService = issueService;
        this.workerService = workerService;
        this.proguserChatService = proguserChatService;
        this.commandParser = commandParser;
        this.answerFormatter = answerFormatter;
        this.compressor = compressor;
        this.utils = utils;
        this.restTemplate = restTemplateBuilder.build();
    }

    public void registerBot(TamtamBot tamtamBot) {
        this.tamtamBot = tamtamBot;
    }

    @Transactional
    public void processShowCommand(Message message) throws ClientException {
        log.info("User with name " + message.getSender().getUsername() + " sent /show command");
        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }

        String userId = String.valueOf(message.getSender().getUserId());
        if (!isUserIdAuthorized(userId)) {
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "У Вас нет доступа"));
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
        String issueId;
        if (commandArgs[0].charAt(0) == '#' && utils.isDigit(commandArgs[0].substring(1))) {
            issueId = commandArgs[0].substring(1);
        } else if (commandArgs[0].startsWith("GITS-") && utils.isDigit(commandArgs[0].substring(5))) {
            issueId = commandArgs[0].substring(5);
        } else if (utils.isDigit(commandArgs[0])) {
            issueId = commandArgs[0];
        } else {
            log.debug("show command argument not valid");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Неверный синтаксис для команды /show"));
            processHelpCommand(message);
            return;
        }

        log.info("Try to get issue info by id = " + issueId);
        try {
            Issue issue = issueService.getIssueById(Integer.parseInt(issueId));
            String answerText;
            if (commandArgs.length == 1) {
                answerText = answerFormatter.getAnswerForShowCommand(issue);
            } else if (commandArgs.length == 2 && Objects.equals(commandArgs[1].trim(), "short")) {
                IssueStatus issueStatus = issueService.getIssueStatusByIssueId(issue.getIssueId());
                answerText = answerFormatter.getAnswerForShortShowCommand(issueStatus, issue);
            } else {
                log.debug("show command argument not valid");
                tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                        "Неверный синтаксис для команды /show"));
                processHelpCommand(message);
                return;
            }

            AttachmentsCarrier attachmentsCarrier = createAttachments(issue.getIssueAppendices());
            if (answerText.length() > 4000) {
                List<String> answerTextList = utils.answerTextToStringList(answerText);
                for (int i = 0; i < answerTextList.size() - 1; i++) {
                    NewMessageBody answer = NewMessageBodyBuilder
                            .ofText(answerTextList.get(i), TextFormat.HTML)
                            .build();
                    SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                            .chatId(message.getRecipient().getChatId());
                    createSendMessageWithAttachmentsQuery(query);
                }
                NewMessageBody answer = NewMessageBodyBuilder
                        .ofText(answerTextList.get(answerTextList.size() - 1), TextFormat.HTML)
                        .withAttachments(attachmentsCarrier.getImages())
                        .build();
                SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                        .chatId(message.getRecipient().getChatId());
                createSendMessageWithAttachmentsQuery(query);
            } else {
                NewMessageBody answer = NewMessageBodyBuilder
                        .ofText(answerText, TextFormat.HTML)
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

            for (String uncPath : attachmentsCarrier.getUncPaths()) {
                NewMessageBody answer = NewMessageBodyBuilder
                        .ofText(answerFormatter.getLinkAnswer(uncPath), TextFormat.MARKDOWN)
                        .build();
                SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                        .chatId(message.getRecipient().getChatId());
                tamtamBot.sendAnswerMessage(query);
            }
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Задачи с id = " + issueId + " не существует"));
        } catch (APIException | IOException | InterruptedException | RuntimeException e) {
            log.warn(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    answerFormatter.getAnswerOnError(e)));
        }
    }

    public void processBotStartedUpdate(BotStartedUpdate update) throws ClientException {
        NewMessageBody answer = NewMessageBodyBuilder
                .ofText("Введите команду /auth {логин от gits} {пароль от gits} для аутентификации")
                .build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer).chatId(update.getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processHelpCommand(Message message) throws ClientException {
        CallbackButton btn = new CallbackButton("btn pressed", "Все задачи");
        NewMessageBody answer = NewMessageBodyBuilder.ofText("Доступные команды:\n" +
                        "/auth {логин от gits} {пароль от gits} - Аутентификация\n" +
                        "/logout - Выход\n" +
                        "/help - Список команд\n" +
                        "/show {номер_задачи} или /show GITS-{номер_задачи} - Содержание задачи\n" +
                        "/show {номер_задачи} short - Краткое содержание задачи\n" +
                        "/inbox - Список всех Ваших задач\n\n" +
                        "Для доступа к вложениям, прикрепленным в виде" +
                        " unc-пути, скопируйте их путь, вставьте в строку Быстрый доступ в проводнике и нажмите Enter")
                .withAttachments(AttachmentsBuilder
                        .inlineKeyboard(InlineKeyboardBuilder
                                .singleRow(btn)))
                .build();
        SendMessageQuery query = new SendMessageQuery(tamtamBot.getClient(), answer)
                .chatId(message.getRecipient().getChatId());
        tamtamBot.sendAnswerMessage(query);
    }

    public void processAuthCommand(Message message) throws ClientException {
        log.info("User with name " + message.getSender().getUsername() + " sent /auth command");
        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }

        String userId = String.valueOf(message.getSender().getUserId());
        ParsedCommand parsedCommand = commandParser.getParsedCommand(messageText);
        if (parsedCommand.getText() == null) {
            log.debug("Auth command arguments is null");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Команда /auth должна иметь аргументы (логин и пароль)"));
            return;
        }

        String[] commandArgs = parsedCommand.getText().split(" ");
        if (commandArgs.length != 2) {
            log.debug("Auth command arguments != 2");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Команда /auth должна иметь два аргумента (логин и пароль)"));
            return;
        }

        String username = commandArgs[0].trim().toUpperCase();
        String password = commandArgs[1].trim();
        log.info("Try to auth with username " + username + " and password " + password);
        try {
            Connection connection = DriverManager.getConnection(dbUrl, username, password);
            connection.close();
            proguserChatService.insertProguserChat(username, userId);
            log.debug("User with userId = " + userId + " has been authorized");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Вы успешно авторизовались"));
            processHelpCommand(message);
        } catch (SQLException e) {
            log.debug("Incorrect password");
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Неверный логин или пароль"));
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Proguser с именем " + username + " не найден"));
        } catch (AlreadyAuthException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Вы уже аутентифицированы"));
        }
    }

    public void processLogoutCommand(Message message) throws ClientException {
        log.info("User with name " + message.getSender().getUsername() + " sent /logout command");
        String userId = String.valueOf(message.getSender().getUserId());
        log.info("Try to delete ProguserChat with userId = " + userId);
        try {
            proguserChatService.deleteProguserChat(userId);
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Вы успешно вышли"));
            log.debug("ProguserChat with userId = " + userId + " has been deleted");
        } catch (ResourceNotFoundException e) {
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Вы успешно вышли"));
            log.debug(e.getMessage());
        }

    }

    public void processInboxCommand(Message message) throws ClientException {
        log.info("User with name " + message.getSender().getUsername() + " sent /inbox command");
        String messageText = message.getBody().getText();
        if (messageText == null) {
            log.debug("Message text is null");
            return;
        }

        String userId = String.valueOf(message.getSender().getUserId());
        if (!isUserIdAuthorized(userId)) {
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "У Вас нет доступа"));
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
            List<Issue> issues = workerService.getIssuesByUserId(userId);
            if (issues.isEmpty()) {
                tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                        "У Вас сейчас нет задач"));
                return;
            }
            String answerText = answerFormatter.getAnswerForInboxCommand(issues);
            if (answerText.length() > 4000) {
                List<String> answerTextList = utils.answerTextToStringList(answerText);
                for (String s : answerTextList) {
                    tamtamBot.sendAnswerMessage(createSendMessageQuery(
                            message.getRecipient().getChatId(), s));
                }
            } else {
                tamtamBot.sendAnswerMessage(createSendMessageQuery(
                        message.getRecipient().getChatId(), answerText));
            }
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    "Не удалось найти ваш юзернейм в бд"));
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(message.getRecipient().getChatId(),
                    answerFormatter.getAnswerOnError(e)));
        }
    }

    public void processButtonPressed(MessageCallbackUpdate update) throws ClientException {
        log.info("User with name " + update.getCallback().getUser().getUsername() + " pressed AllIssues button");
        String userId = String.valueOf(update.getCallback().getUser().getUserId());
        if (!isUserIdAuthorized(userId)) {
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "У Вас нет доступа"));
            return;
        }

        try {
            List<Issue> issues = workerService.getIssuesByUserId(userId);
            if (issues.isEmpty()) {
                tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                        "У Вас сейчас нет задач"));
                return;
            }
            String answerText = answerFormatter.getAnswerForInboxCommand(issues);
            if (answerText.length() > 4000) {
                List<String> answerTextList = utils.answerTextToStringList(answerText);
                for (String s : answerTextList) {
                    tamtamBot.sendAnswerMessage(createSendMessageQuery(
                            update.getMessage().getRecipient().getChatId(), s));
                }
            } else {
                tamtamBot.sendAnswerMessage(createSendMessageQuery(
                        update.getMessage().getRecipient().getChatId(), answerText));
            }
        } catch (ResourceNotFoundException e) {
            log.debug(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "Не удалось найти ваш юзернейм в бд"));
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    answerFormatter.getAnswerOnError(e)));
        }
    }

    @Transactional
    public void processMessageCreatedUpdate(MessageCreatedUpdate update) throws ClientException {
        if (update.getMessage().getRecipient().getChatType() != ChatType.DIALOG) {
            return;
        }
        String command = update.getMessage().getBody().getText();

        String url = "http://127.0.0.1:8000/class/";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> map = new HashMap<>();
        map.put("text", command);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<CommandDto> response = this.restTemplate.postForEntity(url, entity, CommandDto.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn(String.valueOf(response.getStatusCode()));
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "Попробуйте использовать команду, а не сообщение"));
            return;
        }
        float showConf = Objects.requireNonNull(response.getBody()).getShowConf();
        float allConf = Objects.requireNonNull(response.getBody()).getAllConf();
        float logoutConf = Objects.requireNonNull(response.getBody()).getLogoutConf();
        float helpConf = Objects.requireNonNull(response.getBody()).getHelpConf();
        String commandText = response.getBody().getText();
        List<Float> confList = List.of(showConf, allConf, logoutConf, helpConf);
        float maxConf = Collections.max(confList);
        if (maxConf < 0.75f) {
            tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                    "Команда не распознана"));
            return;
        }
        Message message = update.getMessage();
        switch (confList.indexOf(maxConf)) {
            case (0):
                int issueId = commandParser.getNumberFromShowCommand(commandText);
                if (issueId == -1) {
                    tamtamBot.sendAnswerMessage(createSendMessageQuery(update.getMessage().getRecipient().getChatId(),
                            "Для предоставления информации о задаче необходимо передать ее идентификатор"));
                    return;
                }
                MessageBody messageBodyShow = new MessageBody(message.getBody().getMid(), message.getBody().getSeq(),
                        "/show #" + issueId, message.getBody().getAttachments());
                Message newMessageShow = new Message(message.getRecipient(), message.getTimestamp(), messageBodyShow);
                newMessageShow.setSender(message.getSender());
                processShowCommand(newMessageShow);
                break;
            case (1):
                MessageBody messageBodyAll = new MessageBody(message.getBody().getMid(), message.getBody().getSeq(),
                        "/inbox", message.getBody().getAttachments());
                Message newMessageAll = new Message(message.getRecipient(), message.getTimestamp(), messageBodyAll);
                newMessageAll.setSender(message.getSender());
                processInboxCommand(newMessageAll);
                break;
            case (2):
                MessageBody messageBodyLogout = new MessageBody(message.getBody().getMid(), message.getBody().getSeq(),
                        "/logout", message.getBody().getAttachments());
                Message newMessageLogout = new Message(message.getRecipient(), message.getTimestamp(),
                        messageBodyLogout);
                newMessageLogout.setSender(message.getSender());
                processLogoutCommand(newMessageLogout);
                break;
            case (3):
                MessageBody messageBodyHelp = new MessageBody(message.getBody().getMid(), message.getBody().getSeq(),
                        "/help", message.getBody().getAttachments());
                Message newMessageHelp = new Message(message.getRecipient(), message.getTimestamp(), messageBodyHelp);
                newMessageHelp.setSender(message.getSender());
                processHelpCommand(newMessageHelp);
                break;
        }

//        processHelpCommand(update.getMessage());
    }

    private SendMessageQuery createSendMessageQuery(Long chatId, String text) {
        NewMessageBody answer = NewMessageBodyBuilder.ofText(text, TextFormat.HTML).build();
        return new SendMessageQuery(tamtamBot.getClient(), answer).chatId(chatId);
    }

    private void createSendMessageWithAttachmentsQuery(SendMessageQuery query) throws InterruptedException, ClientException {
        boolean flag = true;
        while (flag) {
            log.info("Try to send message with attachment");
            Thread.sleep(500);
            try {
                query.execute();
                flag = false;
            } catch (ClientException | APIException e) {
                if (!e.getMessage().contains("You cannot send message with unprocessed attachment")) {
                    flag = false;
                }
                log.warn(e.getMessage());
            } catch (RuntimeException e) {
                log.warn(e.getMessage());
                tamtamBot.sendAnswerMessage(createSendMessageQuery(query.chatId.getValue(),
                        answerFormatter.getAnswerOnError(e)));
            }
        }
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
        List<String> uncPaths = new ArrayList<>();
        for (IssueAppendix appendix : appendices) {
            if (appendix.getIssueAppendixLinkpath() != null) {
                uncPaths.add(appendix.getIssueAppendixLinkpath());
                continue;
            }

            byte[] content = appendix.getIssueAppendixContent();
            if (content == null) {
                continue;
            }

            String name = appendix.getIssueAppendixName();
            String format = utils.cutFileFormat(name);
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            File file = File.createTempFile(utils.fileNameToLat(utils.cutFileName(name)), "." + format);
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
        return new AttachmentsCarrier(builder, uploadedInfos, uncPaths);
    }

    private boolean isUserIdAuthorized(String userId) {
//        if (proguserChatService.isProguserChatFindByUserId(userId)) {
//            return true;
//        } else {
//            log.debug("Unauthorized user, userId = " + userId);
//            return false;
//        }
        return true;
    }
}
