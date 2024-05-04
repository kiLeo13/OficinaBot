package ofc.bot.util.exclusions;

/**
 * This enum references the types of exclusion for the
 * table {@link ofc.bot.databases.entities.tables.UsersExclusions UsersExclusions}.
 */
public enum ExclusionType {
    MARRIAGE_FEE,
    BIRTHDAY_AGE_DISPLAY,
    APPEAR_ON_STAFF_LIST;

    public static ExclusionType byName(String name) {

        for (ExclusionType et : values()) {

            if (et.name().equals(name)) {
                return et;
            }
        }

        throw new IllegalStateException("Could not find Exclusion Type for name " + name);
    }
}