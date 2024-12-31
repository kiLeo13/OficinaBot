package ofc.bot.handlers.interactions.buttons;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.TemporaryStorage;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContext;

public class ButtonManager {
    private static final ButtonManager instance = new ButtonManager();
    private final TemporaryStorage<ButtonContext> buttons = new TemporaryStorage<>();

    private ButtonManager() {}

    public static ButtonManager getManager() {
        return instance;
    }

    public void save(ButtonContext... contexts) {
        for (ButtonContext ctx : contexts) {
            String id = ctx.getButton().getId();
            Checks.notNull(id, "Button ID");

            this.buttons.put(id, ctx, ctx.getValidityMillis());
        }
    }

    public ButtonContext get(String buttonId) {
        return this.buttons.find(buttonId);
    }

    public void remove(String buttonId) {
        this.buttons.remove(buttonId);
    }
}