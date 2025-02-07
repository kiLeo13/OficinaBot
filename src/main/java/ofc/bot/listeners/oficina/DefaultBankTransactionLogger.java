package ofc.bot.listeners.oficina;

import ofc.bot.domain.sqlite.repository.BankTransactionRepository;
import ofc.bot.events.EventListener;
import ofc.bot.events.impl.BankTransactionEvent;
import ofc.bot.util.content.annotations.listeners.ApplicationEventHandler;
import org.jetbrains.annotations.NotNull;

@ApplicationEventHandler
public final class DefaultBankTransactionLogger extends EventListener {
    private final BankTransactionRepository bankTrRepo;

    public DefaultBankTransactionLogger(BankTransactionRepository bankTrRepo) {
        this.bankTrRepo = bankTrRepo;
    }

    @Override
    public void onBankTransaction(@NotNull BankTransactionEvent e) {
        bankTrRepo.save(e.getTransaction());
    }
}