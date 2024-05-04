package ofc.bot.handlers.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.*;
import ofc.bot.content.annotations.commands.CommandGroup;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.slash.containers.SubcommandContainer;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommandGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class SlashCommand extends AbstractCommandData implements SubcommandContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommand.class);
    private final Permission permission;
    private final Map<String, SlashSubcommand> subCommands = new HashMap<>();
    private final Map<String, SlashSubcommandGroup> commandGroups;

    public SlashCommand(SlashSubcommand... subcommands) {
        super();

        this.permission = resolvePermission();
        this.commandGroups = resolveGroups();

        for (SlashSubcommand cmd : subcommands) {

            if (cmd.isInSubcommandGroup())
                sendSubcommandToGroup(cmd);
            else
                this.subCommands.put(cmd.getName(), cmd);
        }
    }

    // For commands that extend this class, the implementation of this method is optional
    // and will be ignored if Subcommands and Subcommand Groups are declared
    @Override
    public CommandResult onCommand(CommandContext ctx) {
        throw new UnsupportedOperationException("Command '" + ctx.toString() + "' must implement 'onCommand(CommandContext)' method");
    }

    @Override
    public Collection<? extends SlashSubcommand> getSubcommands() {
        return this.subCommands.values();
    }

    @Override
    public final SlashSubcommand getSubcommand(String name) {
        return this.subCommands.get(name);
    }

    public final SlashSubcommand getSubcommand(String groupName, String name) {

        SlashSubcommandGroup group = getSubcommandGroup(groupName);

        return group == null
                ? null
                : group.getSubcommand(name);
    }

    @Override
    public final boolean hasSubcommands() {
        return !subCommands.isEmpty();
    }

    public final boolean hasSubcommandGroups() {
        return !commandGroups.isEmpty();
    }

    public final Collection<? extends SlashSubcommandGroup> getSubcommandGroups() {
        return this.commandGroups.values();
    }

    public final SlashSubcommandGroup getSubcommandGroup(String name) {
        return this.commandGroups.get(name);
    }

    public final SlashCommandData toCommandData() {

        Collection<? extends SubcommandGroupData> subcommandGroups = this.getSubcommandGroupsData();
        Collection<? extends SubcommandData> subCommands = this.getSubcommandsData();
        SlashCommandData slashData = Commands.slash(getName(), getDescription());
        List<OptionData> options = this.getOptions();

        if (hasSubcommands())
            slashData.addSubcommands(subCommands);

        if (hasSubcommandGroups())
            slashData.addSubcommandGroups(subcommandGroups);

        slashData.addOptions(options);

        if (this.permission != null)
            slashData.setDefaultPermissions(DefaultMemberPermissions.enabledFor(this.permission));

        return slashData;
    }

    /**
     * Here is where the Subcommands that provided a valid {@link CommandGroup} value
     * will be sent to their respective Subcommand Groups.
     */
    private void sendSubcommandToGroup(SlashSubcommand cmd) {

        String groupName = cmd.getSubcommandGroupName();
        SlashSubcommandGroup group = commandGroups.get(groupName);

        if (group == null) {
            LOGGER.warn("Group for name '{}' in Subcommand '{}' was not found", groupName, cmd.getClass().getName());
            return;
        }

        group.addSubcommand(cmd);
    }

    private Collection<? extends SubcommandGroupData> getSubcommandGroupsData() {

        Collection<SlashSubcommandGroup> groups = commandGroups.values();

        return groups.stream()
                .map(SlashSubcommandGroup::getSubcommandGroupData)
                .toList();
    }

    private Map<String, SlashSubcommandGroup> resolveGroups() {

        List<Field> groupFields = findGroupFields();

        return groupFields.stream()
                .map(this::getFieldAsGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableMap(SlashSubcommandGroup::getName, g -> g));
    }

    private SlashSubcommandGroup getFieldAsGroup(Field field) {

        try {
            field.setAccessible(true);
            Object value = field.get(this);
            CommandGroup annotation = field.getDeclaredAnnotation(CommandGroup.class);

            if (value instanceof SlashSubcommandGroup group && annotation != null)
                return group;

            return null;
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not get value from field '" + field.getName() + "'", e);
            return null;
        } catch (IllegalStateException e) {
            LOGGER.error("Could not get annotation from option '" + field.getName() + "'", e);
            return null;
        }
    }

    private List<Field> findGroupFields() {

        Class<? extends SlashCommand> clazz = this.getClass();

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CommandGroup.class))
                .toList();
    }

    private Permission resolvePermission() {

        Class<? extends SlashCommand> clazz = this.getClass();
        CommandPermission annotation = clazz.getDeclaredAnnotation(CommandPermission.class);

        return annotation == null || annotation.value() == Permission.UNKNOWN
                ? null
                : annotation.value();
    }
}