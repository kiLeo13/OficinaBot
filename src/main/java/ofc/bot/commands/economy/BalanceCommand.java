package ofc.bot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.awt.*;
import java.util.List;

@DiscordCommand(name = "balance", description = "Verifica o saldo de um usuário.")
public class BalanceCommand extends SlashCommand {
    private final UserEconomyRepository ecoRepo;

    public BalanceCommand(UserEconomyRepository econRepo) {
        this.ecoRepo = econRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        User issuer = ctx.getUser();
        User target = ctx.getOption("user", issuer, OptionMapping::getAsUser);
        boolean full = ctx.getOption("full", false, OptionMapping::getAsBoolean);
        long userId = target.getIdLong();
        UserEconomy userEco = ecoRepo.findByUserId(userId, UserEconomy.fromUserId(userId));
        MessageEmbed embed = embed(target, userEco, full && userEco.isGenerated());

        return ctx.replyEmbeds(embed);
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.USER, "user", "O usuário a verificar o saldo"),
                new OptionData(OptionType.BOOLEAN, "full", "Se devemos enviar todas as informações sobre o estado do usuário na economia (Padrão: False).")
        );
    }

    private MessageEmbed embed(User user, UserEconomy eco, boolean isFull) {
        EmbedBuilder builder = new EmbedBuilder();

        int rank = ecoRepo.findRankByUser(eco);
        String fmtRank = rank == 0 ? "*Sem rank*" : "#" + rank;
        String name = user.getEffectiveName();
        String avatar = user.getEffectiveAvatarUrl();
        String fmtBalance = Bot.fmtNum(eco.getBalance());
        Color color = Bot.Colors.DEFAULT;

        builder.setAuthor(name, null, avatar)
                .setDescription("Use `/leaderboard` para ver o ranking global.")
                .setColor(color)
                .addField(UserEconomy.SYMBOL + " Saldo", fmtBalance, true)
                .addField(UserEconomy.RANK_SYMBOL + " Rank", fmtRank, true);

        if (isFull)
            applyExtraFields(eco, builder);

        return builder.build();
    }

    private void applyExtraFields(UserEconomy eco, EmbedBuilder builder) {
        builder
                .addField("📅 Iniciou", formatTimestamp(eco.getTimeCreated()), true)
                .addField("💼 Último Trabalho", formatTimestamp(eco.getLastWorkAt()), true)
                .addField("\uD83C\uDF1E Último Daily", formatTimestamp(eco.getLastDailyAt()), true);
    }

    private String formatTimestamp(long timestamp) {
        return String.format("<t:%d>", timestamp);
    }
}