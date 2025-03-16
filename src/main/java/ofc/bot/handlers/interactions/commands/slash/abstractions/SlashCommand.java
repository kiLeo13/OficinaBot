package ofc.bot.handlers.interactions.commands.slash.abstractions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import ofc.bot.handlers.interactions.commands.slash.SubcommandGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        if (getPermission() != null) {
            slash.setDefaultPermissions(DefaultMemberPermissions.enabledFor(getPermission()));
        }
        return slash;
    }
}