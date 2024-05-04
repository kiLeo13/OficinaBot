package ofc.bot.commands.userinfo.custom;

import net.dv8tion.jda.api.Permission;
import ofc.bot.commands.userinfo.custom.subcommands.Reset;
import ofc.bot.commands.userinfo.custom.subcommands.SetColor;
import ofc.bot.commands.userinfo.custom.subcommands.SetDescription;
import ofc.bot.commands.userinfo.custom.subcommands.SetFooter;
import ofc.bot.databases.DBManager;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;

import static ofc.bot.databases.entities.tables.CustomUserinfo.CUSTOM_USERINFO;

@DiscordCommand(name = "customize", description = "Customize o seu userinfo.")
@CommandPermission(Permission.MANAGE_SERVER)
public class CustomUserinfo extends SlashCommand {

    public CustomUserinfo() {
        super(
            new SetDescription(),
            new SetColor(),
            new SetFooter(),
            new Reset()
        );
    }

    public static void ensureExists(long userId) {

        DSLContext ctx = DBManager.getContext();
        long timestamp = Bot.unixNow();

        ctx.insertInto(CUSTOM_USERINFO)
                .set(CUSTOM_USERINFO.USER_ID, userId)
                .set(CUSTOM_USERINFO.CREATED_AT, timestamp)
                .set(CUSTOM_USERINFO.UPDATED_AT, timestamp)
                .onDuplicateKeyIgnore()
                .execute();
    }
}