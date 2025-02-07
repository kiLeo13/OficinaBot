package ofc.bot.domain.entity.enums;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public enum WarnAction {
    TIMEOUT("advertido", true,  Member::timeoutFor),
    KICK(   "expulso",   false, (m, d) -> m.kick()),
    BAN(    "banido",    false, (m, d) -> m.ban(0, TimeUnit.DAYS));

    private final String display;
    private final boolean hasDeadline;
    private final BiFunction<Member, Duration, AuditableRestAction<?>> action;

    WarnAction(String display, boolean hasDeadline, BiFunction<Member, Duration, AuditableRestAction<?>> action) {
        this.display = display;
        this.hasDeadline = hasDeadline;
        this.action = action;
    }

    public String getDisplay() {
        return this.display;
    }

    /**
     * Checks whether this {@link WarnAction} supports the {@code duration}
     * field or not.
     *
     * @return {@code true} if the punishment type supports a duration, {@code false} otherwise.
     */
    public boolean hasDeadline() {
        return this.hasDeadline;
    }

    public AuditableRestAction<?> apply(Member member, Duration duration) {
        return action.apply(member, duration);
    }
}