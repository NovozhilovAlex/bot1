package biz.gelicon.gits.tamtambot.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Component
public class Utils {
    public String cutFileFormat(String fileName) {
        int dotIndex = fileName.indexOf('.');
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == '.') {
                dotIndex = i;
            }
        }
        return fileName.substring(dotIndex + 1);
    }

    public String cutFileName(String fileName) {
        int dotIndex = fileName.indexOf('.');
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == '.') {
                dotIndex = i;
            }
        }
        return fileName.substring(0, dotIndex);
    }

    public List<String> answerTextToStringList(String answerText) {
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

    public boolean isDigit(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public String fileNameToLat(String fileName) {
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
                entry("щ", "sh"),
                entry("ъ", "'"),
                entry("ы", "i"),
                entry("ь", "'"),
                entry("э", "e"),
                entry("ю", "yu"),
                entry("я", "ya"),
                entry("№", "nomer"));
        StringBuilder answer = new StringBuilder();
        fileName = fileName.toLowerCase();
        for (char c : fileName.toCharArray()) {
            if (map.containsKey(String.valueOf(c))) {
                answer.append(map.get(String.valueOf(c)));
            } else {
                answer.append(c);
            }
        }
        return answer.toString();
    }
}
