package ofc.bot.commands.groups.member;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@DiscordCommand(name = "group member add")
public class AddGroupMemberCommand extends SlashSubcommand {
    private final BankTransactionRepository bankTrRepo;
    private final OficinaGroupRepository grpRepo;

    public AddGroupMemberCommand(BankTransactionRepository bankTrRepo, OficinaGroupRepository grpRepo) {
        this.bankTrRepo = bankTrRepo;
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long issuerId = ctx.getUserId();
        Member issuer = ctx.getIssuer();
        Member newMember = ctx.getOption("member", OptionMapping::getAsMember);
        Guild guild = ctx.getGuild();
        OficinaGroup group = grpRepo.findByOwnerId(issuerId);

        if (newMember == null)
            return Status.MEMBER_NOT_FOUND;

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        if (newMember.getUser().isBot())
            return Status.CANNOT_ADD_BOTS_TO_GROUP;

        long groupRoleId = group.getRoleId();
        Role groupRole = guild.getRoleById(groupRoleId);

        // Also impossible
        if (groupRole == null)
            return Status.GROUP_ROLE_NOT_FOUND;

        if (newMember.getRoles().contains(groupRole))
            return Status.MEMBER_ALREADY_IN_THE_GROUP;

        boolean hasFreeSlots = hasFreeSlots(group);
        boolean targetHasFreeAccess = OficinaGroup.hasFreeAccess(newMember);
        int price = group.hasFreeAccess() || targetHasFreeAccess || hasFreeSlots
                ? 0
                : StoreItemType.GROUP_SLOT.getPrice();
        Button confirm = EntityContextFactory.createAddGroupMemberConfirm(group, newMember, price);
        MessageEmbed embed = EmbedFactory.embedGroupMemberAdd(issuer, group, newMember, price);
        return ctx.create()
                .setActionRow(confirm)
                .setEmbeds(embed)
                .send();
    }

    @Override
    protected void init() {
        setDesc("Adiciona um membro ao seu grupo.");

        addOpt(OptionType.USER, "member", "O membro que você deseja adicionar no seu grupo.", true);
    }

    private boolean hasFreeSlots(OficinaGroup group) {
        long timestamp = group.getTimeCreated();
        List<BankTransaction> trs = bankTrRepo.findByItemTypesAndUserAfter(
                timestamp,
                group.getOwnerId(),
                TransactionType.ITEM_BOUGHT,
                StoreItemType.GROUP_SLOT
        );

        return trs.size() < OficinaGroup.INITIAL_SLOTS;
    }
}