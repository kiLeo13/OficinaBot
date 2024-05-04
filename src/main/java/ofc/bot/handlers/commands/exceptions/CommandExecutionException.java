package ofc.bot.handlers.commands.exceptions;

public class CommandExecutionException extends RuntimeException {

    public CommandExecutionException(String message) {
        super(message);
    }
}