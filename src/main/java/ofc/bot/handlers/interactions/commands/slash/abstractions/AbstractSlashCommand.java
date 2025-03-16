package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.handlers.interactions.commands.Cooldown;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.exceptions.CommandCreationException;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractSlashCommand implements ICommand<SlashCommandContext> {
    private final String fullName;
    private final Permission permission;
    private final List<OptionData> options;
    private String description;
    private Cooldown cooldown;

    public AbstractSlashCommand() {
        this.fullName = getValue(DiscordCommand::name);
        this.permission = getValue(DiscordCommand::permission);
        this.options = new ArrayList<>();
        this.cooldown = Cooldown.EMPTY;

        // If the full qualified name contains spaces but is NOT a subcommand...
        // This is not allowed
        if (isCompoundName() && !isSubcommand())
            throw new IllegalStateException("Comand names cannot contain spaces");

        init();
    }

    public AbstractSlashCommand(String fullName, String description, Permission permission) {
        this.fullName = fullName;
        this.description = description;
        this.permission = permission;
        this.options = new ArrayList<>();
        this.cooldown = Cooldown.EMPTY;
    }

    /**
     * Initializes options, description and other properties of a command.
     * <p>
     * Only called in the empty parameters constructor.
     */
    protected abstract void init();

    @NotNull
    @Override
    public final String getQualifiedName() {
        return this.fullName;
    }

    @NotNull
    @Override
    public final String getDescription() {
        return this.description;
    }

    @Override
    public Permission getPermission() {
        return this.permission == Permission.UNKNOWN ? null : this.permission;
    }

    @NotNull
    @Override
    public final List<OptionData> getOptions() {
        return this.options;
    }

    @NotNull
    @Override
    public final Cooldown getCooldown() {
        return this.cooldown;
    }

    protected final void setDesc(@NotNull String desc) {
        this.description = desc;
    }

    protected final void setCooldown(boolean staffBypass, int period, TimeUnit unit) {
        this.cooldown = new Cooldown(staffBypass, unit.toSeconds(period));
    }

    protected final void setCooldown(int period, TimeUnit unit) {
        this.setCooldown(true, period, unit);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc,
                                boolean required, boolean autoComplete, double minRange, double maxRange) {
        OptionData opt = new OptionData(type, name, desc, required, autoComplete)
                .setRequiredRange(minRange, maxRange);

        this.options.add(opt);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc,
                                boolean required, boolean autoComplete, long minRange, long maxRange) {
        OptionData opt = new OptionData(type, name, desc, required, autoComplete);
        switch (type) {
            case INTEGER, NUMBER -> opt.setRequiredRange(minRange, maxRange);
            case STRING -> opt.setRequiredLength((int) minRange, (int) maxRange);
            default -> throw new IllegalArgumentException("Ranges are not allowed for Option Type: " + type);
        }
        this.options.add(opt);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc,
                                boolean required, boolean autoComplete) {
        this.options.add(new OptionData(type, name, desc, required, autoComplete));
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc, boolean required) {
        this.addOpt(type, name, desc, required, false);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc, boolean required,
                                long minRange, long maxRange) {
        this.addOpt(type, name, desc, required, false, minRange, maxRange);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc, boolean required,
                                double minRange, double maxRange) {
        this.addOpt(type, name, desc, required, false, minRange, maxRange);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc,
                                double minRange, double maxRange) {
        this.addOpt(type, name, desc, false, false, minRange, maxRange);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc,
                                long minRange, long maxRange) {
        this.addOpt(type, name, desc, false, false, minRange, maxRange);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name, @NotNull String desc) {
        this.addOpt(type, name, desc, false, false);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name,
                                @NotNull String desc, boolean required, @NotNull Consumer<OptionData> mod) {
        OptionData opt = new OptionData(type, name, desc, required);
        mod.accept(opt);
        this.options.add(opt);
    }

    protected final void addOpt(@NotNull OptionType type, @NotNull String name,
                                @NotNull String desc, @NotNull Consumer<OptionData> mod) {
        this.addOpt(type, name, desc, false, mod);
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
