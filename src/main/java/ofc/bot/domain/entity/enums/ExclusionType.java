package ofc.bot.domain.entity.enums;

import ofc.bot.domain.tables.UsersExclusionsTable;

/**
 * This enum references the types of exclusion for the
 * table {@link UsersExclusionsTable UsersExclusions}.
 */
public enum ExclusionType {
    MARRIAGE_FEE,
    BIRTHDAY_AGE_DISPLAY,
    APPEAR_ON_STAFF_LIST;

    public static ExclusionType findByName(String name) {
        for (ExclusionType et : values()) {
            if (et.name().equals(name)) return et;
        }

        return null;
    }
}