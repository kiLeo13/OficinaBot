package ofc.bot.util.content;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.util.Arrays;
import java.util.List;

public enum Levels {
    DEMON(        1,   1185469524309450762L),
    SPIDER(       10,  1185468711658864750L),
    SWAMP(        20,  1185468709050011668L),
    SLASHER(      30,  1185468707116417134L),
    SABITO(       40,  1185468705279328306L),
    YAHABA(       50,  1185468702653694053L),
    SUSAMARU(     60,  1185468699939979335L),
    KYOGAI(       70,  1185468697108807700L),
    YUSHIRO(      80,  1185468694135062598L),
    TAMAYO(       90,  1185468692197285958L),
    NEZUKO_KAMADO(100, 1185468689911386214L);

    private final int level;
    private final long roleId;

    Levels(int level, long roleId) {
        this.level = level;
        this.roleId = roleId;
    }

    public int level() {
        return this.level;
    }

    public long roleId() {
        return this.roleId;
    }

    public Role role(Guild guild) {
        return guild.getRoleById(this.roleId);
    }

    /**
     * Returns a list of {@link Levels} instances whose {@code level}
     * is greater than or equal to the specified {@code anchor} value.
     * <p>
     * This method filters the {@link Levels} instances based on their {@code level} attribute,
     * retaining only those with a {@code level} that meets or exceeds the specified {@code anchor}.
     * </p>
     * <p>
     * Equivalent to: {@code Arrays.stream(Levels.values()).filter(l -> l.level >= anchor)}.
     * </p>
     *
     * @param anchor the value used to filter the {@link Levels} instances.
     * @return a {@link List} of {@link Levels} instances that have a {@code level} greater than or equal to {@code anchor}.
     */
    public static List<Levels> fromAbove(int anchor) {
        return Arrays.stream(values())
                .filter(l -> l.level >= anchor)
                .toList();
    }

    public static List<Levels> fromAbove(Levels level) {
        return fromAbove(level.level);
    }
}