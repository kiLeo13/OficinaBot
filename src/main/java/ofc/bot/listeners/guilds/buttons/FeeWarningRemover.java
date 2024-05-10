package ofc.bot.listeners.guilds.buttons;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ofc.bot.util.content.annotations.listeners.EventHandler;
import ofc.bot.databases.DBManager;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@EventHandler
public class FeeWarningRemover extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeeWarningRemover.class);

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (!event.getComponentId().equals("fee-warning"))
            return;

        User user = event.getUser();

        removeNotification(user.getIdLong());

        event.reply("Você não será mais avisado(a) sobre taxas de casamento.")
                .setEphemeral(true)
                .queue();

        LOGGER.info("User @{} has disabled the fee warning", user.getName());
    }

    private void removeNotification(long userId) {

        DSLContext ctx = DBManager.getContext();

        ctx.insertInto(table("disable_marriage_warning"))
                .set(field("user_id"), userId)
                .onDuplicateKeyIgnore()
                .execute();
    }
}