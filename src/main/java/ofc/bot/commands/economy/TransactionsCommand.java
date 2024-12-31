package ofc.bot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(name = "transactions", description = "Mostra o histórico de transações.")
public class TransactionsCommand extends SlashCommand {
    private final BankTransactionRepository bankTrRepo;

    public TransactionsCommand(BankTransactionRepository bankTrRepo) {
        this.bankTrRepo = bankTrRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long userId = ctx.getUserId();
        int pageIndex = ctx.getOption("page", 1, OptionMapping::getAsInt) - 1;
        boolean hasChatMoney = ctx.getOption("include-chatmoney", false, OptionMapping::getAsBoolean);
        boolean crossEconomy = ctx.getOption("cross-economy", true, OptionMapping::getAsBoolean);



        MessageEmbed embed = embed();
        return ctx.replyEmbeds(embed);
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.BOOLEAN, "include-chatmoney", "Se devemos incluir registros de chat-money na lista (padrão: False)."),
                new OptionData(OptionType.BOOLEAN, "cross-economy", "Se devemos incluir meros registros de outras economias na lista (padrão: True)."),
                new OptionData(OptionType.INTEGER, "page", "A página do histórico de transações.")
                        .setRequiredRange(1, Integer.MAX_VALUE)
        );
    }

    private MessageEmbed embed() {
        EmbedBuilder builder = new EmbedBuilder();
        return builder.build();
    }
}