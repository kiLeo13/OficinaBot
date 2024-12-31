package ofc.bot.commands.groups;

import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.economy.CurrencyType;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.Arrays;
import java.util.List;

/**
 * Group creation after user confirmation is handled at
 * {@link ofc.bot.listeners.discord.interactions.buttons.groups.GroupCreationHandler GroupCreationHandler}.
 */
@DiscordCommand(name = "group create", description = "Cria um grupo novo em seu nome.", cooldown = 60)
public class CreateGroupCommand extends SlashSubcommand {
    private final OficinaGroupRepository grpRepo;

    public CreateGroupCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        String name = ctx.getSafeOption("name", OptionMapping::getAsString).strip();
        String hexColor = ctx.getSafeOption("color", OptionMapping::getAsString);
        String emoji = ctx.getSafeOption("emoji", OptionMapping::getAsString);
        CurrencyType currency = ctx.getEnumOption("currency", CurrencyType.UNBELIEVABOAT, CurrencyType.class);
        Member member = ctx.getIssuer();
        Guild guild = ctx.getGuild();
        long userId = member.getIdLong();
        long guildId = guild.getIdLong();
        int color;

        if (hitMaxRoles(guild))
            return Status.GROUPS_CANNOT_BE_CREATED_AT_THE_MOMENT;

        if (EmojiManager.containsEmoji(name))
            return Status.GROUP_NAMES_CANNOT_CONTAIN_EMOJIS;

        if (!EmojiManager.isEmoji(emoji))
            return Status.EMOJI_OPTION_CAN_ONLY_CONTAIN_EMOJI;

        if (hasGroup(userId))
            return Status.USERS_CANNOT_HAVE_MULTIPLE_GROUPS;

        try {
            color = Integer.parseInt(hexColor, 16);
        } catch (NumberFormatException e) {
            return Status.INVALID_COLOR_PROVIDED;
        }

        boolean isRentRecurring = OficinaGroup.isRentRecurring(member);
        boolean hasFreeAccess = OficinaGroup.hasFreeAccess(member);
        int price = hasFreeAccess ? 0 : OficinaGroup.PRICE;
        float refundPercent = hasFreeAccess ? 0 : OficinaGroup.REFUND_PERCENT;
        RentStatus rentStatus = isRentRecurring ? RentStatus.TRIAL : RentStatus.FREE;
        // If all checks passed, we create sort of a partial group instance,
        // containing all the information we have so far.
        // We are not sending the group to the database as we must wait
        // for the user to confirm the purchase (handled at ofc.bot.listeners.buttons.groups.GroupCreationHandler).
        OficinaGroup group = new OficinaGroup(name, userId, guildId, rentStatus, hasFreeAccess)
                .setEmoji(emoji)
                .setCurrency(currency)
                .setAmountPaid(price)
                .setRefundPercent(refundPercent);

        MessageEmbed embed = embed(group, color, member);
        Button confirmButton = ButtonContextFactory.createGroupConfirmationButton(group, price, color);
        return ctx.create()
                .setActionRow(confirmButton)
                .setEmbeds(embed)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "name", "O nome do grupo. Você pode usar fontes custom, mas não pode usar emojis.", true)
                        .setRequiredLength(OficinaGroup.MIN_NAME_LENGTH, OficinaGroup.MAX_NAME_LENGTH),

                new OptionData(OptionType.STRING, "color", "A cor do grupo em formato HEX (sem o #).", true)
                        .setRequiredLength(6, 6),

                new OptionData(OptionType.STRING, "currency", "Qual o tipo de economia deve ser usada para efetuar cobranças nesse grupo.", true)
                        .addChoices(getCurrencyChoices()),

                new OptionData(OptionType.STRING, "emoji", "O emoji utilizado para criar chats e calls (forneça o emoji em si, ex: 💪).", true)
                        .setRequiredLength(1, 50)
        );
    }

    private MessageEmbed embed(OficinaGroup group, int color, Member owner) {
        EmbedBuilder builder = new EmbedBuilder();
        User user = owner.getUser();
        Guild guild = owner.getGuild();
        String hexColor = '#' + Integer.toHexString(color);

        return builder
                .setTitle(group.getName())
                .setDescription("Deseja confirmar a criação deste grupo?")
                .setColor(color)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter(guild.getName(), guild.getIconUrl())
                .addField("🎨 Cor", hexColor, true)
                .addField("💳 Economia", group.getCurrency().getName(), true)
                .addField("💰 Preço", Bot.fmtNum(group.getAmountPaid()), true)
                .build();
    }

    private boolean hasGroup(long userId) {
        return grpRepo.existsByOwnerId(userId);
    }

    private boolean hitMaxRoles(Guild guild) {
        List<Role> roles = guild.getRoles();
        // Yes, this is a magic value.
        // Since JDA does not provide a constant value for this, I am keeping it as a magic value.
        return roles.size() >= 250;
    }

    private List<Command.Choice> getCurrencyChoices() {
        return Arrays.stream(CurrencyType.values())
                .map((ct) -> new Command.Choice(ct.getName(), ct.toString()))
                .toList();
    }
}