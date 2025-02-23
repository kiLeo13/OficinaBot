package ofc.bot.handlers.interactions;

import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.util.content.annotations.listeners.InteractionHandler;

public interface InteractionListener<T extends InteractionSubmitContext<?, ?>> {

    InteractionResult onExecute(T ctx);

    default String getScope() {
        InteractionHandler ann = getAnn();
        return ann.scope();
    }

    default AutoResponseType getAutoResponseType() {
        InteractionHandler ann = getAnn();
        return ann.autoResponseType();
    }

    private InteractionHandler getAnn() {
        Class<? extends InteractionListener> clazz = this.getClass();
        InteractionHandler ann = clazz.getDeclaredAnnotation(InteractionHandler.class);

        if (ann == null)
            throw new IllegalStateException(
                    "Composed interaction listener " + clazz.getName() + " is not annotated with @" +
                    clazz.getName());

        return ann;
    }
}