package ofc.bot.handlers.interactions.buttons.contexts;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import ofc.bot.handlers.interactions.AcknowledgeableAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ButtonClickContext extends AcknowledgeableAction<ButtonInteraction> {
    private final ButtonContext buttonContext;

    public ButtonClickContext(ButtonContext data, ButtonInteraction interaction) {
        super(interaction);
        this.buttonContext = data;
    }

    @Override
    public void ack(boolean ephemeral) {
        if (!isAcknowledged())
            getInteraction().deferReply(ephemeral).queue();
    }

    public void ackEdit() {
        if (!isAcknowledged())
            getInteraction().deferEdit().queue();
    }

    /**
     * Disables the button the user has clicked.
     */
    public void disable() {
        List<Button> buttons = new ArrayList<>(getMessage().getButtons());
        int btnIndex = findTargetButtonIndex();

        Button button = buttons.get(btnIndex);
        buttons.set(btnIndex, button.asDisabled());

        editMessageComponents(ActionRow.of(buttons))
                .queue();
    }

    public MessageEditAction editMessage(String content) {
        return getMessage().editMessage(content);
    }

    public MessageEditAction editEmbeds(MessageEmbed... embeds) {
        return getMessage().editMessageEmbeds(embeds);
    }

    public MessageEditAction editMessageComponents(LayoutComponent... components) {
        return getMessage().editMessageComponents(components);
    }

    public Button getButton() {
        return getInteraction().getButton();
    }

    public Message getMessage() {
        return getInteraction().getMessage();
    }

    public ButtonContext getData() {
        return this.buttonContext;
    }

    public long getAuthorId() {
        return this.buttonContext.getAuthorId();
    }

    @NotNull
    public <T> T get(String key) {
        return this.buttonContext.get(key);
    }

    @Nullable
    public <T> T find(String key) {
        return this.buttonContext.find(key);
    }

    @Contract("null, null -> null")
    public <T> T getOrDefault(String key, T defaultValue) {
        return this.buttonContext.getOrDefault(key, defaultValue);
    }

    private int findTargetButtonIndex() {
        List<Button> buttons = getMessage().getButtons();
        for (int i = 0; i < buttons.size(); i++) {
            Button current = buttons.get(i);
            if (getInteraction().getComponentId().equals(current.getId()))
                return i;
        }
        return -1;
    }
}