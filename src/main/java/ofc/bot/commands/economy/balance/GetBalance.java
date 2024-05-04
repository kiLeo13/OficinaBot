package ofc.bot.commands.economy.balance;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.databases.DBManager;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.Record6;

import java.awt.*;

import static ofc.bot.databases.entities.tables.Economy.ECONOMY;
import static ofc.bot.databases.entities.tables.Users.USERS;
import static org.jooq.impl.DSL.*;

@DiscordCommand(name = "balance", description = "Verifica o saldo de um usuário.")
public class GetBalance extends SlashCommand {
    private static final BalanceData EMPTY_BALANCE_DATA = new BalanceData(0, 0, 0 ,0 ,0, 0, false);

    @Option
    private static final OptionData USER = new OptionData(OptionType.USER, "user", "O usuário a verificar o saldo.");

    @Option
    private static final OptionData FULL = new OptionData(OptionType.BOOLEAN, "full", "Se devemos enviar todas as informações sobre o estado do usuário na economia (Padrão: False).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        User issuer = ctx.getUser();
        User target = ctx.getOption("user", issuer, OptionMapping::getAsUser);
        boolean full = ctx.getOption("full", false, OptionMapping::getAsBoolean);

        long userId = target.getIdLong();
        BalanceData balanceData = retrieveBalance(userId);
        MessageEmbed embed = embed(target, balanceData, full);

        ctx.replyEmbeds(embed);

        return Status.PASSED;
    }

    private MessageEmbed embed(User user, BalanceData data, boolean fullBody) {
        EmbedBuilder builder = new EmbedBuilder();

        String rank = data.prettyRank();
        String name = user.getEffectiveName();
        String avatar = user.getEffectiveAvatarUrl();
        String balance = data.prettyBalance();
        Color color = Bot.Colors.DEFAULT;

        builder.setAuthor(name, null, avatar)
                .setDescription("Use `/leaderboard` para ver o ranking global.")
                .setColor(color)
                .addField("Saldo", balance, true)
                .addField("Rank", rank, true);

        if (fullBody && data.found())
            applyExtraFields(data, builder);

        return builder.build();
    }

    private void applyExtraFields(BalanceData data, EmbedBuilder builder) {
        builder
                .addField("📅 Iniciou", data.prettyCreation(), true)
                .addField("💼 Último Trabalho", data.prettyLastWork(), true)
                .addField("☀ Último Daily", data.prettyLastDaily(), true);
    }

    private BalanceData retrieveBalance(long userId) {

        DSLContext ctx = DBManager.getContext();
        Record6<Integer, Long, Long, Long, Long, Long> balanceData = ctx.with("ranked_economy")
                .as(
                        select(
                                ECONOMY.USER_ID,
                                ECONOMY.BALANCE,
                                rowNumber().over(orderBy(ECONOMY.BALANCE.desc())).as("rank")
                        ).from(ECONOMY)
                )
                .select(
                        field(name("ranked_economy", "rank"), int.class),
                        field(name("ranked_economy", "balance"), long.class),
                        ECONOMY.CREATED_AT,
                        ECONOMY.UPDATED_AT,
                        ECONOMY.LAST_WORK_AT,
                        ECONOMY.LAST_DAILY_AT
                )
                .from(table(name("ranked_economy")))
                .join(ECONOMY)
                .on(field(name("ranked_economy", "user_id"), long.class).eq(ECONOMY.USER_ID))
                .join(USERS)
                .on(USERS.ID.eq(field(name("ranked_economy", "user_id"), long.class)))
                .where(field(name("ranked_economy", "user_id")).eq(userId))
                .fetchOne();

        if (balanceData == null)
            return EMPTY_BALANCE_DATA;

        int rank = balanceData.value1();
        long balance = balanceData.value2();
        long created = balanceData.value3();
        long updated = balanceData.value4();
        long lastWork = balanceData.value5();
        long lastDaily = balanceData.value6();

        return new BalanceData(rank, balance, created, updated, lastWork, lastDaily, true);
    }
}