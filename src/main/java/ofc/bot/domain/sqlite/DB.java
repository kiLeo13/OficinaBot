package ofc.bot.domain.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.tables.*;
import ofc.bot.internal.data.BotFiles;
import ofc.bot.listeners.console.QueryCounter;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class DB {
    private static final Logger LOGGER = LoggerFactory.getLogger(DB.class);
    private static HikariDataSource dataSource;
    
    public static DSLContext getContext() {
        Configuration config = new DefaultConfiguration()
                .set(dataSource)
                .set(SQLDialect.SQLITE)
                .set(new QueryCounter());
        return DSL.using(config);
    }

    public static void init() throws DataAccessException {
        initConfigs();
        DSLContext ctx = getContext();
        List<InitializableTable<?>> tables = getTablesCreation();

        for (InitializableTable<?> table : tables) {
            table.getSchema(ctx).execute();
            LOGGER.info("Successfully created table \"{}\"", table.getName());
        }
        LOGGER.info("Successfully created all tables");
    }

    private static List<InitializableTable<?>> getTablesCreation() {
        return List.of(
                AnnouncedGamesTable.ANNOUNCED_GAMES,
                BankTransactionsTable.BANK_TRANSACTIONS,
                BirthdaysTable.BIRTHDAYS,
                ColorRolesStateTable.COLOR_ROLES_STATES,
                CustomUserinfoTable.CUSTOM_USERINFO,
                DiscordMessagesTable.DISCORD_MESSAGES,
                DiscordMessageUpdatesTable.DISCORD_MESSAGE_UPDATES,
                FormerMembersRolesTable.FORMER_MEMBERS_ROLES,
                MarriageRequestsTable.MARRIAGE_REQUESTS,
                MarriagesTable.MARRIAGES,
                MembersEmojisTable.MEMBERS_EMOJIS,
                OficinaGroupsTable.OFICINA_GROUPS,
                UserNamesUpdatesTable.USERNAMES_UPDATES,
                UsersEconomyTable.USERS_ECONOMY,
                UsersExclusionsTable.USERS_EXCLUSIONS,
                UsersPreferencesTable.USERS_PREFERENCES,
                UsersTable.USERS
        );
    }

    private static void initConfigs() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + BotFiles.DATABASE);

        dataSource = new HikariDataSource(config);
    }
}