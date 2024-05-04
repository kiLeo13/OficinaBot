package ofc.bot.databases.entities.records;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import ofc.bot.Main;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.Repository;
import ofc.bot.databases.entities.tables.Groups;
import ofc.bot.util.Bot;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.awt.*;

public class GroupRecord extends Repository<Integer, GroupRecord> {

    public static final Groups GROUPS = Groups.GROUPS;

    public GroupRecord() {
        super(GROUPS);
    }

    public GroupRecord(String name, long roleId, int color, long ownerId, long guildId, boolean privileged) {
        this();
        long timestamp = Bot.unixNow();

        set(GROUPS.NAME, name);
        set(GROUPS.COLOR, color);
        set(GROUPS.OWNER_ID, ownerId);
        set(GROUPS.GUILD_ID, guildId);
        set(GROUPS.ROLE_ID, roleId);
        set(GROUPS.PRIVILEGED, privileged ? 1 : 0);
        set(GROUPS.CREATED_AT, timestamp);
        set(GROUPS.UPDATED_AT, timestamp);
    }

    @Override
    public Field<Integer> getIdField() {
        return GROUPS.ID;
    }

    public long getOwnerId() {
        return get(GROUPS.OWNER_ID);
    }

    public long getRoleId() {
        return get(GROUPS.ROLE_ID);
    }

    public long getTextChannelId() {
        Long id = get(GROUPS.TEXT_CHANNEL_ID);
        return id == null ? 0 : id;
    }

    public TextChannel getTextChannel() {

        JDA api = Main.getApi();
        long textChannelId = getTextChannelId();

        return textChannelId == 0
                ? null
                : api.getTextChannelById(textChannelId);
    }

    public long getVoiceChannelId() {
        Long id = get(GROUPS.VOICE_CHANNEL_ID);
        return id == null ? 0 : id;
    }

    public VoiceChannel getVoiceChannel() {

        JDA api = Main.getApi();
        long voiceChannelId = getVoiceChannelId();

        return voiceChannelId == 0
                ? null
                : api.getVoiceChannelById(voiceChannelId);
    }

    public String getName() {
        return get(GROUPS.NAME);
    }

    public int getColorRaw() {
        Integer color = get(GROUPS.COLOR);
        return color == null ? 0 : color;
    }

    public Color getColor() {
        return new Color(getColorRaw());
    }

    public boolean isPrivileged() {
        Integer privileged = get(GROUPS.PRIVILEGED);
        return privileged != null && privileged == 1;
    }

    public long getTimeCreated() {
        Long created = get(GROUPS.CREATED_AT);
        return created == null ? 0 : created;
    }

    public long getLastUpdated() {
        Long updated = get(GROUPS.UPDATED_AT);
        return updated == null ? 0 : updated;
    }
}