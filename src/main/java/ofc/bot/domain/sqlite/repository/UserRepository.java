package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.AppUser;
import ofc.bot.domain.tables.UsersTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link ofc.bot.domain.entity.AppUser AppUser} entity.
 */
public class UserRepository {
    private final UsersTable USERS = UsersTable.USERS;
    private final DSLContext ctx;

    public UserRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Merge follows the same idea as an upsert and a
     * {@code INSERT INTO ... ON DUPLICATE KEY UPDATE} call.
     * <p>
     * In general, this method attempts to insert a new user,
     * if it collides, the row is updated.
     * <p>
     * The update respect the {@code created_at} column.
     *
     * @param user the user to be upsert.
     */
    public void upsert(AppUser user) {
        user.changed(USERS.CREATED_AT, false);
        ctx.insertInto(USERS)
                .set(user.intoMap())
                .onDuplicateKeyUpdate()
                .set(user)
                .execute();
    }
}