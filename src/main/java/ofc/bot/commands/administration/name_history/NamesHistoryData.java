package ofc.bot.commands.administration.name_history;

import ofc.bot.databases.entities.INameChangeLog;
import ofc.bot.util.Bot;

import java.util.List;

public record NamesHistoryData(
        List<? extends INameChangeLog> names,
        int rowsCount,
        int offset,
        int page
) {
    public boolean isEmpty() {
        return this.names.isEmpty();
    }

    public int maxPages() {
        return Bot.calculateMaxPages(rowsCount, 10);
    }
}