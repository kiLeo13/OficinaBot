package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.GroupBot;
import ofc.bot.domain.tables.GroupBotsTable;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link ofc.bot.domain.entity.GroupBot GroupBot} entity.
 */
public class GroupBotRepository {
    private static final GroupBotsTable GROUP_BOTS = GroupBotsTable.GROUP_BOTS;
    private final DSLContext ctx;

    public GroupBotRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<GroupBot> findByName(String name, int limit) {
        return ctx.selectFrom(GROUP_BOTS)
                .where(GROUP_BOTS.BOT_NAME.like('%' + name + '%'))
                .limit(limit)
                .fetch();
    }

    public GroupBot findById(int id) {
        return ctx.selectFrom(GROUP_BOTS)
                .where(GROUP_BOTS.ID.eq(id))
                .fetchOne();
    }
}