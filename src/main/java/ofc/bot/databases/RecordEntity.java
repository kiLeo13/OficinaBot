package ofc.bot.databases;

import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableRecordImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract representation of a database record, with basic methods
 * like {@link #save()}, {@link #update()} and {@link #delete()}.
 *
 * @param <K> The primary key data type.
 * @param <R> The record type (the child class of this {@link RecordEntity}).
 */
public abstract class RecordEntity<K, R extends TableRecord<R>> extends TableRecordImpl<R> {
    private static final DSLContext CTX = DBManager.getContext();

    public RecordEntity(Table<R> table) {
        super(table);
    }

    /**
     * @return The primary key {@link Field} of this table.
     */
    @NotNull
    public abstract Field<K> getIdField();

    public final K getId() {
        return getIdField().get(this);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public final <T extends RecordEntity<K, R>> T setId(@NotNull K id) {

        if (id == null)
            throw new IllegalArgumentException("Id value for table " + getTable().getName() + " may not be null");

        this.set(getIdField(), id);
        return (T) this;
    }

    /**
     * Sends all the data stored in-memory of this intance to the database.
     * <p>
     * This is the exact same as calling {@code Repository.getSave(boolean).execute()}.
     */
    public final void save(boolean override) {
        getSave(override).execute();
    }

    /**
     * Sends all the data stored in-memory of this intance to the database.
     * <p>
     * <b>This method WILL OVERRIDE the rows in collision.</b>
     * <p>
     * This is the exact same as calling {@code Repository.getSave(boolean).execute()}.
     */
    public final void save() {
        getSave().execute();
    }

    /**
     * Sends all the data stored in-memory of this intance to the database.
     * <p>
     * <b>This method WILL OVERRIDE the rows in collision.</b>
     * <p>
     * This is the exact same as calling {@code Repository.save()} which is
     * also the same as calling {@code Repository.getSave(true).execute()}.
     */
    public final void merge() {
        save();
    }

    /**
     * Returns the built {@link Query} that will be used
     * for any call at {@link #save(boolean)} on this instance.
     *
     * @return The {@link Query} used in {@link #save()} method.
     */
    public final Query getSave(boolean override) {

        InsertSetStep<R> insertion = CTX.insertInto(this.getTable());
        // This map excludes the "created_at" column to avoid
        // updating its value
        Map<Field<?>, Object> mappedValues = getValues();
        InsertSetMoreStep<R> insert = insertion.set(mappedValues)
                .set(DSL.field("created_at"), Bot.unixNow());

        return override
                ? insert.onDuplicateKeyUpdate().set(mappedValues)
                : insert.onDuplicateKeyIgnore();
    }

    /**
     * Returns the built {@link Query} that will be used
     * for any call at {@link #save()} on this instance.
     * <p>
     * <b>This query WILL OVERRIDE the rows in collision.</b>
     *
     * @return The {@link Query} used in {@link #save()} method.
     */
    public final Query getSave() {
        return getSave(true);
    }

    public void update() {

        Map<Field<?>, Object> values = getValues();
        Field<K> keyField = getIdField();
        K keyValue = keyField.get(this);

        CTX.update(this.getTable())
                .set(values)
                .where(keyField.eq(keyValue))
                .execute();
    }

    public void delete() {

        Field<K> keyField = getIdField();
        K keyValue = getId();

        CTX.deleteFrom(this.getTable())
                .where(keyField.eq(keyValue))
                .execute();
    }

    private Map<Field<?>, Object> getValues() {

        Field<?>[] fields = fields();
        Map<Field<?>, Object> mappedValues = new HashMap<>(fields.length);

        for (Field<?> f : fields) {

            if (!f.getName().equals("created_at"))
                mappedValues.put(f, f.get(this));
        }

        return mappedValues;
    }
}