package ofc.bot.handlers;

import net.dv8tion.jda.api.JDA;
import ofc.bot.Main;
import ofc.bot.commands.groups.LeaveGroupCommand;
import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.domain.sqlite.repository.EntityPolicyRepository;
import ofc.bot.domain.sqlite.repository.Repositories;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.cache.PolicyService;
import ofc.bot.handlers.interactions.InteractionMemoryManager;
import ofc.bot.handlers.interactions.buttons.ButtonInteractionGateway;
import ofc.bot.handlers.interactions.commands.SlashCommandsGateway;
import ofc.bot.handlers.interactions.commands.slash.CommandsInitializer;
import ofc.bot.handlers.interactions.modals.ModalInteractionGateway;
import ofc.bot.jobs.*;
import ofc.bot.jobs.groups.GroupsInvoiceHandler;
import ofc.bot.jobs.groups.LateGroupsChecker;
import ofc.bot.jobs.income.VoiceChatMoneyHandler;
import ofc.bot.jobs.income.VoiceXPHandler;
import ofc.bot.jobs.roles.ExpiredBackupsRemover;
import ofc.bot.jobs.weekdays.SadMonday;
import ofc.bot.jobs.weekdays.SadSunday;
import ofc.bot.listeners.discord.economy.ChatMoney;
import ofc.bot.listeners.discord.guilds.BlockDumbCommands;
import ofc.bot.listeners.discord.guilds.UnbanTempBanCleaner;
import ofc.bot.listeners.discord.guilds.members.MemberJoinUpsert;
import ofc.bot.listeners.discord.guilds.messages.*;
import ofc.bot.listeners.discord.guilds.reactionroles.BotChangelogRoleHandler;
import ofc.bot.listeners.discord.guilds.reactionroles.StudyRoleHandler;
import ofc.bot.listeners.discord.guilds.roles.ColorRoleHandler;
import ofc.bot.listeners.discord.guilds.roles.MemberRolesBackup;
import ofc.bot.listeners.discord.interactions.GenericInteractionLocaleUpsert;
import ofc.bot.listeners.discord.interactions.autocomplete.*;
import ofc.bot.listeners.discord.interactions.buttons.WorkReminderHandler;
import ofc.bot.listeners.discord.interactions.buttons.groups.*;
import ofc.bot.listeners.discord.interactions.buttons.pagination.*;
import ofc.bot.listeners.discord.interactions.buttons.pagination.infractions.DeleteInfraction;
import ofc.bot.listeners.discord.interactions.buttons.pagination.infractions.InfractionsPageUpdate;
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

    public static void initServices() {
        EntityPolicyRepository policyRepo = Repositories.getEntityPolicyRepository();
        PolicyService.setPolicyRepo(policyRepo);
    }

    public static void registerComposedInteractions() {
        var pnshRepo   = Repositories.getMemberPunishmentRepository();
        var mreqRepo   = Repositories.getMarriageRequestRepository();
        var namesRepo  = Repositories.getUserNameUpdateRepository();
        var policyRepo = Repositories.getEntityPolicyRepository();
        var grpRepo    = Repositories.getOficinaGroupRepository();
        var ecoRepo    = Repositories.getUserEconomyRepository();
        var bdayRepo   = Repositories.getBirthdayRepository();
        var xpRepo     = Repositories.getUserXPRepository();

        InteractionMemoryManager.getManager().registerListeners(
                // Infractions
                new DeleteInfraction(pnshRepo),
                new InfractionsPageUpdate(),

                // Pagination
                new BirthdayPageUpdate(bdayRepo),
                new LeaderboardOffsetUpdate(ecoRepo),
                new LevelsPageUpdate(xpRepo),
                new NamesPageUpdate(namesRepo),
                new ProposalListPagination(mreqRepo),
                new TransactionsPagination(),

                // Groups' commands confirmation handlers
                new GroupBotAddHandler(),
                new GroupChannelCreationHandler(grpRepo),
                new GroupCreationHandler(grpRepo),
                new GroupMemberAddHandler(),
                new GroupMemberRemoveHandler(),
                new GroupPermissionAddHandler(policyRepo),
                new GroupPinsHandler(),
                new GroupUpdateHandler(grpRepo),

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
        var usprefRepo    = Repositories.getUserPreferenceRepository();
        var colorsRepo    = Repositories.getColorRoleStateRepository();
        var policyRepo    = Repositories.getEntityPolicyRepository();
        var cmdRepo       = Repositories.getCommandHistoryRepository();
        var namesRepo     = Repositories.getUserNameUpdateRepository();
        var modActRepo    = Repositories.getAutomodActionRepository();
        var blckWordsRepo = Repositories.getBlockedWordRepository();
        var msgRepo       = Repositories.getDiscordMessageRepository();
        var grpRepo       = Repositories.getOficinaGroupRepository();
        var ecoRepo       = Repositories.getUserEconomyRepository();
        var grpBotRepo    = Repositories.getGroupBotRepository();
        var tmpBanRepo    = Repositories.getTempBanRepository();
        var xpRepo        = Repositories.getUserXPRepository();
        var userRepo      = Repositories.getUserRepository();

        api.addEventListener(
                new AutoModerator(policyRepo, blckWordsRepo, pnshRepo, modActRepo),
                new AutoModLogger(),
                new BlockDumbCommands(),
                new BotChangelogRoleHandler(),
                new ButtonInteractionGateway(),
                new ChangelogCreationHandler(),
                new ChatMoney(ecoRepo),
                new ChoosableRolesListener(),
                new ColorRoleHandler(colorsRepo),
                new DirectMessageReceived(),
                new ErikPingReactionHelper(),
                new GenericInteractionLocaleUpsert(usprefRepo),
                new GroupBotAutocompletion(grpBotRepo),
                new InfractionsAutocompletion(pnshRepo),
                new LeaveGroupCommand.FakePISuggester(),
                new LogTimeout(),
                new MemberJoinUpsert(),
                new MemberNickUpdateLogger(namesRepo, userRepo),
                new MemberRolesBackup(rolesRepo, xpRepo),
                new ModalInteractionGateway(),
                new UnbanTempBanCleaner(tmpBanRepo),
                new MessageBulkDeleteLogger(msgRepo),
                new MessageCreatedLogger(msgRepo),
                new MessageDeletedLogger(msgRepo),
                new MessageReferenceIndicator(),
                new MessageUpdatedLogger(msgRepo, updRepo),
                new OficinaGroupAutocompletion(grpRepo),
                new OutageCommandsDisclaimer(),
                new ResourceAutocompletion(userRepo),
                new SlashCommandsGateway(cmdRepo),
                new SteamScamBlocker(),
                new StudyRoleHandler(),
                new UnverifiedMembersRegisterBlocker(),
                new UserGlobalNameUpdateLogger(namesRepo, userRepo),
                new UserNameUpdateLogger(namesRepo, userRepo),
                new UsersXPHandler(),
                new VoiceActivity(),
                new WorkReminderHandler()
        );
    }
}