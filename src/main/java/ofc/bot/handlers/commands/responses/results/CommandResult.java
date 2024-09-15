package ofc.bot.handlers.commands.responses.results;

import java.util.Arrays;

public interface CommandResult {

    String getContent();

    Status getStatus();

    CommandResult setEphm(boolean ephemeral);

    boolean isEphemeral();

    default Object[] getArgs() {
        return new Object[0];
    }

    default String[] getStrArgs() {
        return Arrays.stream(getArgs())
                .map(Object::toString)
                .toArray(String[]::new);
    }
}