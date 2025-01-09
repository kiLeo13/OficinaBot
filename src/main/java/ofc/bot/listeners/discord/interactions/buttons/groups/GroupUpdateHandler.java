package ofc.bot.listeners.discord.interactions.buttons.groups;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import ofc.bot.domain.entity.BankTransaction;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.entity.enums.TransactionType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.events.entities.BankTransactionEvent;
import ofc.bot.events.eventbus.EventBus;
import ofc.bot.handlers.economy.*;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;

@ButtonHandler(
        scope = OficinaGroup.GROUP_UPDATE_BUTTON_SCOPE,
        autoResponseType = AutoResponseType.THINKING
)
public class GroupUpdateHandler implements BotButtonListener {
    private final OficinaGroupRepository grpRepo;

    public GroupUpdateHandler(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        OficinaGroup group = ctx.get("group");
        String newName = ctx.find("new_name");
        Guild guild = ctx.getGuild();
        PaymentManager bank = PaymentManagerProvider.fromType(group.getCurrency());
        long ownerId = group.getOwnerId();
        long guildId = guild.getIdLong();
        int newColor = ctx.get("new_color");
        int price = ctx.get("amount");
        boolean changedName = newName != null && !newName.isBlank();

        if (changedName)
            group.setName(newName);

        BankAction chargeAction = bank.charge(ownerId, guildId, 0, price, "Group data updated");
        if (!chargeAction.isOk()) {
            return Status.INSUFFICIENT_BALANCE;
        }

        modifyRole(guild, group, newColor);

        if (changedName)
            modifyChannels(group);

        grpRepo.upsert(group.tickUpdate());

        ctx.disable();
        dispatchGroupUpdateEvent(group.getCurrency(), ownerId, price);
        return Status.GROUP_SUCCESSFULLY_UPDATED;
    }

    private void modifyRole(Guild guild, OficinaGroup group, int newColor) {
        long roleId = group.getRoleId();
        Role role = guild.getRoleById(roleId);
        String roleName = String.format(OficinaGroup.ROLE_NAME_FORMAT, group.getName());

        if (role == null) return;

        role.getManager()
                .setName(roleName)
                .setColor(newColor == -1 ? role.getColorRaw() : newColor)
                .queue();
    }

    private void modifyChannels(OficinaGroup group) {
        TextChannel textChan = group.getTextChannel();
        VoiceChannel voiceChan = group.getVoiceChannel();

        if (textChan != null)
            textChan.getManager()
                    .setName(group.getTextChannelName())
                    .queue();

        if (voiceChan != null)
            voiceChan.getManager()
                    .setName(group.getVoiceChannelName())
                    .queue();
    }

    private void dispatchGroupUpdateEvent(CurrencyType currency, long buyerId, int price) {
        BankTransaction tr = new BankTransaction(buyerId, -price, currency, TransactionType.ITEM_BOUGHT, StoreItemType.UPDATE_GROUP);
        EventBus.dispatchEvent(new BankTransactionEvent(tr));
    }
}
