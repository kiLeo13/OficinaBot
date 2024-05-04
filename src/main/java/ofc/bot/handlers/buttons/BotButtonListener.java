package ofc.bot.handlers.buttons;

import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

public interface BotButtonListener {

    void onClick(ButtonInteraction event, ButtonData buttonData);
}