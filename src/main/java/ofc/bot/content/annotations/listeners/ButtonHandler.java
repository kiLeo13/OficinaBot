package ofc.bot.content.annotations.listeners;

import ofc.bot.handlers.EntityHandlersInitializers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes with this annotation will be registered as listeners
 * only for {@link net.dv8tion.jda.api.interactions.components.buttons.Button Button} clicks
 * and must implement the {@link ofc.bot.handlers.buttons.BotButtonListener BotButtonListener}
 * interface, otherwise, an {@link IllegalStateException} is thrown
 * by the {@link EntityHandlersInitializers EntityHandlersInitializers}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ButtonHandler {

    String identity();
}