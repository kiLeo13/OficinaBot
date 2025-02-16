package ofc.bot.handlers.economy.oficina;

import ofc.bot.domain.entity.UserEconomy;
import ofc.bot.domain.sqlite.repository.Repositories;
import ofc.bot.domain.sqlite.repository.UserEconomyRepository;
import ofc.bot.handlers.economy.BankAccount;
import ofc.bot.handlers.economy.CurrencyType;

public class OficinaAccount implements BankAccount {
    private final long userId;
    private long balance;

    protected OficinaAccount(long userId, long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    protected static OficinaAccount from(UserEconomy eco) {
        return new OficinaAccount(eco.getUserId(), eco.getBalance());
    }

    @Override
    public long getUserId() {
        return this.userId;
    }

    @Override
    public long getGuildId() {
        return 0L;
    }

    @Override
    public long getCash() {
        return 0L;
    }

    @Override
    public long getBank() {
        return this.balance;
    }

    @Override
    public long getTotal() {
        return this.balance;
    }

    @Override
    public int getRank() {
        UserEconomyRepository ecoRepo = Repositories.getUserEconomyRepository();
        return ecoRepo.findRankByUserId(userId);
    }

    @Override
    public CurrencyType getType() {
        return CurrencyType.OFICINA;
    }

    @Override
    public BankAccount setCash(long cash) {
        return this;
    }

    @Override
    public BankAccount modifyCash(long cash) {
        return this;
    }

    @Override
    public BankAccount setBank(long bank) {
        this.balance = bank;
        return this;
    }

    @Override
    public BankAccount modifyBank(long bank) {
        this.balance += bank;
        return this;
    }

    @Override
    public boolean isDummy() {
        return false;
    }
}