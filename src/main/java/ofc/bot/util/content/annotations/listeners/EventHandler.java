package ofc.bot.util.content.annotations.listeners;

import ofc.bot.handlers.EntityHandlersInitializers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes with this annotation will be registered as event listeners
 * by {@link net.dv8tion.jda.api.JDA#addEventListener(Object...) JDA.addEventListener(Object...)}
 * and must extend the {@link net.dv8tion.jda.api.hooks.ListenerAdapter ListenerAdapter}
 * class, otherwise, an {@link IllegalStateException} is thrown
 * by the {@link EntityHandlersInitializers EntityHandlersInitializers}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventHandler {}