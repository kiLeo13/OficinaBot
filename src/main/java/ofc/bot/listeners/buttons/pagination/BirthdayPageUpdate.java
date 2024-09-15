package ofc.bot.listeners.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import ofc.bot.commands.birthdays.BaseBirthdays;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.databases.entities.records.BirthdayRecord;
import ofc.bot.handlers.buttons.BotButtonListener;
import ofc.bot.handlers.buttons.ButtonData;

import java.util.List;

@ButtonHandler(identity = "birthdays")
public class BirthdayPageUpdate implements BotButtonListener {

    @Override
    public void onClick(ButtonInteraction event, ButtonData data) {

        Message message = event.getMessage();
        Guild guild = event.getGuild();
        int month = data.valueInt();
        List<BirthdayRecord> birthdays = BaseBirthdays.retrieveBirthdays(month);
        Button[] newButtons = BaseBirthdays.generateButtons(month);
        MessageEmbed newEmbed = BaseBirthdays.embed(birthdays, guild, month);

        message.editMessageEmbeds(newEmbed)
                .setComponents(ActionRow.of(newButtons))
                .queue();
    }
}