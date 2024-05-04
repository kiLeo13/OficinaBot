package ofc.bot.databases.entities.records;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.CustomUserinfo;
import ofc.bot.util.Bot;
import ofc.bot.util.content.MemberEmoji;
import org.jooq.Field;

import java.awt.*;
import java.util.List;

public class CustomUserinfoRecord extends Repository<Long, CustomUserinfoRecord> {
    private static final String DEFAULT_TITLE_FORMAT = "👥 %s";

    public static final CustomUserinfo CUSTOM_USERINFO = CustomUserinfo.CUSTOM_USERINFO;

    public CustomUserinfoRecord() {
        super(CUSTOM_USERINFO);
    }

    public CustomUserinfoRecord(long userId) {
        this(userId, 0, null, null);
    }

    public CustomUserinfoRecord(long userId, int color, String description, String footer) {
        this();
        long timestamp = Bot.unixNow();

        set(CUSTOM_USERINFO.USER_ID, userId);
        set(CUSTOM_USERINFO.COLOR, color);
        set(CUSTOM_USERINFO.DESCRIPTION, description);
        set(CUSTOM_USERINFO.FOOTER, footer);
        set(CUSTOM_USERINFO.CREATED_AT, timestamp);
        set(CUSTOM_USERINFO.UPDATED_AT, timestamp);
    }

    @Override
    public Field<Long> getIdField() {
        return CUSTOM_USERINFO.USER_ID;
    }

    public long getUserId() {
        return getId();
    }

    public int getColor() {
        Integer color = get(CUSTOM_USERINFO.COLOR);
        return color == null
                ? 0
                : color;
    }

    public String getDescription(Member member) {

        String desc = get(CUSTOM_USERINFO.DESCRIPTION);
        User user = member.getUser();

        if (desc == null)
            return null;

        return desc
                .replace("{member.name}", member.getEffectiveName())
                .replace("{user.name}", user.getEffectiveName());
    }

    public String getFooter() {
        return get(CUSTOM_USERINFO.FOOTER);
    }

    public long getCreated() {
        Long created = get(CUSTOM_USERINFO.CREATED_AT);
        return created == null
                ? 0
                : created;
    }

    public long getLastUpdated() {
        Long updated = get(CUSTOM_USERINFO.UPDATED_AT);
        return updated == null
                ? 0
                : updated;
    }

    public String getEffectiveTitle(User user) {
        
        String emoji = MemberEmoji.emojiById(user.getId());
        String name = user.getEffectiveName();

        return emoji == null
                ? String.format(DEFAULT_TITLE_FORMAT, name)
                : emoji + " " + name;
    }

    public String getEffectiveDescription(Member member) {

        String description = getDescription(member);

        return description == null
                ? String.format(CustomUserinfo.DEFAULT_DESCRIPTION_FORMAT, member.getEffectiveName())
                : description;
    }

    public Color getEffectiveColor(Member member) {

        int color = getColor();
        List<Role> roles = member.getRoles();

        if (color != 0)
            return new Color(color);

        return roles.isEmpty()
                ? Color.GRAY
                : roles.get(0).getColor();
    }

    public String getEffectiveFooter(Guild guild) {

        String footer = getFooter();

        return footer == null
                ? guild.getName()
                : footer;
    }
}