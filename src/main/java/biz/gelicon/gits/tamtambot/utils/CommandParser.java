package biz.gelicon.gits.tamtambot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class CommandParser {
    private final String COMMAND_PREFIX = "/";

    public ParsedCommand getParsedCommand(String messageText) {
        ParsedCommand result = new ParsedCommand(Command.NONE, messageText);
        if (messageText.isEmpty()) {
            return result;
        }
        Command command = Command.NONE;
        int startCommandIndex = messageText.indexOf(COMMAND_PREFIX);
        int endCommandIndex = messageText.indexOf(" ", startCommandIndex);
        if (endCommandIndex == -1) {
            endCommandIndex = messageText.length();
        }
        String strCommand = messageText.substring(startCommandIndex + 1, endCommandIndex).toUpperCase();
        try {
            command = Command.valueOf(strCommand);
        } catch (IllegalArgumentException e) {
            log.debug("Can't parse command: " + messageText);
        }
        if (endCommandIndex >= messageText.length()) {
            result.setCommand(command);
            result.setText(null);
        } else {
            String text = messageText.substring(endCommandIndex).trim();
            result.setCommand(command);
            result.setText(text);
        }
        return result;
    }

    public int getNumberFromShowCommand(String command) {
        String numStr = command.replaceAll("\\D+","");
        if (numStr.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(numStr);
    }
}
