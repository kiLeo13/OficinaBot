package ofc.bot.handlers;

import net.dv8tion.jda.api.JDA;
import ofc.bot.Main;
import ofc.bot.commands.groups.LeaveGroupCommand;
import ofc.bot.domain.sqlite.repository.*;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.interactions.buttons.ButtonInteractionGateway;
import ofc.bot.handlers.interactions.commands.SlashCommandsGateway;
import ofc.bot.handlers.interactions.commands.slash.CommandsInitializer;
import ofc.bot.jobs.*;
import ofc.bot.jobs.QueryCountPrinter;
import ofc.bot.jobs.groups.GroupsInvoiceHandler;
import ofc.bot.jobs.groups.LateGroupsChecker;
import ofc.bot.jobs.income.VoiceChatMoneyHandler;
import ofc.bot.jobs.income.VoiceXPHandler;
import ofc.bot.jobs.roles.ExpiredBackupsRemover;
import ofc.bot.jobs.weekdays.SadMonday;
import ofc.bot.jobs.weekdays.SadSunday;
import ofc.bot.listeners.discord.economy.ChatMoney;
import ofc.bot.listeners.discord.guilds.BlockDumbCommands;
import ofc.bot.listeners.discord.guilds.members.MemberJoinUpsert;
import ofc.bot.listeners.discord.guilds.messages.*;
import ofc.bot.listeners.discord.guilds.reactionroles.BotChangelogRoleHandler;
import ofc.bot.listeners.discord.guilds.reactionroles.StudyRoleHandler;
import ofc.bot.listeners.discord.guilds.roles.ColorRoleHandler;
import ofc.bot.listeners.discord.guilds.roles.MemberRolesBackup;
import ofc.bot.listeners.discord.interactions.GenericInteractionLocaleUpsert;
import ofc.bot.listeners.discord.interactions.autocomplete.GroupBotAutocompletion;
import ofc.bot.listeners.discord.interactions.autocomplete.InfractionsAutocompletion;
import ofc.bot.listeners.discord.interactions.autocomplete.OficinaGroupAutocompletion;
import ofc.bot.listeners.discord.interactions.buttons.WorkReminderHandler;
import ofc.bot.listeners.discord.interactions.buttons.groups.*;
import ofc.bot.listeners.discord.interactions.buttons.pagination.*;
import ofc.bot.listeners.discord.interactions.buttons.pagination.infractions.DeleteInfraction;
import ofc.bot.listeners.discord.interactions.buttons.pagination.infractions.InfractionsPageUpdate;
import ofc.bot.listeners.discord.interactions.dm.DirectMessageReceived;
import ofc.bot.listeners.discord.interactions.modals.ChangelogCreationHandler;
import ofc.bot.listeners.discord.logs.VoiceActivity;
import ofc.bot.listeners.discord.logs.messages.*;
import ofc.bot.listeners.discord.logs.moderation.LogTimeout;
import ofc.bot.listeners.discord.logs.moderation.automod.AutoModLogger;
import ofc.bot.listeners.discord.logs.names.MemberNickUpdateLogger;
import ofc.bot.listeners.discord.logs.names.UserGlobalNameUpdateLogger;
import ofc.bot.listeners.discord.logs.names.UserNameUpdateLogger;
import ofc.bot.listeners.discord.moderation.AutoModerator;
import ofc.bot.listeners.oficina.DefaultBankTransactionLogger;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an utility class where it's main intention is to
 * register commands, listeners and jobs.
 * <p>
 * For {@link DiscordEventHandler} and {@link ofc.bot.util.content.annotations.jobs.CronJob CronJob}
 * classes, they are instnatiated by the default constructor,
 * as {@code new Class()}.
 */
