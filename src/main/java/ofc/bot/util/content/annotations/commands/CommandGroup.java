package ofc.bot.util.content.annotations.commands;

import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommandGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the {@link SlashSubcommandGroup SubcommandGroup}
 * of a Subcommand.
 * <p>
 * This annotation an also be used to declare a group inside an implementation
 * of the {@link ofc.bot.handlers.commands.slash.SlashCommand SlashCommand} class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface CommandGroup {

    /**
     * The name of the {@link SlashSubcommandGroup Subcommand Group}
     * where the current command is in.
     * <p>
     * This value defaults to an empty String if the annotation is being used to declare the group
     * inside a {@link ofc.bot.handlers.commands.slash.SlashCommand SlashCommand} implementation class.
     *
     * @return the name of the {@link SlashSubcommandGroup Subcommand Group}.
     */
    String value() default "";
}