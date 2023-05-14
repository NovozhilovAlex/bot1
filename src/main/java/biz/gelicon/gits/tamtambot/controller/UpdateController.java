package biz.gelicon.gits.tamtambot.controller;

import biz.gelicon.gits.tamtambot.entity.Issue;
import biz.gelicon.gits.tamtambot.entity.IssueAppendix;
import biz.gelicon.gits.tamtambot.exceptions.ResourceNotFoundException;
import biz.gelicon.gits.tamtambot.service.IssueService;
import biz.gelicon.gits.tamtambot.service.WorkerService;
import biz.gelicon.gits.tamtambot.utils.*;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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
                AttachmentsCarrier attachmentsCarrier = createAttachments(issue.getIssueAppendices());

                String answerText = answerFormatter.getAnswerForFindCommand(issue);
                if (answerFormatter.getAnswerForFindCommand(issue).length() > 4000) {
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
                            .ofText(answerFormatter.getAnswerForFindCommand(issue))
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
        return username.substring(8) + "@gelicon.biz";
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
        Map<String, String> map = new HashMap<>();
        map.put("а", "a");
        map.put("б", "b");
        map.put("в", "v");
        map.put("г", "g");
        map.put("д", "d");
        map.put("е", "e");
        map.put("ё", "yo");
        map.put("ж", "zh");
        map.put("з", "z");
        map.put("и", "i");
        map.put("й", "j");
        map.put("к", "k");
        map.put("л", "l");
        map.put("м", "m");
        map.put("н", "n");
        map.put("о", "o");
        map.put("п", "p");
        map.put("р", "r");
        map.put("с", "s");
        map.put("т", "t");
        map.put("у", "u");
        map.put("ф", "f");
        map.put("х", "h");
        map.put("ц", "ts");
        map.put("ч", "ch");
        map.put("ш", "sh");
        map.put("ъ", "'");
        map.put("ы", "i");
        map.put("ь", "'");
        map.put("э", "e");
        map.put("ю", "yu");
        map.put("я", "ya");
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
