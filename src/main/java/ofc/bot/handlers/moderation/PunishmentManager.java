package ofc.bot.handlers.moderation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.domain.entity.AutomodAction;
import ofc.bot.domain.entity.MemberPunishment;
import ofc.bot.domain.entity.enums.WarnAction;
import ofc.bot.domain.sqlite.repository.AutomodActionRepository;
import ofc.bot.domain.sqlite.repository.MemberPunishmentRepository;
import ofc.bot.handlers.exceptions.PunishmentCreationException;
import ofc.bot.util.Bot;
import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;

import java.util.concurrent.TimeUnit;

public final class PunishmentManager {
    private final MemberPunishmentRepository pnshRepo;
    private final AutomodActionRepository modActRepo;

    public PunishmentManager(MemberPunishmentRepository pnshRepo, AutomodActionRepository modActRepo) {
        this.pnshRepo = pnshRepo;
        this.modActRepo = modActRepo;
    }

    public MessageEmbed createPunishment(@NotNull PunishmentData data) {
        Checks.notNull(data, "WarnData");
        Reason reason = data.reason();
        Member target = data.target();
        Member author = data.author();
        String fmtReason = reason.toString();
        MemberPunishment punishment = MemberPunishment.fromMember(target, author.getIdLong(), fmtReason);

        try {
            pnshRepo.upsert(punishment);
            int warnCount = pnshRepo.countByUserIdAfter(target.getIdLong(), 30, TimeUnit.DAYS);
            AutomodAction action = modActRepo.findLastByThreshold(warnCount);

            if (action == null)
                throw new IllegalStateException("No Automod actions defined");

            action.getAction()
                    .apply(target, action.getDuration())
                    .reason(fmtReason)
                    .queue();

            return embedPunishment(target, action.getAction(), fmtReason);
        } catch (DataAccessException e) {
            throw new PunishmentCreationException(
                    "Could not create punisment for member " + punishment.getUserId(), e);
        }
    }

    private MessageEmbed embedPunishment(Member target, WarnAction action, String reason) {
        EmbedBuilder builder = new EmbedBuilder();
        User user = target.getUser();
        String header = String.format("%s foi %s", user.getName(), action.getDisplay());

        return builder
                .setAuthor(header, null, user.getEffectiveAvatarUrl())
                .setColor(Bot.Colors.DEFAULT)
                .setDescription("**Motivo:** " + reason)
                .build();
    }
}