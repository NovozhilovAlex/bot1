package biz.gelicon.gits.tamtambot;

import biz.gelicon.gits.tamtambot.controller.TamtamBot;
import chat.tamtam.bot.exceptions.TamTamBotException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TamtamBotApplication {
    public static void main(String[] args) throws TamTamBotException {
        ConfigurableApplicationContext context = SpringApplication.run(TamtamBotApplication.class, args);
        TamtamBot bot = context.getBean(TamtamBot.class);
        bot.start();
    }
}
