package ofc.bot.databases.services;

import ofc.bot.databases.DBManager;
import org.jetbrains.annotations.NotNull;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

public class BatchInsertService implements DatabaseService<Query> {
    private final List<Query> queries = new ArrayList<>();

    @Override
    public void add(Query query) {

        if (query == null)
            throw new IllegalArgumentException("Record may not be null");

        this.queries.add(query);
    }

    @NotNull
    @Override
    public List<Query> getQueued() {
        return this.queries;
    }

    /**
     * By default, {@link org.jooq.Batch#execute() Batch.execute()} returns an array of {@code int}s,
     * for this method, the returned value will be the the
     * reduced array (sum of all elements).
     *
     * @return The affected rows.
     * @throws DataAccessException If an exception is encountered when committing
     * the batch operation.
     */
    @Override
    public int commit() throws DataAccessException {
        return reduceArray(DBManager.executeBatch(this.queries));
    }

    private int reduceArray(int[] nums) {

        int result = 0;

        for (int n : nums)
            result += n;

        return result;
    }
}