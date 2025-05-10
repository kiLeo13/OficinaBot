package ofc.bot.handlers.commands.legacy.abstractions;

import net.dv8tion.jda.api.Permission;
import ofc.bot.handlers.commands.contexts.impl.MessageCommandContext;
import ofc.bot.handlers.commands.options.LegacyOption;
import ofc.bot.handlers.commands.slash.abstractions.ICommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class MessageCommand implements ICommand<MessageCommandContext, LegacyOption> {
    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern OPTIONAL_ARG_PATTERN = Pattern.compile("\\[[^\\[\\]]+\\]");
    private static final Pattern REQUIRED_ARG_PATTERN = Pattern.compile("<[^<>]+>");
    private final String declaration;
    private final List<Permission> permissions;

    public MessageCommand() {
        this.declaration = Bot.getSafeAnnotationValue(this, DiscordCommand.class, DiscordCommand::name);
        this.permissions = List.of(Bot.getAnnotationValue(this, DiscordCommand.class, DiscordCommand::permissions, new Permission[0]));
    }

    @NotNull
    @Override
    public final String getName() {
        return this.declaration.split(" ")[0];
    }

    @NotNull
    @Override
    public List<Permission> getPermissions() {
        return this.permissions;
    }

    @NotNull
    public final String getDeclaration() {
        return this.declaration;
    }

    @NotNull
    @Override
    public final List<LegacyOption> getOptions() {
        String[] parts = declaration.trim().split("\\s+");
        List<LegacyOption> options = new ArrayList<>();

        boolean seenOptional = false;
        boolean foundAnyArgs = false;

        for (int i = 1; i < parts.length; i++) {
            String token = parts[i];

            if (REQUIRED_ARG_PATTERN.matcher(token).matches()) {
                if (seenOptional) {
                    throw new IllegalArgumentException("Required arguments must come before optional ones.");
                }
                String name = token.substring(1, token.length() - 1);
                options.add(new LegacyOption(name, true));
                foundAnyArgs = true;
            } else if (OPTIONAL_ARG_PATTERN.matcher(token).matches()) {
                seenOptional = true;
                String name = token.substring(1, token.length() - 1);
                options.add(new LegacyOption(name, false));
                foundAnyArgs = true;
            }
        }

        if (!foundAnyArgs) {
            throw new IllegalStateException("No <required> or [optional] arguments found in declaration.");
        }

        return options;
    }
}