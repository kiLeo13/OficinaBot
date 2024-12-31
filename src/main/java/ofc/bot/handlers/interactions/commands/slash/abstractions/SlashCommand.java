package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import ofc.bot.handlers.interactions.commands.slash.SubcommandGroup;
import ofc.bot.handlers.interactions.commands.slash.dummy.EmptySlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents all SlashCommands.
 * For Subcommands, extend the {@link SlashSubcommand} abstract class.
 * <p>
 * This class should only be used for single base-commands.
 * If your command has subcommands and subcommand groups, you shall instead,
 * instantiate a new {@link EmptySlashCommand EmptySlashCommand}
 * and treat it as your SlashCommand instance, where you can set a
 * name, description, add subcommands and subcommand groups to it.
 * <p>
 * If you want to use a subcommand group, create a new {@link SubcommandGroup},
 * add all {@link SlashSubcommand subcommands} to it and pass it as parameter when calling
 * {@link #addGroup(SubcommandGroup)} on the base slash command instance.
 * <p>
 * Providing a {@code /} before the command name is optional and will be ignored,
 * you can use it for readability purposes if you want really to.
 * <p>
 * All commands must provide a valid {@link DiscordCommand#name() name}
 * and {@link DiscordCommand#description() description}.
 */
public abstract class SlashCommand extends AbstractSlashCommand {
    private final List<SlashSubcommand> subCmds;
    private final List<SubcommandGroup> groups;

    public SlashCommand() {
        super();
        this.groups = new ArrayList<>();
        this.subCmds = new ArrayList<>();
    }

    public SlashCommand(String name, String description, Permission permission, int cooldown) {
        super(name, description, permission, cooldown);
        this.groups = new ArrayList<>();
        this.subCmds = new ArrayList<>();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    /**
     * Whether this command has subcommands (this includes subcommand groups with subcommands).
     *
     * @return {@code true} if this instance has subcommands, {@code false} otherwise.
     */
    public boolean hasSubs() {
        return !subCmds.isEmpty() || !groups.isEmpty();
    }

    public final SlashCommand addGroup(SubcommandGroup group) {
        this.groups.add(group);
        return this;
    }

    public final SlashCommand addGroups(SubcommandGroup... groups) {
        for (SubcommandGroup group : groups) {
            addGroup(group.setSuperCommand(this));
        }
        return this;
    }

    public final List<SubcommandGroup> getGroups() {
        return this.groups;
    }

    public final SlashCommand addSubcommand(SlashSubcommand sub) {
        this.subCmds.add(sub);
        return this;
    }

    public final SlashCommand addSubcommands(SlashSubcommand... subs) {
        for (SlashSubcommand sub : subs) {
            addSubcommand(sub);
        }
        return this;
    }

    public final List<SlashSubcommand> getSubcommands() {
        return this.subCmds;
    }

    /**
     * This will deeply look for every subcommand in this tree, including
     * subcommands inside groups.
     *
     * @return the list of all subcommands held in this command.
     */
    public final List<SlashSubcommand> findAllSubcommands() {
        List<SlashSubcommand> subs = new ArrayList<>(this.subCmds);

        for (SubcommandGroup group : this.groups) {
            subs.addAll(group.getSubcommands());
        }
        return Collections.unmodifiableList(subs);
    }

    public final SlashCommandData build() {
        SlashCommandData slash = Commands.slash(getName(), getDescription())
                .addOptions(getOptions());

        for (SubcommandGroup group : getGroups()) {
            slash.addSubcommandGroups(group.build());
        }

        for (SlashSubcommand sub : getSubcommands()) {
            slash.addSubcommands(sub.build());
        }

        slash.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        if (getPermission() != null) {
            //slash.setDefaultPermissions(DefaultMemberPermissions.enabledFor(getPermission()));
        }
        return slash;
    }
}