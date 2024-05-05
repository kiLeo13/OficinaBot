package ofc.bot.handlers;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.Main;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.listeners.ButtonHandler;
import ofc.bot.content.annotations.listeners.EventHandler;
import ofc.bot.handlers.buttons.BotButtonListener;
import ofc.bot.handlers.buttons.ButtonClickHandler;
import ofc.bot.handlers.commands.CommandsRegistryManager;
import ofc.bot.handlers.commands.slash.SlashCommand;
import org.quartz.SchedulerException;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.Set;

/**
 * This is an utility class where it's main intention is to
 * register commands, listeners and jobs dynamically,
 * without having to call the declared/default constructor of each desired class.
 * <p>
 * This will always instantiate classes by their default constructor ({@code new Class()}),
 * without any parameters.
 */
public class EntityHandlersInitializers {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHandlersInitializers.class);
    private static final Reflections reflections = new Reflections(new ConfigurationBuilder().forPackage("ofc.bot"));

    public static void initializeCronJobs() throws ParseException {

        try {
            SchedulerRegistryManager.initializeSchedulers();
        } catch (SchedulerException e) {
            LOGGER.error("Could not initialize schedulers", e);
        }
    }

    public static void registerButtons() {

        Class<ButtonHandler> annotation = ButtonHandler.class;
        Set<Class<?>> buttonHandlers = reflections.getTypesAnnotatedWith(annotation);

        for (Class<?> handler : buttonHandlers) {

            if (!BotButtonListener.class.isAssignableFrom(handler))
                throw new IllegalStateException("Class '" + handler.getName() + "' annotated with ButtonClickHandler does not implement the " + BotButtonListener.class.getName() + " interface");

            try {
                Constructor<?> constructor = handler.getConstructor();
                constructor.setAccessible(true);

                BotButtonListener listener = (BotButtonListener) constructor.newInstance();
                ButtonHandler buttonAnnotation = handler.getDeclaredAnnotation(ButtonHandler.class);
                String identity = buttonAnnotation.identity();

                if (identity.isBlank())
                    throw new IllegalArgumentException("ButtonClickHandler identity cannot be empty at class " + handler.getName());

                ButtonClickHandler.registerButton(identity, listener);

            } catch (InstantiationException e) {
                LOGGER.error("Could not instantiate button listener at {}", handler.getName(), e);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Could not access button listener at {}", handler.getName(), e);
            }
        }
    }

    public static void registerListeners() {

        JDA api = Main.getApi();
        Class<EventHandler> annotation = EventHandler.class;
        Set<Class<?>> eventHandlers = reflections.getTypesAnnotatedWith(annotation);

        for (Class<?> handler : eventHandlers) {

            if (!ListenerAdapter.class.isAssignableFrom(handler))
                throw new IllegalStateException("Class '" + handler.getName() + "' annotated with EventHandler does not extend the " + ListenerAdapter.class.getName() + " abstract class");

            try {
                Constructor<?> constructor = handler.getConstructor();
                constructor.setAccessible(true);

                Object listener = constructor.newInstance();

                api.addEventListener(listener);

            } catch (InstantiationException e) {
                LOGGER.error("Could not instantiate event listener at {}", handler.getName(), e);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Could not access event listener at {}", handler.getName(), e);
            }
        }
    }

    public static void registerCommands() {

        CommandsRegistryManager.Builder builder = CommandsRegistryManager.newBuilder();
        Class<DiscordCommand> annotation = DiscordCommand.class;
        Set<Class<?>> eventHandlers = reflections.getTypesAnnotatedWith(annotation);

        for (Class<?> cmdClass : eventHandlers) {

            // Since Subcommands also use the same @DiscordCommand annotation,
            // we do not throw an exception here.
            // Instead, we assume that classes annotated with @DiscordCommand but not extending SlashCommand
            // are instances of SlashSubcommand, which are handled by the SlashCommand instance
            // through its superclass constructor.
            if (!SlashCommand.class.isAssignableFrom(cmdClass))
                continue;

            try {
                Constructor<?> constructor = cmdClass.getConstructor();
                constructor.setAccessible(true);

                SlashCommand cmd = (SlashCommand) constructor.newInstance();

                builder.addCommand(cmd);

            } catch (InstantiationException e) {
                LOGGER.error("Could not instantiate event listener at {}", cmdClass.getName(), e);
            } catch (ReflectiveOperationException e) {
                LOGGER.error("Could not access event listener at {}", cmdClass.getName(), e);
            }
        }

        builder.commit();
    }
}