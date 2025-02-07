package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.AutomodAction;
import ofc.bot.domain.tables.AutomodActionsTable;
import org.jooq.DSLContext;

/**
 * Repository for {@link AutomodAction} entity.
 */
public class AutomodActionRepository {
    private static final AutomodActionsTable AUTOMOD_ACTIONS = AutomodActionsTable.AUTOMOD_ACTIONS;
    private final DSLContext ctx;

    public AutomodActionRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public AutomodAction findLastByThreshold(int threshold) {
        return ctx.selectFrom(AUTOMOD_ACTIONS)
                .where(AUTOMOD_ACTIONS.THRESHOLD.le(threshold))
                .orderBy(AUTOMOD_ACTIONS.THRESHOLD.desc())
                .limit(1)
                .fetchOne();
    }
}
