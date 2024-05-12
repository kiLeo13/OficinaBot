package ofc.bot.databases.entities.records;

import ofc.bot.databases.RecordEntity;
import ofc.bot.databases.entities.tables.Marriages;
import ofc.bot.databases.services.TransactionalService;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Query;

import static org.jooq.impl.DSL.deleteFrom;

public class MarriageRecord extends RecordEntity<Integer, MarriageRecord> {

    // This value keeps track whether the divorce() method
    // was called on this instance
    private boolean divorceQueued = false;

    public static final Marriages MARRIAGES = Marriages.MARRIAGES;

    public MarriageRecord() {
        super(MARRIAGES);
    }

    @NotNull
    @Override
    public Field<Integer> getIdField() {
        return MARRIAGES.ID;
    }

    public String getSelectedUserName() {
        return get(MARRIAGES.USER_NAME);
    }

    public String getSelectedUserGlobalName() {
        return get(MARRIAGES.USER_GLOBAL_NAME);
    }

    public String getSelectedUserEffectiveName() {
        String name = getSelectedUserGlobalName();

        return name == null
                ? getSelectedUserName()
                : name;
    }

    public long getRequesterId() {
        Long requester = get(MARRIAGES.REQUESTER_ID);
        return requester == null
                ? 0
                : requester;
    }

    public long getTargetId() {
        Long target = get(MARRIAGES.TARGET_ID);
        return target == null
                ? 0
                : target;
    }

    public long getCreated() {
        Long created = get(MARRIAGES.CREATED_AT);
        return created == null
                ? 0
                : created;
    }

    public long getLastUpdated() {
        Long updated = get(MARRIAGES.UPDATED_AT);
        return updated == null
                ? 0
                : updated;
    }

    /**
     * Checks whether the current instance is "queued" to be divorced when calling
     * {@link TransactionalService#commit()}.
     * <p>
     * This method does not rely on any database column; instead, it tracks whether
     * the {@link #divorce(TransactionalService)} method has been called on this instance.
     * <p>
     * While this method provides information about the current divorce queue state, it is
     * not recommended to be relied upon for critical decisions, but can be used for filtering.
     *
     * @return {@code true} if this instance represents a queued divorce relationship, {@code false} otherwise.
     */
    public boolean isDivorceQueued() {
        return this.divorceQueued;
    }

    public void divorce(TransactionalService transaction) {

        Query query = deleteFrom(MARRIAGES)
                .where(MARRIAGES.ID.eq(this.getId()));

        transaction.add(query);

        this.divorceQueued = true;
    }
}