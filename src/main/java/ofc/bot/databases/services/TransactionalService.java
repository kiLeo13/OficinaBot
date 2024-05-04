package ofc.bot.databases.services;

import ofc.bot.databases.DBManager;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionalService implements DatabaseService<Query> {
    private final List<Query> queries = new ArrayList<>();

    @Override
    public void add(Query task) {

        if (task == null)
            throw new IllegalArgumentException("Query cannot be null");

        this.queries.add(task);
    }

    @NotNull
    @Override
    public List<Query> getQueued() {
        return queries.isEmpty()
                ? List.of()
                : Collections.unmodifiableList(queries);
    }

    @Override
    public int commit() throws DataAccessException {

        DSLContext ctx = DBManager.getContext();
        AtomicInteger affected = new AtomicInteger();

        ctx.transaction(cfg -> {
            DSLContext trx = cfg.dsl();

            for (Query query : queries) {
                affected.set(trx.execute(query));
            }
        });

        return affected.get();
    }
}