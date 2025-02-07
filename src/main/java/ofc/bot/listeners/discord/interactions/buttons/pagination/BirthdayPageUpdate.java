package ofc.bot.listeners.discord.interactions.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.Birthday;
import ofc.bot.domain.sqlite.repository.BirthdayRepository;
import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.time.Month;
import java.util.List;

@ButtonHandler(scope = Scopes.Misc.PAGINATE_BIRTHDAYS, autoResponseType = AutoResponseType.DEFER_EDIT)
public class BirthdayPageUpdate implements BotButtonListener {
    private final BirthdayRepository bdayRepo;

    public BirthdayPageUpdate(BirthdayRepository bdayRepo) {
        this.bdayRepo = bdayRepo;
    }

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        Guild guild = ctx.getGuild();
        Month month = ctx.get("month");
        List<Birthday> birthdays = bdayRepo.findByMonth(month);
        List<Button> newButtons = ButtonContextFactory.createBirthdayListButtons(month);
        MessageEmbed newEmbed = EmbedFactory.embedBirthdayList(birthdays, guild, month);

        ctx.editEmbeds(newEmbed)
                .setActionRow(newButtons)
                .queue();

        return Status.OK;
    }
}