package ofc.bot.handlers.commands.slash.containers;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.content.annotations.commands.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class CommandOptionsContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandOptionsContainer.class);
    private final List<OptionData> options;

    public CommandOptionsContainer() {
        this.options = resolveOptions();
    }

    /**
     * Gets an ordered list of command options for a slash subcommand.
     * This method accounts for the limitations of the Discord API, which does not allow
     * required options to be defined after optional ones.
     * <p>
     * The returned list is structured to ensure that all required options precede optional ones,
     * aligning with Discord API's requirements and avoiding common configuration errors.
     *
     * @return List<OptionData> - An ordered list of options for the slash subcommand,
     *                            structured to be exception-safe and compliant with Discord API standards.
     */
    public final List<OptionData> getOptions() {
        return this.options;
    }

    private List<OptionData> resolveOptions() {

        List<Field> optionFields = getOptionFields();

        if (optionFields.isEmpty())
            return List.of();

        return optionFields.stream()
                .map(this::getFieldAsOption)
                .filter(Objects::nonNull)
                .sorted((opt1, opt2) -> {

                    boolean opt1Required = opt1.isRequired();
                    boolean opt2Required = opt2.isRequired();

                    if (opt1Required == opt2Required)
                        return 0;

                    return opt1Required ? -1 : 1;
                })
                .toList();
    }

    private OptionData getFieldAsOption(Field field) {

        try {
            field.setAccessible(true);
            Object value = field.get(this);
            Option annotation = field.getAnnotation(Option.class);

            if (value instanceof OptionData data && annotation != null)
                return data.setRequired(annotation.required())
                        .setAutoComplete(annotation.autoComplete());

            return null;
        } catch (IllegalAccessException e) {
            LOGGER.error("Could not get value from field '" + field.getName() + "'", e);
            return null;
        } catch (IllegalStateException e) {
            LOGGER.error("Could not get annotation from option '" + field.getName() + "'", e);
            return null;
        }
    }

    private List<Field> getOptionFields() {

        Class<? extends CommandOptionsContainer> clazz = this.getClass();

        return Arrays.stream(clazz.getDeclaredFields())
                .filter((f) -> f.isAnnotationPresent(Option.class))
                .toList();
    }
}