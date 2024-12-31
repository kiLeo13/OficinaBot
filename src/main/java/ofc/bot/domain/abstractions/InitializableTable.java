package ofc.bot.domain.abstractions;

import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.TableImpl;

import static org.jooq.impl.DSL.name;

public abstract class InitializableTable<R extends Record> extends TableImpl<R> {

    public InitializableTable(@NotNull String tableName) {
        super(name(tableName));
    }

    protected final <T> TableField<R, T> newField(String name, DataType<T> type) {
        return createField(name(name), type);
    }

    /**
     * Initializes (or attempts to) the current table in the database
     * for the provided context.
     * <p>
     * This method should not suppress, ignore or handle any exceptions.
     *
     * @param ctx The executable database context.
     * @return An executable {@link Query} scoped to the provided context to create the table.
     * @throws DataAccessException if the query fails to be executed.
     */
    abstract public Query getSchema(@NotNull DSLContext ctx) throws DataAccessException;
}