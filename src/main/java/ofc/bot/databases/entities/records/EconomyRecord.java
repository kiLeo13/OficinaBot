package ofc.bot.databases.entities.records;

import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.Economy;
import org.jooq.Field;

public class EconomyRecord extends Repository<Long, EconomyRecord> {

    public static final Economy ECONOMY = Economy.ECONOMY;

    public EconomyRecord() {
        super(ECONOMY);
    }

    @Override
    public Field<Long> getIdField() {
        return ECONOMY.USER_ID;
    }

    public long getUserId() {
        return getId();
    }

    public String getUserName() {
        return get(ECONOMY.USER_NAME);
    }

    public long getBalance() {
        Long balance = get(ECONOMY.BALANCE);
        return balance == null
                ? 0
                : balance;
    }

    public long getCreated() {
        Long created = get(ECONOMY.CREATED_AT);
        return created == null
                ? 0
                : created;
    }

    public long getLastUpdated() {
        Long updated = get(ECONOMY.UPDATED_AT);
        return updated == null
                ? 0
                : updated;
    }
}
