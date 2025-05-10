package ofc.bot.handlers;

import net.dv8tion.jda.api.JDA;
import ofc.bot.Main;
import ofc.bot.commands.groups.LeaveGroupCommand;
import ofc.bot.domain.sqlite.repository.*;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.cache.PolicyService;
import ofc.bot.handlers.interactions.InteractionMemoryManager;
import ofc.bot.handlers.interactions.buttons.ButtonInteractionGateway;
import ofc.bot.handlers.commands.SlashCommandsGateway;
import ofc.bot.handlers.commands.slash.CommandsInitializer;
import ofc.bot.handlers.interactions.modals.ModalInteractionGateway;
import ofc.bot.jobs.*;
import ofc.bot.jobs.groups.GroupsInvoiceHandler;
import ofc.bot.jobs.groups.LateGroupsChecker;
import ofc.bot.jobs.income.VoiceChatMoneyHandler;
import ofc.bot.jobs.income.VoiceXPHandler;
import ofc.bot.jobs.roles.ExpiredBackupsRemover;
import ofc.bot.jobs.weekdays.SadMonday;
import ofc.bot.jobs.weekdays.SadSunday;
import ofc.bot.listeners.discord.economy.ChatMoneyHandler;
import ofc.bot.listeners.discord.guilds.BlockDumbCommands;
import ofc.bot.listeners.discord.guilds.UnbanTempBanCleaner;
import ofc.bot.listeners.discord.guilds.members.MemberJoinUpsert;
import ofc.bot.listeners.discord.guilds.messages.*;
import ofc.bot.listeners.discord.guilds.reactionroles.BotChangelogRoleHandler;
import ofc.bot.listeners.discord.guilds.reactionroles.StudyRoleHandler;
import ofc.bot.listeners.discord.guilds.roles.ColorRoleHandler;
import ofc.bot.listeners.discord.guilds.roles.MemberRolesBackup;
import ofc.bot.listeners.discord.guilds.voice.solo.SoloChannelsHandler;
import ofc.bot.listeners.discord.interactions.GenericInteractionLocaleUpsert;
import ofc.bot.listeners.discord.interactions.autocomplete.*;
import ofc.bot.listeners.discord.interactions.buttons.WorkReminderHandler;
import ofc.bot.listeners.discord.interactions.buttons.bets.TicTacToeAcceptHandler;
import ofc.bot.listeners.discord.interactions.buttons.groups.*;
import ofc.bot.listeners.discord.interactions.buttons.pagination.*;
import ofc.bot.listeners.discord.interactions.buttons.pagination.infractions.DeleteInfraction;
import ofc.bot.listeners.discord.interactions.buttons.pagination.infractions.InfractionsPageUpdate;
import ofc.bot.listeners.discord.interactions.buttons.pagination.reminders.DeleteReminder;
import ofc.bot.listeners.discord.interactions.buttons.pagination.reminders.RemindersPageUpdate;
import ofc.bot.listeners.discord.interactions.buttons.pagination.twitch.PaginateTwitchUsers;
import ofc.bot.listeners.discord.interactions.dm.DirectMessageReceived;
import ofc.bot.listeners.discord.interactions.menus.ChoosableRolesListener;
import ofc.bot.listeners.discord.interactions.modals.ChangelogCreationHandler;
import ofc.bot.listeners.discord.interactions.modals.ChoosableRolesHandler;
import ofc.bot.listeners.discord.logs.VoiceActivity;
import ofc.bot.listeners.discord.logs.messages.*;
import ofc.bot.listeners.discord.logs.moderation.LogTimeout;
import ofc.bot.listeners.discord.logs.moderation.automod.AutoModLogger;
import ofc.bot.listeners.discord.logs.names.MemberNickUpdateLogger;
import ofc.bot.listeners.discord.logs.names.UserGlobalNameUpdateLogger;
import ofc.bot.listeners.discord.logs.names.UserNameUpdateLogger;
import ofc.bot.listeners.discord.moderation.AutoModerator;
import ofc.bot.listeners.oficina.DefaultBankTransactionLogger;
import ofc.bot.util.GroupHelper;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an utility class to register/initialize commands,
 * listeners, jobs, services, etc.
 * <p>
 * For {@link ofc.bot.util.content.annotations.jobs.CronJob CronJob}
 * classes, they are instantiated through the default constructor,
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
                    new ExpiredBackupsRemover(),
                    new SadMonday(),
                    new SadSunday(),
                    new BirthdayReminder(),
                    new ColorRoleRemotionHandler(),
                    new ExpiredBanHandler(),
                    new HappyNewYearAnnouncement(),
                    new NickTimeUpdate(),
                    new QueryCountPrinter(),
                    new RemindersHandler(),

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

    public static void initServices() {
        var policyRepo  = Repositories.getEntityPolicyRepository();
        var grpPerkRepo = Repositories.getGroupPerkRepository();

        PolicyService.setPolicyRepo(policyRepo);
        GroupHelper.setRepositories(grpPerkRepo);
    }

    public static void registerComposedInteractions() {
        var pnshRepo     = Repositories.getMemberPunishmentRepository();
        var betUsersRepo = Repositories.getGameParticipantRepository();
        var mreqRepo     = Repositories.getMarriageRequestRepository();
        var namesRepo    = Repositories.getUserNameUpdateRepository();
        var policyRepo   = Repositories.getEntityPolicyRepository();
        var grpRepo      = Repositories.getOficinaGroupRepository();
        var ecoRepo      = Repositories.getUserEconomyRepository();
        var appBanRepo   = Repositories.getAppUserBanRepository();
        var bdayRepo     = Repositories.getBirthdayRepository();
        var remRepo      = Repositories.getReminderRepository();
        var betRepo      = Repositories.getBetGameRepository();
        var xpRepo       = Repositories.getUserXPRepository();

        InteractionMemoryManager.getManager().registerListeners(
                // Infractions
                new DeleteInfraction(pnshRepo),
                new InfractionsPageUpdate(),

                // Reminders
                new DeleteReminder(remRepo),
                new RemindersPageUpdate(),

                // Pagination
                new BirthdayPageUpdate(bdayRepo),
                new LeaderboardOffsetUpdate(),
                new LevelsPageUpdate(xpRepo),
                new NamesPageUpdate(namesRepo),
                new ProposalListPagination(mreqRepo),
                new TransactionsPagination(),

                // Twitch
                new PaginateTwitchUsers(),

                // Groups' commands confirmation handlers
                new GroupBotAddHandler(),
                new GroupChannelCreationHandler(grpRepo),
                new GroupCreationHandler(grpRepo),
                new GroupInvoicePaymentHandler(grpRepo),
                new GroupMemberAddHandler(),
                new GroupMemberRemoveHandler(),
                new GroupPermissionAddHandler(policyRepo),
                new GroupPinsHandler(),
                new GroupUpdateHandler(grpRepo),

                // Bets
                new TicTacToeAcceptHandler(ecoRepo, betRepo, betUsersRepo, appBanRepo),

                // Generic
                new ChoosableRolesHandler()
        );
    }

    public static void registerListeners() {
        registerDiscordListeners();
        registerApplicationListeners();
    }

    private static void registerApplicationListeners() {
        BankTransactionRepository bankTrRepo = Repositories.getBankTransactionRepository();
        EventBus eventBus = EventBus.getEventBus();

        eventBus.register(
                new DefaultBankTransactionLogger(bankTrRepo)
        );
    }

    private static void registerDiscordListeners() {
        JDA api = Main.getApi();
        var updRepo       = Repositories.getDiscordMessageUpdateRepository();
        var rolesRepo     = Repositories.getFormerMemberRoleRepository();
        var pnshRepo      = Repositories.getMemberPunishmentRepository();
        var msgRepo       = Repositories.getDiscordMessageRepository();
        var colorsRepo    = Repositories.getColorRoleStateRepository();
        var cmdRepo       = Repositories.getCommandHistoryRepository();
        var namesRepo     = Repositories.getUserNameUpdateRepository();
        var usprefRepo    = Repositories.getUserPreferenceRepository();
        var modActRepo    = Repositories.getAutomodActionRepository();
        var policyRepo    = Repositories.getEntityPolicyRepository();
        var grpRepo       = Repositories.getOficinaGroupRepository();
        var blckWordsRepo = Repositories.getBlockedWordRepository();
        var ecoRepo       = Repositories.getUserEconomyRepository();
        var appBanRepo    = Repositories.getAppUserBanRepository();
        var grpBotRepo    = Repositories.getGroupBotRepository();
        var tmpBanRepo    = Repositories.getTempBanRepository();
        var xpRepo        = Repositories.getUserXPRepository();
        var userRepo      = Repositories.getUserRepository();

        api.addEventListener(
                new AutoModerator(blckWordsRepo, pnshRepo, modActRepo),
                new AutoModLogger(),
                new BlockDumbCommands(),
                new BotChangelogRoleHandler(),
                new ButtonInteractionGateway(appBanRepo),
                new ChangelogCreationHandler(),
                new ChatMoneyHandler(ecoRepo),
                new ChoosableRolesListener(),
                new ColorRoleHandler(colorsRepo),
                new DirectMessageReceived(),
                new ErikPingReactionHelper(),
                new GenericInteractionLocaleUpsert(usprefRepo),
                new GroupBotAutocompletion(grpBotRepo),
                new InfractionsAutocompletion(pnshRepo),
                new LeaveGroupCommand.FakePISuggester(),
                new LogTimeout(),
                new LorittaDailySpamBlocker(),
                new MemberJoinUpsert(),
                new MemberNickUpdateLogger(namesRepo, userRepo),
                new MemberRolesBackup(rolesRepo, xpRepo),
                new ModalInteractionGateway(),
                new MessageBulkDeleteLogger(msgRepo),
                new MessageCreatedLogger(msgRepo),
                new MessageDeletedLogger(msgRepo),
                new MessageReferenceIndicator(),
                new MessageUpdatedLogger(msgRepo, updRepo),
                new OficinaGroupAutocompletion(grpRepo),
                new OutageCommandsDisclaimer(),
                new ResourceAutocompletion(userRepo),
                new SlashCommandsGateway(cmdRepo, appBanRepo),
                new SoloChannelsHandler(),
                new SteamScamBlocker(),
                new StudyRoleHandler(),
                new UnbanTempBanCleaner(tmpBanRepo),
                new UnverifiedMembersRegisterBlocker(),
                new UserGlobalNameUpdateLogger(namesRepo, userRepo),
                new UserNameUpdateLogger(namesRepo, userRepo),
                new UsersXPHandler(),
                new VoiceActivity(),
                new VoiceDisconnector(),
                new WorkReminderHandler()
        );
    }
}