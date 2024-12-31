package ofc.bot.commands.birthday;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.domain.entity.Birthday;
import ofc.bot.domain.sqlite.repository.BirthdayRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.embeds.EmbedFactory;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@DiscordCommand(
        name = "birthdays",
        description = "Veja o aniversário de todos os membros da staff.",
        permission = Permission.MANAGE_SERVER
)
public class BirthdaysCommand extends SlashCommand {
    private final BirthdayRepository bdayRepo;

    public BirthdaysCommand(BirthdayRepository bdayRepo) {
        this.bdayRepo = bdayRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        Month month = ctx.getEnumOption("month", LocalDate.now().getMonth(), Month.class);
        Guild guild = ctx.getGuild();
        List<Birthday> birthdays = bdayRepo.findByMonth(month);
        MessageEmbed embed = EmbedFactory.embedBirthdayList(birthdays, guild, month);
        List<Button> buttons = ButtonContextFactory.createBirthdayListButtons(month);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "month", "O mês dos aniversariantes.")
                        .addChoices(getMonthChoices())
        );
    }

    private List<Command.Choice> getMonthChoices() {
        return Stream.of(Month.values())
                .map(m -> new Command.Choice(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH), m.name()))
                .toList();
    }
}
