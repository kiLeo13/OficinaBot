package ofc.bot.handlers.buttons;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.util.content.annotations.listeners.EventHandler;

import java.util.HashMap;
import java.util.Map;

@EventHandler
public class ButtonClickHandler extends ListenerAdapter {
    private static final Map<String, BotButtonListener> buttons = new HashMap<>();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        Button button = event.getButton();
        String buttonId = button.getId();
        Member member = event.getMember();
        String taskType = ButtonManager.getType(buttonId);

        if (!event.isFromGuild())
            return;

        BotButtonListener listener = buttons.get(taskType);
        ButtonData data = ButtonManager.get(buttonId);

        if (listener == null || data == null)
            return;

        event.deferEdit().queue();

        if (data.isPermitted(member)) {

            listener.onClick(event.getInteraction(), data);
            ButtonManager.remove(buttonId);
        }
    }

    public static void registerButton(String taskType, BotButtonListener listener) {
        buttons.put(taskType, listener);
    }
}