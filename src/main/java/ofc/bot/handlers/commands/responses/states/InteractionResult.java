package ofc.bot.handlers.commands.responses.states;

public interface InteractionResult {
    String getContent();

    InteractionResult setEphm(boolean ephemeral);

    Status getStatus();

    boolean isEphemeral();

    boolean ticksCooldown();

    default boolean isOk() {
        return getStatus().isOk();
    }

    default Object[] getArgs() {
        return new Object[0];
    }
}