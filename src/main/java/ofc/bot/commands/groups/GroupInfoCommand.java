package ofc.bot.commands.groups;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@DiscordCommand(name = "group info", description = "Mostra informações sobre o seu grupo.", cooldown = 10)
public class GroupInfoCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupInfoCommand.class);
    private static final String RENT_AMOUNT_FIELD_NAME = "Aluguel 📅";
    private static final MessageEmbed.Field FAILED_CALCULATION_FIELD = new MessageEmbed.Field(RENT_AMOUNT_FIELD_NAME, "?", true);
    private final BankTransactionRepository bankTrRepo;
    private final OficinaGroupRepository grpRepo;

    public GroupInfoCommand(BankTransactionRepository bankTrRepo, OficinaGroupRepository grpRepo) {
        this.bankTrRepo = bankTrRepo;
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        int groupId = ctx.getOption("group", -1, OptionMapping::getAsInt);
        long userId = ctx.getUserId();
        Guild guild = ctx.getGuild();
        OficinaGroup group = groupId == -1 ? grpRepo.findByOwnerId(userId) : grpRepo.findById(groupId);

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        long roleId = group.getRoleId();
        Role role = guild.getRoleById(roleId);

        if (role == null)
            return Status.GROUP_ROLE_NOT_FOUND;

        EmbedBuilder embed = embed(role.getColorRaw(), guild, group);

        ctx.replyEmbeds(embed.build());

        // Lazy-update the "Rent" field
        if (group.isRentRecurring())
            updateEmbedRentField(ctx, group, guild, role, embed);

        return Status.OK;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "group", "O grupo que você deseja saber as informações", false, true)
        );
    }

    private EmbedBuilder embed(int color, Guild guild, OficinaGroup group) {
        EmbedBuilder builder = new EmbedBuilder();
        String hex = Integer.toHexString(color);
        String rent = group.isRentRecurring() ? "..." : "$0";
        long appreciation = -getAppreciation(group);
        String fmtApprec = '$' + Bot.fmtNum(appreciation);
        RentStatus rentStatus = group.getRentStatus();

        return builder
                .setTitle(group.getName())
                .setColor(color)
                .addField("🎨 Cor", hex, true)
                .addField("💳 Economia", group.getCurrency().getName(), true)
                .addField("💎 Valorização", fmtApprec, true)
                .addField(RENT_AMOUNT_FIELD_NAME, rent, true)
                .addField("🏡 Status de Aluguel", rentStatus.getDisplayStatus(), true)
                .setFooter(guild.getName(), guild.getIconUrl());
    }

    private void updateEmbedRentField(SlashCommandContext ctx, OficinaGroup group, Guild guild, Role role, EmbedBuilder embed) {
        List<MessageEmbed.Field> embedFields = embed.getFields();
        long roleId = role.getIdLong();
        int rentFieldIndex = getRentFieldIndex(embed);

        guild.findMembersWithRoles(role).onSuccess(members -> {
            long rent = group.calcRent(members);
            String fmtRent = String.format("$%s/mês", Bot.fmtNum(rent));

            embedFields.set(rentFieldIndex, new MessageEmbed.Field(RENT_AMOUNT_FIELD_NAME, fmtRent, true));
            ctx.replyEmbeds(embed.build());
        }).onError(err -> {
            LOGGER.error("Could not fetch members from role {}", roleId, err);
            embedFields.set(rentFieldIndex, FAILED_CALCULATION_FIELD);
            ctx.replyEmbeds(embed.build());
        });
    }

    private long getAppreciation(OficinaGroup group) {
        // Fetching items the user BOUGHT only after the group creation
        List<BankTransaction> items = bankTrRepo.findByItemTypesAndUserAfter(
                group.getTimeCreated(),
                group.getOwnerId(),
                TransactionType.ITEM_BOUGHT,
                StoreItemType.getGroupRefundable()
        );

        return items.stream()
                .mapToLong(BankTransaction::getAmount)
                .sum();
    }

    private int getRentFieldIndex(EmbedBuilder embed) {
        List<MessageEmbed.Field> fields = embed.getFields();
        return fields.size() - 2;
    }
}