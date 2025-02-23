package ofc.bot.handlers.interactions;

import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InteractionSubmitContext <C extends EntityContext<?, ?>, I extends Interaction & IReplyCallback>
        extends AcknowledgeableAction<I> {
    private final C context;

    public InteractionSubmitContext(C context, I interaction) {
        super(interaction);
        this.context = context;
    }

    public final C getContext() {
        return this.context;
    }

    @NotNull
    public final <T> T get(String key) {
        return this.context.get(key);
    }

    @Nullable
    public final <T> T find(String key) {
        return this.context.find(key);
    }

    @Contract("_, null -> null")
    public final <T> T getOrDefault(String key, T defaultValue) {
        return this.context.getOrDefault(key, defaultValue);
    }
}