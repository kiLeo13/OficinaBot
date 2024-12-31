package ofc.bot.events.eventbus;

import ofc.bot.events.EventListener;

public abstract class GenericApplicationEvent {
    protected abstract void invoke(EventListener listener);
}