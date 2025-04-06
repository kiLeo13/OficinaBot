package ofc.bot.commands.groups;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.RentStatus;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.Bot;
import ofc.bot.util.GroupHelper;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@DiscordCommand(name = "group info")
public class GroupInfoCommand extends SlashSubcommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupInfoCommand.class);
    private final OficinaGroupRepository grpRepo;

    public GroupInfoCommand(OficinaGroupRepository grpRepo) {
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
    protected void init() {
        setDesc("Mostra informações sobre o seu grupo.");
        setCooldown(30, TimeUnit.SECONDS);

        addOpt(OptionType.INTEGER, "group", "O grupo que você deseja saber as informações", false, true);
    }

    private MessageEmbed embed(int color, List<Member> members, Guild guild, OficinaGroup group) {
        EmbedBuilder builder = new EmbedBuilder();
        long rent = group.calcRawRent(members);
        long appreciation = calcExpenses(group);
        RentStatus rentStatus = group.getRentStatus();
        String hex = Bot.fmtColorHex(color);
        String fmtRent = String.format("%s/mês", Bot.fmtMoney(rent));
        String fmtApprec = Bot.fmtMoney(appreciation);
        String fmtMembers = Bot.fmtNum(members.size());
        String fmtTimestamp = String.format("<t:%d>", group.getTimeCreated());
        String fmtRentStatus = group.isRentLate() ? "⚠️ Atrasado" : rentStatus.getDisplayStatus();

        return builder
                .setTitle(group.getName())
                .setColor(color)
                .addField("🎨 Cor", hex, true)
                .addField("💳 Economia", group.getCurrency().getName(), true)
                .addField("💎 Gastos", fmtApprec, true)
                .addField("📅 Aluguel", fmtRent, true)
                .addField("👑 Dono", group.getOwnerAsMention(), true)
                .addField("🏡 Status de Aluguel", fmtRentStatus, true)
                .addField("👥 Membros", fmtMembers, true)
                .addField("📅 Criação", fmtTimestamp, true)
                .setFooter(guild.getName(), guild.getIconUrl())
                .build();
    }

    private long calcExpenses(OficinaGroup group) {
        return group.getAmountPaid() + GroupHelper.calcPurchases(group);
    }
}