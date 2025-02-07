package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.sqlite.DB;
import org.jooq.DSLContext;

public final class RepositoryFactory {
    private static AnnouncedGameRepository announcedGameRepository;
    private static AutomodActionRepository automodActionRepository;
    private static BankTransactionRepository bankTransactionRepository;
    private static BirthdayRepository birthdayRepository;
    private static BlockedWordRepository blockedWordRepository;
    private static ColorRoleStateRepository colorRoleStateRepository;
    private static CustomUserinfoRepository customUserinfoRepository;
    private static DiscordMessageRepository discordMessageRepository;
    private static DiscordMessageUpdateRepository discordMessageUpdateRepository;
    private static FormerMemberRoleRepository formerMemberRoleRepository;
    private static GroupBotRepository groupBotRepository;
    private static LevelRoleRepository levelRoleRepository;
    private static MarriageRepository marriageRepository;
    private static MarriageRequestRepository marriageRequestRepository;
    private static MemberEmojiRepository memberEmojiRepository;
    private static MemberPunishmentRepository memberPunishmentRepository;
    private static OficinaGroupRepository oficinaGroupRepository;
    private static UserEconomyRepository userEconomyRepository;
    private static EntityPolicyRepository entityPolicyRepository;
    private static UserNameUpdateRepository userNameUpdateRepository;
    private static UserPreferenceRepository userPreferenceRepository;
    private static UserRepository userRepository;
    private static UserXPRepository userXPRepository;

    private RepositoryFactory() {}

    public static AnnouncedGameRepository getAnnouncedGameRepository() {
        if (announcedGameRepository == null) announcedGameRepository = new AnnouncedGameRepository(getDSLContext());
        return announcedGameRepository;
    }

    public static AutomodActionRepository getAutomodActionRepository() {
        if (automodActionRepository == null) automodActionRepository = new AutomodActionRepository(getDSLContext());
        return automodActionRepository;
    }

    public static BankTransactionRepository getBankTransactionRepository() {
        if (bankTransactionRepository == null) bankTransactionRepository = new BankTransactionRepository(getDSLContext());
        return bankTransactionRepository;
    }

    public static BirthdayRepository getBirthdayRepository() {
        if (birthdayRepository == null) birthdayRepository = new BirthdayRepository(getDSLContext());
        return birthdayRepository;
    }

    public static BlockedWordRepository getBlockedWordRepository() {
        if (blockedWordRepository == null) blockedWordRepository = new BlockedWordRepository(getDSLContext());
        return blockedWordRepository;
    }

    public static ColorRoleStateRepository getColorRoleStateRepository() {
        if (colorRoleStateRepository == null) colorRoleStateRepository = new ColorRoleStateRepository(getDSLContext());
        return colorRoleStateRepository;
    }

    public static CustomUserinfoRepository getCustomUserinfoRepository() {
        if (customUserinfoRepository == null) customUserinfoRepository = new CustomUserinfoRepository(getDSLContext());
        return customUserinfoRepository;
    }

    public static DiscordMessageRepository getDiscordMessageRepository() {
        if (discordMessageRepository == null) discordMessageRepository = new DiscordMessageRepository(getDSLContext());
        return discordMessageRepository;
    }

    public static DiscordMessageUpdateRepository getDiscordMessageUpdateRepository() {
        if (discordMessageUpdateRepository == null) discordMessageUpdateRepository = new DiscordMessageUpdateRepository(getDSLContext());
        return discordMessageUpdateRepository;
    }

    public static FormerMemberRoleRepository getFormerMemberRoleRepository() {
        if (formerMemberRoleRepository == null) formerMemberRoleRepository = new FormerMemberRoleRepository(getDSLContext());
        return formerMemberRoleRepository;
    }

    public static GroupBotRepository getGroupBotRepository() {
        if (groupBotRepository == null) groupBotRepository = new GroupBotRepository(getDSLContext());
        return groupBotRepository;
    }

    public static LevelRoleRepository getLevelRoleRepository() {
        if (levelRoleRepository == null) levelRoleRepository = new LevelRoleRepository(getDSLContext());
        return levelRoleRepository;
    }

    public static MarriageRepository getMarriageRepository() {
        if (marriageRepository == null) marriageRepository = new MarriageRepository(getDSLContext());
        return marriageRepository;
    }

    public static MarriageRequestRepository getMarriageRequestRepository() {
        if (marriageRequestRepository == null) marriageRequestRepository = new MarriageRequestRepository(getDSLContext());
        return marriageRequestRepository;
    }

    public static MemberEmojiRepository getMemberEmojiRepository() {
        if (memberEmojiRepository == null) memberEmojiRepository = new MemberEmojiRepository(getDSLContext());
        return memberEmojiRepository;
    }

    public static MemberPunishmentRepository getMemberPunishmentRepository() {
        if (memberPunishmentRepository == null) memberPunishmentRepository = new MemberPunishmentRepository(getDSLContext());
        return memberPunishmentRepository;
    }

    public static OficinaGroupRepository getOficinaGroupRepository() {
        if (oficinaGroupRepository == null) oficinaGroupRepository = new OficinaGroupRepository(getDSLContext());
        return oficinaGroupRepository;
    }

    public static UserEconomyRepository getUserEconomyRepository() {
        if (userEconomyRepository == null) userEconomyRepository = new UserEconomyRepository(getDSLContext());
        return userEconomyRepository;
    }

    public static EntityPolicyRepository getEntityPolicyRepository() {
        if (entityPolicyRepository == null) entityPolicyRepository = new EntityPolicyRepository(getDSLContext());
        return entityPolicyRepository;
    }

    public static UserNameUpdateRepository getUserNameUpdateRepository() {
        if (userNameUpdateRepository == null) userNameUpdateRepository = new UserNameUpdateRepository(getDSLContext());
        return userNameUpdateRepository;
    }

    public static UserPreferenceRepository getUserPreferenceRepository() {
        if (userPreferenceRepository == null) userPreferenceRepository = new UserPreferenceRepository(getDSLContext());
        return userPreferenceRepository;
    }

    public static UserRepository getUserRepository() {
        if (userRepository == null) userRepository = new UserRepository(getDSLContext());
        return userRepository;
    }

    public static UserXPRepository getUserXPRepository() {
        if (userXPRepository == null) userXPRepository = new UserXPRepository(getDSLContext());
        return userXPRepository;
    }

    private static DSLContext getDSLContext() {
        return DB.getContext();
    }
}
