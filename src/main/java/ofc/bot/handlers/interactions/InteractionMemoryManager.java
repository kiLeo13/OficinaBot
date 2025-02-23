package ofc.bot.handlers.interactions;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.TemporaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class InteractionMemoryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionMemoryManager.class);
    private static final InteractionMemoryManager instance = new InteractionMemoryManager();
    private final TemporaryStorage<EntityContext<?, ?>> entities = new TemporaryStorage<>();
    private final Map<String, InteractionListener<?>> listeners = new HashMap<>();

    private InteractionMemoryManager() {}

    public static InteractionMemoryManager getManager() {
        return instance;
    }

    public void save(EntityContext<?, ?>... contexts) {
        for (EntityContext<?, ?> ctx : contexts) {
            ctx.checkFields();

            String id = ctx.getId();
            Checks.notNull(id, "entity id");

            this.entities.put(id, ctx, ctx.getValidityMillis());
        }
    }

    public void registerListeners(InteractionListener<?>... listeners) {
        for (InteractionListener<?> ls : listeners) {
            String scope = ls.getScope();

            if (this.listeners.containsKey(scope))
                LOGGER.warn("Overriding already existing scope {}", scope);

            this.listeners.put(scope, ls);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends InteractionListener<?>> T getListener(String scope) {
        return (T) this.listeners.get(scope);
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityContext<?, ?>> T get(String id) {
        return (T) this.entities.find(id);
    }

    public void remove(String id) {
        this.entities.remove(id);
    }
}