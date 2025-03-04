package ofc.bot.domain.sqlite.repository;

import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.abstractions.InitializableTable;
import ofc.bot.domain.entity.OficinaRecord;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;

import java.util.Arrays;
import java.util.List;

/**
 * The abstract repository for database operations.
 *
 * @param <T> the type of the entity.
 */
public abstract class Repository<T extends OficinaRecord<T>> {
    public static final int MAX_BATCH_SIZE = 1000;
    protected final DSLContext ctx;

    public Repository(@NotNull DSLContext ctx) {
        Checks.notNull(ctx, "DSLContext");
        this.ctx = ctx;
    }

    @NotNull
    public abstract InitializableTable<T> getTable();

    @NotNull
    public final DSLContext getContext() {
        return this.ctx;
    }

    /**
     * Attempts to save this record, if any constraints collide, nothing happens.
     *
     * @param rec the record to be saved.
     * @see #upsert(OficinaRecord)
     */
    public final void save(@NotNull T rec) {
        ctx.insertInto(rec.getTable())
                .set(rec.intoMap())
                .onConflictDoNothing()
                .execute();
    }

    /**
     * Attempts to save this record, if any constraints collide,
     * the row is updated with the values present in the {@code rec} parameter.
     *
     * @param rec the record to be upsert.
     * @see #save(OficinaRecord)
     */
    public final void upsert(@NotNull T rec) {
        ctx.insertInto(rec.getTable())
                .set(rec.intoMap())
                .onDuplicateKeyUpdate()
                .set(rec)
                .execute();
    }

    public final List<T> findAll() {
        return ctx.fetch(getTable());
    }

    public final int bulkSave(@NotNull List<T> recs) {
        Checks.notNull(recs, "Records");
        if (recs.isEmpty()) return 0; // Okay :shrug:

        return batch(recs);
    }

    private int batch(List<T> recs) {
        return Arrays.stream(ctx.batchInsert(recs).execute()).sum();
    }
}