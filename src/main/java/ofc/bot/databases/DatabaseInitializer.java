package ofc.bot.databases;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static ofc.bot.databases.entities.records.UserExclusionRecord.USERS_EXCLUSIONS;
import static ofc.bot.databases.entities.tables.Birthdays.BIRTHDAYS;
import static ofc.bot.databases.entities.tables.ColorRoles.COLOR_ROLES;
import static ofc.bot.databases.entities.tables.CustomUserinfo.CUSTOM_USERINFO;
import static ofc.bot.databases.entities.tables.DiscordMessageUpdates.DISCORD_MESSAGE_UPDATES;
import static ofc.bot.databases.entities.tables.DiscordMessages.DISCORD_MESSAGES;
import static ofc.bot.databases.entities.tables.Economy.ECONOMY;
import static ofc.bot.databases.entities.tables.FormerMembersRoles.FORMER_MEMBERS_ROLES;
import static ofc.bot.databases.entities.tables.MarriageRequests.MARRIAGE_REQUESTS;
import static ofc.bot.databases.entities.tables.Marriages.MARRIAGES;
import static ofc.bot.databases.entities.tables.Nicknames.NICKNAMES;
import static ofc.bot.databases.entities.tables.UserGlobalNameUpdates.USER_GLOBAL_NAME_UPDATES;
import static ofc.bot.databases.entities.tables.UserNameUpdates.USER_NAME_UPDATES;
import static ofc.bot.databases.entities.tables.Users.USERS;
import static ofc.bot.databases.entities.tables.UsersPreferences.USERS_PREFERENCES;
import static ofc.bot.internal.data.BotFiles.DATABASE;
import static org.jooq.impl.DSL.foreignKey;

public final class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static void init() throws IOException {

        if (DATABASE.createNewFile())
            logger.warn("Database {} was not found! A new one has been created.", DATABASE.getName());
        else
            logger.info("Successfully found {}!", DATABASE.getName());

        DSLContext ctx = DBManager.getContext();

        ctx.createTableIfNotExists(ECONOMY)
                .primaryKey(ECONOMY.USER_ID)
                .columns(ECONOMY.fields())
                .constraint(
                        foreignKey(ECONOMY.USER_ID)
                                .references(USERS)
                )
                .execute();

        logger.info("Successfully created table '{}'", ECONOMY.getName());

        ctx.createTableIfNotExists(BIRTHDAYS)
                .primaryKey(BIRTHDAYS.USER_ID)
                .columns(BIRTHDAYS.fields())
                .execute();

        logger.info("Successfully created table '{}'", BIRTHDAYS.getName());

        ctx.createTableIfNotExists(COLOR_ROLES)
                .primaryKey(COLOR_ROLES.ID)
                .columns(COLOR_ROLES.fields())
                .unique(COLOR_ROLES.ROLE_ID, COLOR_ROLES.GUILD_ID, COLOR_ROLES.CREATED_AT)
                .execute();

        logger.info("Successfully created table '{}'", COLOR_ROLES.getName());

        ctx.createTableIfNotExists(NICKNAMES)
                .primaryKey(NICKNAMES.ID)
                .columns(NICKNAMES.fields())
                .execute();

        logger.info("Successfully created table '{}'", NICKNAMES.getName());

        ctx.createTableIfNotExists(MARRIAGES)
                .primaryKey(MARRIAGES.ID)
                .columns(MARRIAGES.fields())
                .constraints(
                        foreignKey(MARRIAGES.REQUESTER_ID)
                                .references(USERS, USERS.ID),
                        foreignKey(MARRIAGES.TARGET_ID)
                                .references(USERS, USERS.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", MARRIAGES.getName());

        ctx.createTableIfNotExists(CUSTOM_USERINFO)
                .primaryKey(CUSTOM_USERINFO.USER_ID)
                .columns(CUSTOM_USERINFO.fields())
                .execute();

        logger.info("Successfully created table '{}'", CUSTOM_USERINFO.getName());

        ctx.createTableIfNotExists(FORMER_MEMBERS_ROLES)
                .primaryKey(FORMER_MEMBERS_ROLES.ID)
                .columns(FORMER_MEMBERS_ROLES.fields())
                .unique(FORMER_MEMBERS_ROLES.USER, FORMER_MEMBERS_ROLES.ROLE)
                .execute();

        logger.info("Successfully created table '{}'", FORMER_MEMBERS_ROLES.getName());

        ctx.createTableIfNotExists(MARRIAGE_REQUESTS)
                .primaryKey(MARRIAGE_REQUESTS.ID)
                .columns(MARRIAGE_REQUESTS.fields())
                .execute();

        logger.info("Successfully created table '{}'", MARRIAGE_REQUESTS.getName());

        ctx.createTableIfNotExists(USERS)
                .primaryKey(USERS.ID)
                .columns(USERS.fields())
                .execute();

        logger.info("Successfully created table '{}'", USERS.getName());

        ctx.createTableIfNotExists(DISCORD_MESSAGES)
                .primaryKey(DISCORD_MESSAGES.ID)
                .columns(DISCORD_MESSAGES.fields())
                .constraints(
                        foreignKey(DISCORD_MESSAGES.AUTHOR_ID).references(USERS, USERS.ID),
                        foreignKey(DISCORD_MESSAGES.DELETION_AUTHOR_ID).references(USERS, USERS.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", DISCORD_MESSAGES.getName());

        ctx.createTableIfNotExists(DISCORD_MESSAGE_UPDATES)
                .primaryKey(DISCORD_MESSAGE_UPDATES.ID)
                .columns(DISCORD_MESSAGE_UPDATES.fields())
                .constraint(
                        foreignKey(DISCORD_MESSAGE_UPDATES.MESSAGE_ID)
                                .references(DISCORD_MESSAGES, DISCORD_MESSAGES.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", DISCORD_MESSAGE_UPDATES.getName());

        ctx.createTableIfNotExists(USERS_PREFERENCES)
                .primaryKey(USERS_PREFERENCES.USER_ID)
                .columns(USERS_PREFERENCES.fields())
                .constraint(
                        foreignKey(USERS_PREFERENCES.USER_ID)
                                .references(USERS, USERS.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", USERS_PREFERENCES.getName());

        ctx.createTableIfNotExists(USER_NAME_UPDATES)
                .primaryKey(USER_NAME_UPDATES.ID)
                .columns(USER_NAME_UPDATES.fields())
                .constraint(
                        foreignKey(USER_NAME_UPDATES.USER_ID)
                                .references(USERS, USERS.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", USER_NAME_UPDATES.getName());

        ctx.createTableIfNotExists(USER_GLOBAL_NAME_UPDATES)
                .primaryKey(USER_GLOBAL_NAME_UPDATES.ID)
                .columns(USER_GLOBAL_NAME_UPDATES.fields())
                .constraint(
                        foreignKey(USER_GLOBAL_NAME_UPDATES.USER_ID)
                                .references(USERS, USERS.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", USER_GLOBAL_NAME_UPDATES.getName());

        ctx.createTableIfNotExists(USERS_EXCLUSIONS)
                .primaryKey(USERS_EXCLUSIONS.ID)
                .columns(USERS_EXCLUSIONS.fields())
                .unique(USERS_EXCLUSIONS.USER_ID, USERS_EXCLUSIONS.TYPE)
                .constraint(
                        foreignKey(USERS_EXCLUSIONS.USER_ID)
                                .references(USERS, USERS.ID)
                )
                .execute();

        logger.info("Successfully created table '{}'", USERS_EXCLUSIONS.getName());

        logger.info("Successfully created all tables");
    }
}