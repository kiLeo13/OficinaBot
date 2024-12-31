package ofc.bot.events.entities;

import ofc.bot.events.EventListener;
import ofc.bot.events.eventbus.GenericApplicationEvent;

public class GenericTransactionEvent extends GenericApplicationEvent {

    @Override
    protected void invoke(EventListener listener) {
        listener.onGenericTransaction(this);
    }
}
