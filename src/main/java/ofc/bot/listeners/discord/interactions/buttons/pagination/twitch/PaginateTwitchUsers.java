package ofc.bot.listeners.discord.interactions.buttons.pagination.twitch;

import com.github.twitch4j.helix.domain.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.handlers.interactions.AutoResponseType;
import ofc.bot.handlers.interactions.EntityContextFactory;
import ofc.bot.handlers.interactions.InteractionListener;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.util.Scopes;
import ofc.bot.util.content.annotations.listeners.InteractionHandler;
import ofc.bot.util.embeds.EmbedFactory;

import java.util.List;

@InteractionHandler(scope = Scopes.Twitch.PAGINATE_USERS, autoResponseType = AutoResponseType.DEFER_EDIT)
public class PaginateTwitchUsers implements InteractionListener<ButtonClickContext> {

    @Override
    public InteractionResult onExecute(ButtonClickContext ctx) {
        List<User> users = ctx.get("users");
        int pageIndex = ctx.get("page_index");
        Guild guild = ctx.getGuild();
        User cursor = users.get(pageIndex);

        MessageEmbed embed = EmbedFactory.embedTwitchUser(guild, cursor);
        List<Button> buttons = EntityContextFactory.createTwitchUsersButtons(users, pageIndex);

        return ctx.create()
                .setEmbeds(embed)
                .setActionRow(buttons)
                .edit();
    }
}