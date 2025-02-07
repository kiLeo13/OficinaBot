package ofc.bot.domain.sqlite.repository;

import ofc.bot.domain.entity.BlockedWord;
import ofc.bot.domain.tables.BlockedWordsTable;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Repository for {@link BlockedWord} entity.
 */
public class BlockedWordRepository {
    private static final BlockedWordsTable BLOCKED_WORDS = BlockedWordsTable.BLOCKED_WORDS;
    private final DSLContext ctx;

    public BlockedWordRepository(DSLContext ctx) {
        this.ctx = ctx;
    }

    public List<BlockedWord> findAll() {
        return ctx.fetch(BLOCKED_WORDS);
    }
}