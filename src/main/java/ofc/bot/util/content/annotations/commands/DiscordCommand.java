package ofc.bot.util.content.annotations.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DiscordCommand {
    String name();
    String description();

    /**
     * The amount of seconds that EACH user must wait before
     * using the same command again.
     *
     * @return The cooldown in seconds.
     */
    int cooldown() default 0;
}