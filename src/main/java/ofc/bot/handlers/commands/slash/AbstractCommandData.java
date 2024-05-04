package ofc.bot.handlers.commands.slash;

import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.handlers.commands.exceptions.CommandCreationException;
import ofc.bot.handlers.commands.slash.containers.CommandOptionsContainer;
import ofc.bot.util.Bot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractCommandData extends CommandOptionsContainer implements CommandExecutor {
    // Mapping <UserID, LastUsed>
    private final Map<Long, Long> cooldownMapper;
    private final String name;
    private final String description;
    private final boolean autoDefer;
    private final boolean deferEphemeral;
    private final int cooldown;

    public AbstractCommandData() {
        this.name = getValue(DiscordCommand::name);
        this.description = getValue(DiscordCommand::description);
        this.autoDefer = getValue(DiscordCommand::autoDefer);
        this.deferEphemeral = getValue(DiscordCommand::deferEphemeral);
        this.cooldown = getValue(DiscordCommand::cooldown);

        if (this.cooldown < 0)
            throw new CommandCreationException("Command cooldown may not be negative, provided: " + this.cooldown);

        this.cooldownMapper = this.cooldown == 0
                ? Collections.emptyMap()
                : new HashMap<>();
    }

    public final String getName() {
        return this.name;
    }

    public final String getDescription() {
        return this.description;
    }

    public final boolean isAutoDefer() {
        return this.autoDefer;
    }

    public final boolean isDeferEphemeral() {
        return this.deferEphemeral;
    }

    public final boolean hasCooldown() {
        return this.cooldown > 0;
    }

    public final int getCooldown() {
        return this.cooldown;
    }

    /**
     * Returns how many <u>seconds</u> have passed since the user ran the command
     * for the last time.
     * <p>
     * This method will always return {@link Long#MAX_VALUE} if at least one of the following conditions are met:
     * <ul>
     *     <li> No cooldown is set for this command. </li>
     *     <li> The user issued this command for the first time. </li>
     * </ul>
     *
     * @return the amount of time (in seconds) since the user
     * ran the command for the last time.
     */
    public final long getUserCooldown(long userId) {

        if (cooldown == 0)
            return Long.MAX_VALUE;

        Long lastUsed = cooldownMapper.get(userId);
        long now = Bot.unixNow();

        if (lastUsed == null)
            return Long.MAX_VALUE;

        return now - lastUsed;
    }

    public final void refreshCooldown(long userId) {

        if (!hasCooldown())
            return;

        this.cooldownMapper.put(userId, Bot.unixNow());
    }

    private <T> T getValue(Function<DiscordCommand, T> mapper) {

        Class<? extends AbstractCommandData> clazz = this.getClass();
        DiscordCommand annotation = clazz.getDeclaredAnnotation(DiscordCommand.class);

        if (annotation == null)
            throw new CommandCreationException("Subcommands must be annotated with " + DiscordCommand.class.getSimpleName());

        return mapper.apply(annotation);
    }
}