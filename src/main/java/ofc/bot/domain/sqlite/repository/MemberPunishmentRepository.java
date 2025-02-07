package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.domain.tables.MembersPunishmentsTable;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Repository for {@link MemberPunishment} entity.
 */
public class MemberPunishmentRepository {
    private static final MembersPunishmentsTable MEMBERS_PUNISHMENTS = MembersPunishmentsTable.MEMBERS_PUNISHMENTS;
    private final DSLContext ctx;

    public MemberPunishmentRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public MemberPunishment findById(int id) {
        return ctx.selectFrom(MEMBERS_PUNISHMENTS)
                .where(MEMBERS_PUNISHMENTS.ID.eq(id))
                .fetchOne();
    }

    public void upsert(MemberPunishment punishment) {
        punishment.changed(MEMBERS_PUNISHMENTS.CREATED_AT, false);
        ctx.insertInto(MEMBERS_PUNISHMENTS)
                .set(punishment.intoMap())
                .onDuplicateKeyUpdate()
                .set(punishment)
                .execute();
    }

    public List<MemberPunishment> findByUserAndGuildId(long userId, long guildId, int limit) {
        return ctx.selectFrom(MEMBERS_PUNISHMENTS)
                .where(MEMBERS_PUNISHMENTS.USER_ID.eq(userId))
                .and(MEMBERS_PUNISHMENTS.GUILD_ID.eq(guildId))
                .and(MEMBERS_PUNISHMENTS.ACTIVE.eq(true))
                .orderBy(MEMBERS_PUNISHMENTS.CREATED_AT.desc())
                .limit(limit)
                .fetch();
    }

    public List<MemberPunishment> findByUserAndGuildId(
            long userId, long guildId, int limit, int offset
    ) {
        return ctx.selectFrom(MEMBERS_PUNISHMENTS)
                .where(MEMBERS_PUNISHMENTS.USER_ID.eq(userId))
                .and(MEMBERS_PUNISHMENTS.GUILD_ID.eq(guildId))
                .and(MEMBERS_PUNISHMENTS.ACTIVE.eq(true))
                .orderBy(MEMBERS_PUNISHMENTS.CREATED_AT.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    public int countByUserAndGuildId(long userId, long guildId) {
        return ctx.fetchCount(MEMBERS_PUNISHMENTS,
                MEMBERS_PUNISHMENTS.GUILD_ID.eq(guildId)
                        .and(MEMBERS_PUNISHMENTS.USER_ID.eq(userId))
                        .and(MEMBERS_PUNISHMENTS.ACTIVE.eq(true))
        );
    }

    public int countByUserIdAfter(long userId, long period, TimeUnit unit) {
        long now = Bot.unixNow();
        long startPoint = now - unit.toSeconds(period);

        return ctx.fetchCount(MEMBERS_PUNISHMENTS,
                MEMBERS_PUNISHMENTS.USER_ID.eq(userId)
                        .and(MEMBERS_PUNISHMENTS.ACTIVE.eq(true))
                        .and(MEMBERS_PUNISHMENTS.CREATED_AT.ge(startPoint))
        );
    }
}