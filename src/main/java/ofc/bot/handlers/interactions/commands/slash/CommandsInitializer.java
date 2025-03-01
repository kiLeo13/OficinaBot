package ofc.bot.handlers.interactions.commands.slash;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import ofc.bot.Main;
import ofc.bot.commands.*;
import ofc.bot.commands.additionals.AdditionalRolesCommand;
import ofc.bot.commands.birthday.*;
import ofc.bot.commands.economy.*;
import ofc.bot.commands.groups.*;
import ofc.bot.commands.groups.channel.CreateGroupChannelCommand;
import ofc.bot.commands.groups.member.AddGroupMemberCommand;
import ofc.bot.commands.groups.member.RemoveGroupMemberCommand;
import ofc.bot.commands.levels.*;
import ofc.bot.commands.moderation.*;
import ofc.bot.commands.policies.AddPolicyCommand;
import ofc.bot.commands.policies.RemovePolicyCommand;
import ofc.bot.commands.relationships.*;
import ofc.bot.commands.relationships.marriages.*;
import ofc.bot.commands.stafflist.RefreshStaffListMessageCommand;
import ofc.bot.commands.stafflist.StaffListMessagesRegenerateCommand;
import ofc.bot.commands.userinfo.UserinfoCommand;
import ofc.bot.commands.userinfo.custom.*;
import ofc.bot.domain.sqlite.repository.*;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.handlers.interactions.commands.slash.dummy.EmptySlashCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class CommandsInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsInitializer.class);

    /**
     * This method instantiates all slash commands and also
     * sends them to Discord.
     */
    public static void initializeSlashCommands() {
        var modActRepo  = Repositories.getAutomodActionRepository();
        var bankTrRepo  = Repositories.getBankTransactionRepository();
        var bdayRepo    = Repositories.getBirthdayRepository();
        var csinfoRepo  = Repositories.getCustomUserinfoRepository();
        var policyRepo  = Repositories.getEntityPolicyRepository();
        var bckpRepo    = Repositories.getFormerMemberRoleRepository();
        var grpBotRepo  = Repositories.getGroupBotRepository();
        var lvlRoleRepo = Repositories.getLevelRoleRepository();
        var marrRepo    = Repositories.getMarriageRepository();
        var mreqRepo    = Repositories.getMarriageRequestRepository();
        var emjRepo     = Repositories.getMemberEmojiRepository();
        var pnshRepo    = Repositories.getMemberPunishmentRepository();
        var grpRepo     = Repositories.getOficinaGroupRepository();
        var tmpBanRepo  = Repositories.getTempBanRepository();
        var ecoRepo     = Repositories.getUserEconomyRepository();
        var namesRepo   = Repositories.getUserNameUpdateRepository();
        var userRepo    = Repositories.getUserRepository();
        var xpRepo      = Repositories.getUserXPRepository();

        // Additionals
        SlashCommand additionals = new EmptySlashCommand("additionals", "Gerencia recursos adicionais/misc do bot.", Permission.MANAGE_SERVER)
                .addSubcommand(new AdditionalRolesCommand());

        // Birhday
        SlashCommand birthday = new EmptySlashCommand("birthday", "Gerencia os aniversários.", Permission.MANAGE_SERVER)
                .addSubcommand(new BirthdayAddCommand(bdayRepo, policyRepo))
                .addSubcommand(new BirthdayRemoveCommand(bdayRepo));

        // Policies
        SlashCommand policies = new EmptySlashCommand("policies", "Gerencia as regras dos módulos do bot.", Permission.MANAGE_SERVER)
                .addSubcommand(new AddPolicyCommand(policyRepo))
                .addSubcommand(new RemovePolicyCommand(policyRepo));

        // Marriage
        SlashCommand marriage = new EmptySlashCommand("marriage", "Gerencia os seus casamentos.")
                .addSubcommand(new MarriageAcceptCommanad(mreqRepo, marrRepo, ecoRepo))
                .addSubcommand(new CancelProposalCommand(mreqRepo))
                .addSubcommand(new ProposalsListCommand(mreqRepo))
                .addSubcommand(new MarriageRejectCommand(mreqRepo));

        // Custom Userinfo
        SlashCommand customizeUserinfo = new EmptySlashCommand("customize", "Customize o seu userinfo.", Permission.MANAGE_SERVER)
                .addSubcommand(new ResetUserinfoCommand(csinfoRepo))
                .addSubcommand(new SetUserinfoColorCommand(csinfoRepo))
                .addSubcommand(new SetDescriptionCommand(csinfoRepo))
                .addSubcommand(new SetUserinfoFooterCommand(csinfoRepo));

        // Groups
        SlashCommand group = new EmptySlashCommand("group", "Tenha o controle de tudo sobre o seu grupo.")
                .addGroups(
                        new SubcommandGroup("channel", "Gerencie os canais do seu grupo.")
                                .addSubcommand(new CreateGroupChannelCommand(grpRepo)),

                        new SubcommandGroup("member", "Gerencie os membros do seu grupo.")
                                .addSubcommand(new AddGroupMemberCommand(bankTrRepo, grpRepo))
                                .addSubcommand(new RemoveGroupMemberCommand(grpRepo))
                )
                .addSubcommand(new CreateGroupCommand(grpRepo))
                .addSubcommand(new GroupBotsCommand(grpBotRepo, grpRepo))
                .addSubcommand(new GroupInfoCommand(bankTrRepo, grpRepo))
                .addSubcommand(new GroupPayInvoiceCommand(grpRepo))
                .addSubcommand(new GroupPermissionCommand(grpRepo, policyRepo))
                .addSubcommand(new GroupPinsCommand(grpRepo))
                .addSubcommand(new HelpGroupCommand())
                .addSubcommand(new LeaveGroupCommand(grpRepo))
                .addSubcommand(new ModifyGroupCommand(grpRepo));

        List<SlashCommand> cmds = List.of(
                // Compound Commands
                additionals,
                birthday,
                policies,
                marriage,
                customizeUserinfo,
                group,

                // Administration
                new DisconnectAllCommand(),
                new MoveAllCommand(),
                new NamesHistoryCommand(namesRepo),

                // Birthdays
                new BirthdaysCommand(bdayRepo),

                // Economy
                new DailyCommand(ecoRepo),
                new BalanceCommand(ecoRepo),
                new LeaderboardCommand(ecoRepo),
                new PayCommand(ecoRepo, userRepo),
                new SetMoneyCommand(ecoRepo),
                new TransactionsCommand(),
                new UpdateMoneyCommand(ecoRepo),
                new WorkCommand(ecoRepo),

                // Levels
                new LevelsCommand(xpRepo),
                new LevelsRolesCommand(lvlRoleRepo),
                new RankCommand(xpRepo, lvlRoleRepo),

                // Moderation
                new BanCommand(tmpBanRepo),
                new InfractionsCommand(),
                new KickCommand(),
                new MuteCommand(),
                new UnbanCommand(),
                new UnmuteCommand(),
                new WarnCommand(pnshRepo, modActRepo),

                // Relationships
                new DivorceCommand(marrRepo),
                new MarryCommand(mreqRepo, ecoRepo, marrRepo, userRepo),
                new UpdateMarriageCreationCommand(marrRepo),

                // Staff List
                new StaffListMessagesRegenerateCommand(),
                new RefreshStaffListMessageCommand(policyRepo),

                // Userinfo
                new UserinfoCommand(csinfoRepo, emjRepo, ecoRepo, marrRepo, grpRepo),

                // Generic
                new AvatarCommand(),
                new BackupMemberRolesCommand(bckpRepo),
                new BotStatusCommand(),
                new ClearMessagesCommand(),
                new CreateChangelogEntryCommand(),
                new GuildInfoCommand(),
                new GuildLogoCommand(),
                new IPLookupCommand(),
                new MovieInstructionsCommand(),
                new RoleAmongUsCommand(),
                new RoleInfoCommand(),
                new RoleMembersCommand(),
                new ToggleEventsCommand()
        );

        SlashCommandsRegistryManager.register(cmds);
        pushCommands(cmds);
    }

    private static void pushCommands(List<SlashCommand> cmds) {
        JDA api = Main.getApi();
        List<SlashCommandData> slashCmds = cmds.stream()
                .map(SlashCommand::build)
                .toList();

        api.updateCommands()
                .addCommands(slashCmds)
                .queue(CommandsInitializer::printTree,
                        (err) -> LOGGER.error("Failed to create slash commands", err)
                );
    }

    public static void printTree(List<Command> commands) {
        printTree(commands, "", true);
    }

    private static void printTree(List<Command> commands, String prefix, boolean isLast) {
        for (int i = 0; i < commands.size(); i++) {
            Command command = commands.get(i);
            boolean lastCommand = i == commands.size() - 1;
            String newPrefix = prefix + (isLast ? "└───" : "├───");
            LOGGER.info("{}{}", newPrefix, command.getName());

            if (!command.getSubcommands().isEmpty()) {
                printSubcommands(command.getSubcommands(), prefix + (isLast ? "    " : "│   "), lastCommand);
            }

            if (!command.getSubcommandGroups().isEmpty()) {
                printSubcommandGroups(command.getSubcommandGroups(), prefix + (isLast ? "    " : "│   "), lastCommand);
            }

        }
    }

    private static void printSubcommandGroups(List<Command.SubcommandGroup> subcommandGroups, String prefix, boolean isLast) {
        for (int i = 0; i < subcommandGroups.size(); i++) {
            Command.SubcommandGroup group = subcommandGroups.get(i);
            boolean lastGroup = i == subcommandGroups.size() - 1;
            String newPrefix = prefix + (isLast ? "└───" : "├───");
            LOGGER.info("{}{} (group)", newPrefix, group.getName());

            if (!group.getSubcommands().isEmpty()) {
                printSubcommands(group.getSubcommands(), prefix + (isLast ? "    " : "│   "), lastGroup);
            }
        }
    }

    private static void printSubcommands(List<Command.Subcommand> subcommands, String prefix, boolean isLast) {
        for (Command.Subcommand subcommand : subcommands) {
            String newPrefix = prefix + (isLast ? "└───" : "├───");
            LOGGER.info("{}{} (subcommand)", newPrefix, subcommand.getName());
        }
    }
}