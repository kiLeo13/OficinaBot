package ofc.bot.events.eventbus;

import ofc.bot.events.EventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: Add documentation
public final class EventBus {
    private static final EventBus instance = new EventBus();
    private final List<EventListener> listeners;
    private final ExecutorService executor = Executors.newWorkStealingPool();

    private EventBus() {
        this.listeners = new ArrayList<>();
    }

    public static EventBus getEventBus() {
        return instance;
    }

    public static void dispatchEvent(GenericApplicationEvent event) {
        getEventBus().dispatch(event);
    }

    public void register(EventListener listener) {
        this.listeners.add(listener);
    }

    public void dispatch(GenericApplicationEvent event) {
        for (EventListener ls : this.listeners) {
            executor.execute(() -> event.invoke(ls));
        }
    }
}