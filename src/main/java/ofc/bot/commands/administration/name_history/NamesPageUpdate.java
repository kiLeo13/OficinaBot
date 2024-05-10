package ofc.bot.commands.administration.name_history;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.handlers.buttons.BotButtonListener;
import ofc.bot.handlers.buttons.ButtonData;
import ofc.bot.util.Bot;
import ofc.bot.util.NameLogUtil;

@ButtonHandler(identity = "names")
public class NamesPageUpdate implements BotButtonListener {

    @Override
    public void onClick(ButtonInteraction event, ButtonData data) {

        Message message = event.getMessage();
        Guild guild = event.getGuild();
        String type = data.payload(String.class);
        long targetId = data.entity();
        int offset = data.valueInt();
        NamesHistoryData namesData = NameLogUtil.retrieveNamesOfUser(type, targetId, offset);
        boolean hasMorePages = namesData.page() < namesData.maxPages();
        Button[] newButtons = BaseNicknames.generateButtons(type, targetId, offset, hasMorePages);

        if (namesData.isEmpty()) {
            message.editMessage("Nenhum resultado encontrado")
                    .setReplace(true)
                    .queue();
            return;
        }

        Bot.fetchUser(targetId).queue((target) -> {

            MessageEmbed newEmbed = BaseNicknames.embed(namesData, guild, target);

            message.editMessageEmbeds(newEmbed)
                    .setComponents(ActionRow.of(newButtons))
                    .queue();

        }, (error) -> message.editMessage("Usuário não encontrado")
                .setReplace(true)
                .queue());
    }
}