package ofc.bot.commands;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.Permission;
import ofc.bot.internal.data.BotFiles;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import ofc.bot.util.content.annotations.commands.CommandPermission;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import ofc.bot.util.Bot;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

@DiscordCommand(name = "tumaes", description = "GIFs do toquinho.")
@CommandPermission(Permission.MESSAGE_MANAGE)
public class Tumaes extends SlashCommand {
    private static final File file = new File(BotFiles.DIR_CONTENT, "tumaes.json");
    private static final Random random = new Random();
    private static final Gson gson = new Gson();

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        String gif = getGif();

        if (gif == null)
            return Status.NO_GIF_WAS_FOUND;

        ctx.reply(gif);

        return Status.PASSED;
    }

    private String getGif() {

        String json = Bot.readFile(file);
        TypeToken<Map<String, List<String>>> token = new TypeToken<>(){};
        Map<String, List<String>> mapping = gson.fromJson(json, token);
        List<String> gifs = mapping.get("gifs");

        if (gifs == null || gifs.isEmpty())
            return null;

        int index = random.nextInt(gifs.size());

        return gifs.get(index);
    }
}