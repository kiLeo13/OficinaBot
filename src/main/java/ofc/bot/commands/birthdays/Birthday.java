package ofc.bot.commands.birthdays;

import net.dv8tion.jda.api.Permission;
import ofc.bot.commands.birthdays.subcommands.BirthdayAdd;
import ofc.bot.commands.birthdays.subcommands.BirthdayRemove;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.slash.SlashCommand;

import java.time.format.DateTimeFormatter;

@DiscordCommand(name = "birthday", description = "Gerencia o registro dos aniversários dos staffs.")
@CommandPermission(Permission.MANAGE_SERVER)
public class Birthday extends SlashCommand {
    public static final DateTimeFormatter END_USER_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Birthday() {
        super(
                new BirthdayAdd(),
                new BirthdayRemove()
        );
    }
}