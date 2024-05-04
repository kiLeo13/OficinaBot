package ofc.bot.commands.marriages.pagination.proposals;

import ofc.bot.databases.entities.records.MarriageRequestRecord;

import java.util.List;

public record ProposalsData(
        List<MarriageRequestRecord> requests,
        String type,

        // This field can refer to either who sent and who received the proposal,
        // it depends on which option the user provided at the "type" argument
        long userId,
        int page,
        int maxPages,
        int requestCount
) {
    public boolean isEmpty() {
        return this.requests.isEmpty();
    }
}