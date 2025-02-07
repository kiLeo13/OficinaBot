package ofc.bot.events;

import ofc.bot.events.impl.BankTransactionEvent;
import org.jetbrains.annotations.NotNull;

public abstract class EventListener {
    public void onBankTransaction(@NotNull BankTransactionEvent e) {}
}