public final class EntityInitializerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityInitializerManager.class);

    /**
     * This method is just a way to streamline the process of instantiating
     * Slash Commands.
     * <p>
     * The operation is delegated to the default initializer, at
     * {@link CommandsInitializer#initializeSlashCommands()}.
     */
    public static void registerSlashCommands() {
        CommandsInitializer.initializeSlashCommands();
    }

    public static void initializeCronJobs() {
        try {
            SchedulerRegistryManager.initializeSchedulers(
                    new QueryCountPrinter(),
                    new ExpiredBackupsRemover(),
                    new SadMonday(),
                    new SadSunday(),
                    new BirthdayReminder(),
                    new ColorRoleRemotionHandler(),
                    new EpicGamesPromotionAdvertiser(),
                    new HappyNewYearAnnouncement(),
                    new ToddyMedicineReminder(),

                    // Voice Income
                    new VoiceChatMoneyHandler(),
                    new VoiceXPHandler(),

                    // Groups
                    new GroupsInvoiceHandler(),
                    new LateGroupsChecker()
            );
            SchedulerRegistryManager.start();
        } catch (SchedulerException e) {
            LOGGER.error("Could not initialize schedulers", e);
        }
    }

    public static void registerButtons() {
        MemberPunishmentRepository pnshRepo = RepositoryFactory.getMemberPunishmentRepository();
        MarriageRequestRepository mreqRepo = RepositoryFactory.getMarriageRequestRepository();
        UserNameUpdateRepository namesRepo = RepositoryFactory.getUserNameUpdateRepository();
        EntityPolicyRepository policyRepo = RepositoryFactory.getEntityPolicyRepository();
        OficinaGroupRepository grpRepo = RepositoryFactory.getOficinaGroupRepository();
        UserEconomyRepository ecoRepo = RepositoryFactory.getUserEconomyRepository();
        BirthdayRepository bdayRepo = RepositoryFactory.getBirthdayRepository();
        UserXPRepository xpRepo = RepositoryFactory.getUserXPRepository();

        ButtonInteractionGateway.registerButtons(
                // Infractions
                new DeleteInfraction(pnshRepo),
                new InfractionsPageUpdate(),

                // Pagination
                new BirthdayPageUpdate(bdayRepo),
                new LeaderboardOffsetUpdate(ecoRepo),
                new LevelsPageUpdate(xpRepo),
                new NamesPageUpdate(namesRepo),
                new ProposalListPagination(mreqRepo),

                // Groups' commands confirmation handlers
                new GroupBotAddHandler(),
                new GroupChannelCreationHandler(grpRepo),
                new GroupCreationHandler(grpRepo),
                new GroupMemberAddHandler(),
                new GroupMemberRemoveHandler(),
                new GroupPermissionAddHandler(policyRepo),
                new GroupPinsHandler(),
                new GroupUpdateHandler(grpRepo)
        );
    }

    public static void registerListeners() {
        registerDiscordListeners();
        registerApplicationListeners();
    }

    private static void registerApplicationListeners() {
        BankTransactionRepository bankTrRepo = RepositoryFactory.getBankTransactionRepository();
        EventBus eventBus = EventBus.getEventBus();

        eventBus.register(
                new DefaultBankTransactionLogger(bankTrRepo)
        );
    }

    private static void registerDiscordListeners() {
        DiscordMessageUpdateRepository updRepo = RepositoryFactory.getDiscordMessageUpdateRepository();
        FormerMemberRoleRepository rolesRepo = RepositoryFactory.getFormerMemberRoleRepository();
        MemberPunishmentRepository pnshRepo = RepositoryFactory.getMemberPunishmentRepository();
        UserPreferenceRepository usprefRepo = RepositoryFactory.getUserPreferenceRepository();
        ColorRoleStateRepository colorsRepo = RepositoryFactory.getColorRoleStateRepository();
        EntityPolicyRepository policyRepo = RepositoryFactory.getEntityPolicyRepository();
        UserNameUpdateRepository namesRepo = RepositoryFactory.getUserNameUpdateRepository();
        AutomodActionRepository modActRepo = RepositoryFactory.getAutomodActionRepository();
        BlockedWordRepository blckWordsRepo = RepositoryFactory.getBlockedWordRepository();
        DiscordMessageRepository msgRepo = RepositoryFactory.getDiscordMessageRepository();
        OficinaGroupRepository grpRepo = RepositoryFactory.getOficinaGroupRepository();
        LevelRoleRepository lvlRoleRepo = RepositoryFactory.getLevelRoleRepository();
        UserEconomyRepository ecoRepo = RepositoryFactory.getUserEconomyRepository();
        GroupBotRepository grpBotRepo = RepositoryFactory.getGroupBotRepository();
        UserXPRepository xpRepo = RepositoryFactory.getUserXPRepository();
        UserRepository usersRepo = RepositoryFactory.getUserRepository();
        JDA api = Main.getApi();

        api.addEventListener(
                new AutoModLogger(),
                new BlockDumbCommands(),
                new BotChangelogRoleHandler(),
                new ButtonInteractionGateway(),
                new ChangelogCreationHandler(),
                new ChatMoney(ecoRepo),
                new ColorRoleHandler(colorsRepo),
                new DirectMessageReceived(),
                new ErikPingReactionHelper(),
                new GenericInteractionLocaleUpsert(usprefRepo),
                new GroupBotAutocompletion(grpBotRepo),
                new InfractionsAutocompletion(pnshRepo),
                new LeaveGroupCommand.FakePISuggester(),
                new LogTimeout(),
                new MemberJoinUpsert(),
                new MemberNickUpdateLogger(namesRepo, usersRepo),
                new MemberRolesBackup(rolesRepo, xpRepo),
                new MessageBulkDeleteLogger(msgRepo),
                new MessageCreatedLogger(msgRepo),
                new MessageDeletedLogger(msgRepo),
                new MessageReferenceIndicator(),
                new MessageUpdatedLogger(msgRepo, updRepo),
                new OficinaGroupAutocompletion(grpRepo),
                new OutageCommandsDisclaimer(),
                new SlashCommandsGateway(),
                new SteamScamBlocker(),
                new StudyRoleHandler(),
                new UnverifiedMembersRegisterBlocker(),
                new UserGlobalNameUpdateLogger(namesRepo, usersRepo),
                new UserNameUpdateLogger(namesRepo, usersRepo),
                new UsersXPHandler(xpRepo, lvlRoleRepo),
                new VoiceActivity(),
                new WorkReminderHandler(),

                // Moderation
                new AutoModerator(policyRepo, blckWordsRepo, pnshRepo, modActRepo)
        );
    }
}