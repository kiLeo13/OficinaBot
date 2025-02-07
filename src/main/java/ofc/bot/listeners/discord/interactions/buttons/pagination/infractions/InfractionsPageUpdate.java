package ofc.bot.listeners.discord.interactions.buttons.pagination.infractions;

import ofc.bot.handlers.interactions.buttons.AutoResponseType;
import ofc.bot.handlers.interactions.buttons.BotButtonListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;

@ButtonHandler(scope = Scopes.Punishments.VIEW_INFRACTIONS, autoResponseType = AutoResponseType.DEFER_EDIT)
public class InfractionsPageUpdate implements BotButtonListener {

    @Override
    public InteractionResult onClick(ButtonClickContext ctx) {
        return null;
    }
}
