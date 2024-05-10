package ofc.bot.commands.economy.leaderboard;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;
import ofc.bot.handlers.buttons.BotButtonListener;
import ofc.bot.handlers.buttons.ButtonData;

@ButtonHandler(identity = "leaderboard")
public class ButtonOffsetUpdate implements BotButtonListener {

    @Override
    public void onClick(ButtonInteraction event, ButtonData data) {

        Message message = event.getMessage();
        Guild guild = event.getGuild();
        int page = data.valueInt();
        LeaderboardData leaderboard = BaseLeaderboard.retrieveLeaderboard(page);
        boolean hasMorePages = leaderboard.page() < leaderboard.maxPages();
        Button[] newButtons = BaseLeaderboard.generateButtons(page, hasMorePages);
        MessageEmbed newEmbed = BaseLeaderboard.embed(guild, leaderboard);

        message.editMessageEmbeds(newEmbed)
                .setActionRow(newButtons)
                .queue();
    }
}