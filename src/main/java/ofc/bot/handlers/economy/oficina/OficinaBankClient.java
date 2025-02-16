package ofc.bot.handlers.economy.oficina;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.handlers.economy.*;

public class OficinaBankClient implements PaymentManager {
    private final UserEconomyRepository ecoRepo;

    public OficinaBankClient(UserEconomyRepository ecoRepo) {
        this.ecoRepo = ecoRepo;
    }

    @Override
    public BankAccount get(long userId, long guildId) {
        return get(userId);
    }

    @Override
    public BankAccount set(long userId, long guildId, long cash, long bank, String reason) {
        return set(userId, cash, bank, reason);
    }

    @Override
    public BankAccount get(long userId) {
        UserEconomy eco = ecoRepo.findByUserId(userId, UserEconomy.fromUserId(userId));
        return OficinaAccount.from(eco);
    }

    @Override
    public BankAccount set(long userId, long cash, long bank, String reason) {
        UserEconomy eco = ecoRepo.findByUserId(userId, UserEconomy.fromUserId(userId))
                .setBalance(bank)
                .tickUpdate();

        ecoRepo.upsert(eco);
        return OficinaAccount.from(ecoRepo.findByUserId(userId));
    }

    @Override
    public BankAccount update(long userId, long guildId, long cash, long bank, String reason) {
        return update(userId, cash, bank, reason);
    }

    @Override
    public BankAccount update(long userId, long cash, long bank, String reason) {
        UserEconomy eco = ecoRepo.findByUserId(userId, UserEconomy.fromUserId(userId))
                .modifyBalance(bank)
                .tickUpdate();

        ecoRepo.upsert(eco);
        return OficinaAccount.from(ecoRepo.findByUserId(userId));
    }

    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.OFICINA;
    }

    @Override
    public BankAction charge(long userId, long guildId, long cash, long bank, String reason) {
        Checks.notNegative(bank, "Bank");

        if (bank == 0) return BankAction.STATIC_SUCCESS_NO_CHANGE;

        BankAccount acc = get(userId);

        if (!hasEnough(acc, bank)) return BankAction.STATIC_FAILURE_NO_CHANGE;

        BankAccount updatedAcc = update(userId, 0, -bank, reason);
        Runnable rollback = () -> update(userId, 0, bank, null);

        if (isInDebt(updatedAcc)) {
            rollback.run();
            return BankAction.STATIC_FAILURE_NO_CHANGE;
        }

        return new BankAction(true, true, rollback);
    }

    private boolean isInDebt(BankAccount acc) {
        return acc == null || acc.getBank() < 0;
    }

    private boolean hasEnough(BankAccount acc, long amount) {
        return acc != null && acc.getBank() >= amount;
    }
}
