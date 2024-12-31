package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.PaymentManager;
import ofc.bot.handlers.economy.PaymentManagerProvider;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;

@ButtonHandler(
        scope = OficinaGroup.GROUP_DELETE_BUTTON_SCOPE,
        autoResponseType = AutoResponseType.THINKING_EPHEMERAL
)
public class GroupDeletionHandler implements BotButtonListener {
    private final OficinaGroupRepository grpRepo;

    public GroupDeletionHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        long userId = ctx.getUserId();
        Guild guild = ctx.getGuild();
        OficinaGroup group = grpRepo.findByOwnerId(userId);
        Role groupRole = guild.getRoleById(group.getRoleId());
        int refund = ctx.get("refund");

        handleDeletion(userId, refund, group, guild, groupRole);

        ctx.disable();

        // Dispatch event for bank transaction logging
        dispatchGroupDeletionEvent(group, refund);
        return Status.GROUP_SUCCESSFULLY_DELETED.args(group.getName());
    }

    private void handleDeletion(long userId, int refund, OficinaGroup group, Guild guild, Role groupRole) {
        PaymentManager bank = PaymentManagerProvider.fromType(group.getCurrency());
        long guildId = guild.getIdLong();

        if (group.hasChannels())
            deleteChannels(group);

        if (groupRole != null)
            groupRole.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_ROLE));

        if (refund != 0)
            bank.update(userId, guildId, 0, refund, "Refund of group deletion");

        grpRepo.delete(group);
    }

    private void dispatchGroupDeletionEvent(OficinaGroup group, int refund) {
        BankTransaction tr = new BankTransaction(group.getOwnerId(), refund, group.getCurrency(), TransactionType.ITEM_SOLD, StoreItemType.GROUP);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }

    private void deleteChannels(OficinaGroup group) {
        VoiceChannel voiceChan = group.getVoiceChannel();
        TextChannel textChan = group.getTextChannel();

        if (textChan != null)
            textChan.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL));

        if (voiceChan != null)
            voiceChan.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL));
    }
}