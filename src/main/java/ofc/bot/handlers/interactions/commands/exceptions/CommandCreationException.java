package ofc.bot.handlers.interactions.commands.exceptions;

public class CommandCreationException extends RuntimeException {
    public CommandCreationException(String message) {
        super(message);
    }
}