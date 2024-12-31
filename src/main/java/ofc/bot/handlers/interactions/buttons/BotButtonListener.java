package ofc.bot.handlers.interactions.buttons;

import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.util.content.annotations.listeners.ButtonHandler;

public interface BotButtonListener {
    InteractionResult onClick(ButtonClickContext ctx);

    default String getScope() {
        ButtonHandler ann = getAnn();
        return ann.scope();
    }

    default AutoResponseType getAutoResponseType() {
        ButtonHandler ann = getAnn();
        return ann.autoResponseType();
    }

    private ButtonHandler getAnn() {
        Class<? extends BotButtonListener> clazz = this.getClass();
        ButtonHandler ann = clazz.getDeclaredAnnotation(ButtonHandler.class);

        if (ann == null)
            throw new IllegalStateException("Bot button listener " + clazz.getName() + " is not annotated with @ButtonHandler");

        return ann;
    }
}