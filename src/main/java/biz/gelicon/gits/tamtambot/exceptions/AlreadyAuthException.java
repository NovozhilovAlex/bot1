package biz.gelicon.gits.tamtambot.exceptions;

public class AlreadyAuthException extends RuntimeException {
    public AlreadyAuthException(String message) {
        super(message);
    }
}
