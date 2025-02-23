package ofc.bot.handlers.interactions.modals.contexts;

import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import ofc.bot.handlers.interactions.InteractionSubmitContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class ModalSubmitContext extends InteractionSubmitContext<ModalContext, ModalInteraction> {

    public ModalSubmitContext(ModalContext context, ModalInteraction interaction) {
        super(context, interaction);
    }

    @Override
    public void ack(boolean ephemeral) {
        if (!isAcknowledged())
            getInteraction().deferReply(ephemeral).queue();
    }

    @NotNull
    public String getField(@NotNull String id) {
        String value = findField(id);

        if (value == null)
            throw new NoSuchElementException("No such modal field: " + id);

        return value;
    }

    public String getField(@NotNull String id, String defaultValue) {
        String value = findField(id);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    @Nullable
    public String findField(@NotNull String id) {
        ModalInteraction itr = getInteraction();
        ModalMapping value = itr.getValue(id);
        return value == null ? null : value.getAsString();
    }
}