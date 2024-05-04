package ofc.bot.handlers.commands.responses.results;

public interface CommandResult {

    String getContent();

    Status getStatus();

    CommandResult setEphm(boolean ephemeral);

    boolean isEphemeral();
}