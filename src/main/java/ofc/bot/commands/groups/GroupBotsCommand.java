package ofc.bot.commands.groups;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.GroupBot;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.sqlite.repository.GroupBotRepository;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@DiscordCommand(name = "group bots", description = "Adicione bots ao seu grupo.")
public class GroupBotsCommand extends SlashSubcommand {
    private final GroupBotRepository grpBotRepo;
    private final OficinaGroupRepository grpRepo;

    public GroupBotsCommand(GroupBotRepository grpBotRepo, OficinaGroupRepository grpRepo) {
        this.grpBotRepo = grpBotRepo;
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long userId = ctx.getUserId();
        int botRowId = ctx.getSafeOption("bot", OptionMapping::getAsInt);
        GroupBot bot = grpBotRepo.findById(botRowId);
        OficinaGroup group = grpRepo.findByOwnerId(userId);
        Member issuer = ctx.getIssuer();

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        TextChannel textChan = group.getTextChannel();
        if (textChan == null)
            return Status.YOUR_GROUP_DOES_NOT_HAVE_TEXT_CHANNEL;

        if (bot == null)
            return Status.GROUP_BOT_NOT_FOUND;

        StoreItemType itemType = StoreItemType.ADDITIONAL_BOT;
        boolean isFree = group.hasFreeAccess();
        int price = isFree ? 0 : itemType.getPrice();
        Button confirm = ButtonContextFactory.createGroupBotConfirmationButton(group, bot, price);
        MessageEmbed embed = EmbedFactory.embedItemPurchase(issuer, group, price, itemType);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(confirm)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "bot", "O bot para ser adicionado ao grupo.", true, true)
        );
    }
}