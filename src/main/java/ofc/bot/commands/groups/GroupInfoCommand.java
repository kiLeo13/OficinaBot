package ofc.bot.commands.groups;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
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

@DiscordCommand(name = "group info", description = "Mostra informações sobre o seu grupo.", cooldown = 30)
public class GroupInfoCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupInfoCommand.class);
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

        ctx.ack();
        guild.findMembersWithRoles(role).onSuccess((members) -> {
            MessageEmbed embed = embed(role.getColorRaw(), members, guild, group);

            ctx.replyEmbeds(embed);
        }).onError((err) -> {
            LOGGER.error("Could not fetch members from group {}", group.getId(), err);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });

        return Status.OK;
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "group", "O grupo que você deseja saber as informações", false, true)
        );
    }

    private MessageEmbed embed(int color, List<Member> members, Guild guild, OficinaGroup group) {
        EmbedBuilder builder = new EmbedBuilder();
        long rent = group.calcRent(members);
        long appreciation = -getAppreciation(group);
        String hex = Bot.fmtColorHex(color);
        String fmtRent = String.format("%s/mês", Bot.fmtMoney(rent));
        String fmtApprec = Bot.fmtMoney(appreciation);
        String fmtMembers = Bot.fmtNum(members.size());
        RentStatus rentStatus = group.getRentStatus();
        String fmtTimestamp = String.format("<t:%d>", group.getTimeCreated());

        return builder
                .setTitle(group.getName())
                .setColor(color)
                .addField("🎨 Cor", hex, true)
                .addField("💳 Economia", group.getCurrency().getName(), true)
                .addField("💎 Valorização", fmtApprec, true)
                .addField("📅 Aluguel", fmtRent, true)
                .addField("👑 Dono", group.getOwnerAsMention(), true)
                .addField("🏡 Status de Aluguel", rentStatus.getDisplayStatus(), true)
                .addField("👥 Membros", fmtMembers, true)
                .addField("📅 Criação", fmtTimestamp, true)
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
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
}