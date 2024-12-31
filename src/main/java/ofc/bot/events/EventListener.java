package ofc.bot.events;

import ofc.bot.events.entities.GenericTransactionEvent;
import ofc.bot.events.entities.BankTransactionEvent;
import org.jetbrains.annotations.NotNull;

public abstract class EventListener {
    public void onGenericTransaction(@NotNull GenericTransactionEvent e) {}

    public void onBankTransaction(@NotNull BankTransactionEvent e) {}
}