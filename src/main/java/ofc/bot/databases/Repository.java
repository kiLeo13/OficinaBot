package ofc.bot.databases;

import ofc.bot.util.Bot;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableRecordImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract representation of a database repository, with basic methods
 * like {@link #save()}, {@link #update()} and {@link #delete()}.
 *
 * @param <K> The primary key data type.
 * @param <R> The record type (the inheritor of this {@link Repository}).
 */
public abstract class Repository<K, R extends TableRecord<R>> extends TableRecordImpl<R> {

    public Repository(Table<R> table) {
        super(table);
    }

    /**
     * @return The primary key {@link Field} of this table.
     */
    public abstract Field<K> getIdField();

    public final K getId() {
        return getIdField().get(this);
    }

    /**
     * Sends all the data stored in-memory of this intance to the database.
     * <p>
     * This is the exact same as calling {@link Query#execute() Repository.getSave().execute()}.
     */
    public final void save() {
        this.getSave().execute();
    }

    /**
     * Returns the built {@link Query} that will be used
     * for any call at {@link #save()} on this instance.
     *
     * @return The {@link Query} used in {@link #save()} method.
     */
    public final Query getSave() {

        DSLContext ctx = DBManager.getContext();
        InsertSetStep<R> insertion = ctx.insertInto(this.getTable());
        // This map excludes the "created_at" column to avoid
        // updating its value
        Map<Field<?>, Object> mappedValues = getValues();

        return insertion.set(mappedValues)
                .set(DSL.field("created_at"), Bot.unixNow())
                .onDuplicateKeyUpdate()
                .set(mappedValues);
    }

    public void update() {

        Field<K> keyField = getCheckedIdField();
        DSLContext ctx = DBManager.getContext();
        Map<Field<?>, Object> values = getValues();
        K keyValue = keyField.get(this);

        ctx.update(this.getTable())
                .set(values)
                .where(keyField.eq(keyValue))
                .execute();
    }

    public void delete() {

        Field<K> keyField = getCheckedIdField();
        DSLContext ctx = DBManager.getContext();
        K keyValue = keyField.get(this);

        ctx.deleteFrom(this.getTable())
                .where(keyField.eq(keyValue))
                .execute();
    }

    // This method simply ensures that the returned primary key
    // field will never be null. In order to avoid repeated code
    private Field<K> getCheckedIdField() {

        Field<K> field = getIdField();

        if (field == null)
            throw new UnsupportedOperationException("No primary key fields found for table \"" + getTable().getName() + "\"");

        return field;
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