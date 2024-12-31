package ofc.bot.handlers.interactions.buttons;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonClickContext;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@DiscordEventHandler
public class ButtonInteractionGateway extends ListenerAdapter {
    private static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();
    // Mapping <Scope, Listener>
    // Though it may map to a list of AppButtonListener's in the future.
    private static final Map<String, BotButtonListener> buttons = new HashMap<>();
    private final ButtonManager mngr = ButtonManager.getManager();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent e) {
        Button button = e.getButton();
        String buttonId = button.getId();
        Member member = e.getMember();
        ButtonContext buttonCtx = mngr.get(buttonId);
        long userId = e.getUser().getIdLong();

        if (!e.isFromGuild() || member == null) return;

        if (buttonCtx == null) {
            e.reply("Você clicou em um botão que já expirou! Tente executar este comando novamente.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String scope = buttonCtx.getScope();
        BotButtonListener listener = buttons.get(scope);

        if (listener == null) return;

        if (buttonCtx.isAuthorOnly() && !buttonCtx.isAuthor(userId)) {
            e.replyFormat("Apenas <@%s> pode clicar neste botão.", buttonCtx.getAuthorId())
                    .setEphemeral(true).queue();
            return;
        }

        if (!buttonCtx.isPermitted(member)) {
            e.reply("Permissão negada.").setEphemeral(true).queue();
            return;
        }

        ButtonClickContext clickContext = new ButtonClickContext(buttonCtx, e);
        handleAutoResponse(listener.getAutoResponseType(), clickContext);

        mngr.remove(buttonId);

        EXECUTOR.execute(() -> {
            InteractionResult state = listener.onClick(clickContext);

            if (state.getContent() != null)
                clickContext.reply(state);
        });
    }

    public static void registerButtons(BotButtonListener... listeners) {
        for (BotButtonListener ls : listeners) {
            buttons.put(ls.getScope(), ls);
        }
    }

    private void handleAutoResponse(AutoResponseType type, ButtonClickContext ctx) {
        switch (type) {
            case THINKING, THINKING_EPHEMERAL -> ctx.ack(type.isEphemeral());
            case DEFER_EDIT -> ctx.ackEdit();
        }
    }
}