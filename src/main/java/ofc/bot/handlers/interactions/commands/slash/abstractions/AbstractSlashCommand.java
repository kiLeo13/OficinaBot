package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.Permission;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.exceptions.CommandCreationException;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractSlashCommand implements ICommand<SlashCommandContext> {
    // Mapping <UserID, LastUsed>
    private final Map<Long, Long> cooldownMapper;
    private final String fullName;
    private final String description;
    private final Permission permission;
    private final int cooldown;

    public AbstractSlashCommand() {
        this.fullName = getValue(DiscordCommand::name).replace("/", "");
        this.description = getValue(DiscordCommand::description);
        this.permission = getValue(DiscordCommand::permission);
        this.cooldown = getValue(DiscordCommand::cooldown);
        this.cooldownMapper = this.cooldown == 0 ? Collections.emptyMap() : new HashMap<>();

        // If the full qualified name contains spaces but is NOT a subcommand...
        // This is not allowed
        if (isCompoundName() && !isSubcommand())
            throw new IllegalStateException("Comand names cannot contain spaces");

        if (this.cooldown < 0)
            throw new CommandCreationException("Command cooldown may not be negative, provided: " + this.cooldown);
    }

    public AbstractSlashCommand(String fullName, String description, Permission permission, int cooldown) {
        this.fullName = fullName;
        this.description = description;
        this.permission = permission;
        this.cooldown = cooldown;
        this.cooldownMapper = this.cooldown == 0 ? Collections.emptyMap() : new HashMap<>();

        if (this.cooldown < 0)
            throw new CommandCreationException("Command cooldown may not be negative, provided: " + this.cooldown);
    }

    @Override
    public final String getQualifiedName() {
        return this.fullName;
    }

    @Override
    public final String getDescription() {
        return this.description;
    }

    @Override
    public Permission getPermission() {
        return this.permission == Permission.UNKNOWN ? null : this.permission;
    }

    @Override
    public final int getCooldown() {
        return this.cooldown;
    }

    @Override
    public long cooldownRemain(long userId) {
        int cooldown = getCooldown();
        long period = getUserCooldown(userId);

        return cooldown == 0 ? 0 : cooldown - period;
    }

    private boolean hasCooldown() {
        return this.cooldown > 0;
    }

    @Override
    public final void tickCooldown(long userId) {
        if (!hasCooldown()) return;

        this.cooldownMapper.put(userId, Bot.unixNow());
    }

    /*
     * Returns how many seconds have passed since the user ran the command
     * for the last time.
     *
     * This method will always return "Long.MAX_VALUE" when either:
     *
     * - No cooldown is set for this command.
     * - The user issued this command for the first time.
     */
    private long getUserCooldown(long userId) {
        if (cooldown == 0)
            return Long.MAX_VALUE;

        Long lastUsed = cooldownMapper.get(userId);
        long now = Bot.unixNow();

        if (lastUsed == null)
            return Long.MAX_VALUE;
        return now - lastUsed;
    }

    private <T> T getValue(Function<DiscordCommand, T> mapper) {
        Class<? extends AbstractSlashCommand> clazz = this.getClass();
        DiscordCommand annotation = clazz.getDeclaredAnnotation(DiscordCommand.class);

        if (annotation == null)
            throw new CommandCreationException("Commands must be annotated with @" + DiscordCommand.class.getSimpleName());

        return mapper.apply(annotation);
    }

    private boolean isCompoundName() {
        return this.fullName.split(" ").length > 1;
    }

    private boolean isSubcommand() {
        return (this instanceof SlashSubcommand);
    }
}
