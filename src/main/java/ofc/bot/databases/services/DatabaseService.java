package ofc.bot.databases.services;

import org.jetbrains.annotations.NotNull;
import org.jooq.Query;
import org.jooq.exception.DataAccessException;

import java.util.List;

/**
 * Represents a service for managing database operations in a transactional or batch context.
 * This interface provides methods to add database tasks and commit them collectively, abstracting
 * away the complexities of transaction management and batch operations.
 *
 * @param <T> The type of data being dealt with, usually a jOOQ {@link Query} object.
 */
public interface DatabaseService<T> {

    /**
     * Adds a database task to the service.
     *
     * @param e The element of type {@link T} to be added/queued.
     */
    void add(T e);

    /**
     * Returns a {@link List} of type {@link T}
     * containing all the added elements to the service.
     *
     * @return The previously added elements.
     */
    @NotNull
    List<T> getQueued();

    /**
     * Commits all the added database tasks collectively.
     *
     * @return the number of affected rows.
     * @throws DataAccessException If an error occurs while committing the database tasks.
     */
    int commit() throws DataAccessException;
}