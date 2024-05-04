package ofc.bot.databases.entities;

import ofc.bot.commands.administration.name_history.NameChangeContext;

public interface INameChangeLog {

    String getOldValue();

    String getNewValue();

    NameChangeContext getContext();

    long getUserId();

    default long getModeratorId() {
        return getUserId();
    }

    long getTimestamp();
}