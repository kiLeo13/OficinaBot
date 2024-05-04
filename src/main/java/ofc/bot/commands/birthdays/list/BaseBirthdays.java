package ofc.bot.commands.birthdays.list;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.BirthdayRecord;
import ofc.bot.handlers.buttons.ButtonManager;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static ofc.bot.databases.entities.tables.Birthdays.BIRTHDAYS;
import static org.jooq.impl.DSL.day;
import static org.jooq.impl.DSL.month;

@DiscordCommand(name = "birthdays", description = "Veja o aniversário de todos os membros da staff.")
@CommandPermission(Permission.MANAGE_SERVER)
public class BaseBirthdays extends SlashCommand {
    public static final String ICON_URL = "https://cdn.discordapp.com/attachments/631974560605929493/1173787589065592882/calendar.png";
    private static final String BIRTHDAYS_FORMAT = "- %s  <@%d>\n";
    private static final String[] MONTHS = {
            "Janeiro", "Fevereiro",
            "Março", "Abril",
            "Maio", "Junho",
            "Julho", "Agosto",
            "Setembro", "Outubro",
            "Novembro", "Dezembro"
    };

    @Option
    private static final OptionData MONTH = new OptionData(OptionType.STRING, "month", "O mês dos aniversariantes")
            .addChoices(getChoices());

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        int month = ctx.getOption("month", LocalDateTime.now().getMonthValue() - 1, OptionMapping::getAsInt);
        Guild guild = ctx.getGuild();
        List<BirthdayRecord> birthdays = retrieveBirthdays(month);
        MessageEmbed embed = embed(birthdays, guild, month);
        Button[] buttons = generateButtons(month);

        ctx.reply()
                .setEmbeds(embed)
                .setComponents(ActionRow.of(buttons))
                .send();

        return Status.PASSED;
    }

    protected static Button[] generateButtons(int currentMonth) {

        int previousMonth = currentMonth - 1;
        int nextMonth = currentMonth + 1;
        boolean hasPrevious = currentMonth > 0;
        boolean hasNext = currentMonth < 11;

        String previousID = UUID.randomUUID().toString();
        String nextID = UUID.randomUUID().toString();
        Button previous = Button.primary(previousID, Emoji.fromUnicode("◀"))
                .withDisabled(!hasPrevious);
        Button next = Button.primary(nextID, Emoji.fromUnicode("▶"))
                .withDisabled(!hasNext);

        ButtonManager.create(previousID)
                .setValueInt(previousMonth)
                .setIdentity("birthdays")
                .insert();

        ButtonManager.create(nextID)
                .setValueInt(nextMonth)
                .setIdentity("birthdays")
                .insert();

        return new Button[]{ previous, next };
    }

    protected static MessageEmbed embed(List<BirthdayRecord> birthdays, Guild guild, int monthIndex) {

        EmbedBuilder builder = new EmbedBuilder();
        String month = MONTHS[monthIndex];
        String formattedBirthdays = Bot.format(
                birthdays,
                (b) -> String.format(BIRTHDAYS_FORMAT, b.getPrettyDate(), b.getUserId())
        ).strip();
        boolean empty = birthdays.isEmpty();

        builder
                .setAuthor("Aniversários", null, ICON_URL)
                .setDescription("## " + month + "\n\n" + (empty ? "*Nenhum aniversariante.*" : formattedBirthdays))
                .setColor(Bot.Colors.DISCORD)
                .setFooter(guild.getName(), guild.getIconUrl());

        return builder.build();
    }

    protected static List<BirthdayRecord> retrieveBirthdays(int month) {

        DSLContext ctx = DBManager.getContext();

        return ctx.selectFrom(BIRTHDAYS)
                .where(month(BIRTHDAYS.BIRTHDAY).eq(month + 1))
                .groupBy(BIRTHDAYS.USER_ID)
                .orderBy(day(BIRTHDAYS.BIRTHDAY).asc())
                .fetch();
    }

    private static List<Command.Choice> getChoices() {

        return Stream.of(Month.values())
                .map(m -> new Command.Choice(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH), m.getValue() - 1))
                .toList();
    }
}
