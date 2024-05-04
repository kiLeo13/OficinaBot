package ofc.bot.handlers.commands.slash.containers;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import ofc.bot.handlers.commands.slash.innercommands.SlashSubcommand;

import java.util.Collection;

/**
 * Every class implementing this interface indicates
 * they have Subcommands.
 * <p>
 * Currently available only for {@link ofc.bot.handlers.commands.slash.SlashCommand SlashCommand} and
 * {@link SlashSubcommand SlashSubcommand}.
 */
public interface SubcommandContainer {

    Collection<? extends SlashSubcommand> getSubcommands();

    SlashSubcommand getSubcommand(String name);

    boolean hasSubcommands();

    default Collection<? extends SubcommandData> getSubcommandsData() {

        return getSubcommands().stream()
                .map((cmd) -> new SubcommandData(cmd.getName(), cmd.getDescription())
                        .addOptions(cmd.getOptions())
                )
                .toList();
    }
}