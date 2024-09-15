package ofc.bot.listeners.buttons.pagination;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import ofc.bot.Main;
import ofc.bot.commands.marriages.pagination.proposals.BaseProposals;
import ofc.bot.commands.marriages.pagination.proposals.ProposalsData;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.handlers.buttons.BotButtonListener;
import ofc.bot.handlers.buttons.ButtonData;

@ButtonHandler(identity = "proposals")
public class ProposalsPageUpdate implements BotButtonListener {

    @Override
    public void onClick(ButtonInteraction event, ButtonData data) {

        int page = data.valueInt();
        long userId = data.entity();
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        String type = data.payload(String.class);
        ProposalsData proposals = BaseProposals.retrieveProposals(type, userId, page);
        boolean hasMorePages = proposals.page() < proposals.maxPages();
        Button[] buttons = BaseProposals.generateButtons(page, userId, hasMorePages, type);

        Main.getApi().retrieveUserById(userId).queue((user) -> {

            MessageEmbed embed = BaseProposals.embed(guild, user, proposals);

            message.editMessageEmbeds(embed)
                    .setActionRow(buttons)
                    .queue();
        }, (e) -> {
            message.editMessage("Usuário não encontrado.")
                    .setReplace(true)
                    .queue();
        });
    }
}
