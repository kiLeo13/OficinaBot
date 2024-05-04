package ofc.bot.commands.marriages.pagination.marriages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import ofc.bot.Main;
import ofc.bot.content.annotations.listeners.ButtonHandler;
import ofc.bot.handlers.buttons.BotButtonListener;
import ofc.bot.handlers.buttons.ButtonData;

@ButtonHandler(identity = "marriages")
public class MarriagesPageUpdate implements BotButtonListener {

    @Override
    public void onClick(ButtonInteraction event, ButtonData data) {

        Message message = event.getMessage();
        Guild guild = message.getGuild();
        int page = data.valueInt();
        MarriagesData marriagesData = MarriageListSubcommand.retrieveMarriageData(data.entity(), page);
        boolean hasMorePages = marriagesData.page() < marriagesData.maxPages();
        Button[] newButtons = MarriageListSubcommand.generateButtons(data.entity(), page, hasMorePages);

        Main.getApi().retrieveUserById(marriagesData.userId()).queue((target) -> {

            MessageEmbed newEmbed = MarriageListSubcommand.embed(guild, target, marriagesData);

            message.editMessageEmbeds(newEmbed)
                    .setComponents(ActionRow.of(newButtons))
                    .queue();

        }, (error) -> {
            message.editMessage("Usuário não encontrado.")
                    .setReplace(true)
                    .queue();
        });
    }
}