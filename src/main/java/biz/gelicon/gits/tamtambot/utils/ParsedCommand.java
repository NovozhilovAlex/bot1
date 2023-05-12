package biz.gelicon.gits.tamtambot.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParsedCommand {
    private Command command;
    private String text;
}
