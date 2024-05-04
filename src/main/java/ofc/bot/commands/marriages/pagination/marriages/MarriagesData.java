package ofc.bot.commands.marriages.pagination.marriages;

import ofc.bot.databases.entities.records.MarriageRecord;

import java.util.List;

public record MarriagesData(
        List<MarriageRecord> marriages,
        long userId,
        int page,
        int maxPages,
        int marriageCount
) {
    public boolean isEmpty() {
        return this.marriages.isEmpty();
    }
}