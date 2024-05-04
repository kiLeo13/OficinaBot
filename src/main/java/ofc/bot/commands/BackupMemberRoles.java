package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ofc.bot.databases.DBManager;
import ofc.bot.databases.entities.records.FormerMemberRoleRecord;
import ofc.bot.content.annotations.commands.DiscordCommand;
import ofc.bot.content.annotations.commands.CommandPermission;
import ofc.bot.content.annotations.commands.Option;
import ofc.bot.handlers.commands.contexts.CommandContext;
import ofc.bot.handlers.commands.responses.results.CommandResult;
import ofc.bot.handlers.commands.responses.results.Status;
import ofc.bot.handlers.commands.slash.SlashCommand;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static ofc.bot.databases.entities.tables.FormerMembersRoles.FORMER_MEMBERS_ROLES;

@DiscordCommand(name = "backup", description = "Recupere todos os cargos de um membro que saiu anteriormente (ou não).", autoDefer = true)
@CommandPermission(Permission.MANAGE_SERVER)
public class BackupMemberRoles extends SlashCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupMemberRoles.class);

    @Option(required = true)
    private static final OptionData TARGET = new OptionData(OptionType.USER, "user", "O alvo a devolver os cargos.");

    @Option
    private static final OptionData DELETE = new OptionData(OptionType.BOOLEAN, "delete", "Se devemos limpar os cargos do backup assim que eles forem devolvidos ao membro (Padrão: True).");

    @Override
    public CommandResult onCommand(CommandContext ctx) {

        Member target = ctx.getOption("user", OptionMapping::getAsMember);
        Guild guild = ctx.getGuild();
        boolean deletion = ctx.getOption("delete", true, OptionMapping::getAsBoolean);

        if (target == null)
            return Status.MEMBER_NOT_FOUND;

        long targetId = target.getIdLong();
        long guildId = guild.getIdLong();
        List<FormerMemberRoleRecord> rolesRecord = retrieveFormerRoles(guildId, targetId);
        List<Role> roles = resolveRoles(guild, rolesRecord);

        if (roles.isEmpty())
            return Status.ROLES_NOT_FOUND_TO_BACKUP;

        guild.modifyMemberRoles(target, roles, null).queue((s) -> {

            String name = target.getUser().getEffectiveName();
            int amount = roles.size();

            ctx.reply(Status.ROLES_SUCCESSFULLY_BACKED_UP.args(amount, name));

            if (deletion)
                deleteBackup(guildId, targetId);

        }, (e) -> {
            LOGGER.error("Could not backup roles of member '{}' in guild '{}'", targetId, guildId);
            ctx.reply(Status.COULD_NOT_EXECUTE_SUCH_OPERATION);
        });

        return Status.PASSED;
    }

    private List<Role> resolveRoles(Guild guild, List<FormerMemberRoleRecord> records) {

        List<Role> roles = guild.getRoles();
        List<Long> ids = records.stream()
                .map(FormerMemberRoleRecord::getRoleId)
                .toList();

        return roles.stream()
                .filter(r -> ids.contains(r.getIdLong()))
                .toList();
    }

    private List<FormerMemberRoleRecord> retrieveFormerRoles(long guildId, long userId) {

        DSLContext ctx = DBManager.getContext();

        return ctx.selectFrom(FORMER_MEMBERS_ROLES)
                .where(FORMER_MEMBERS_ROLES.GUILD.eq(guildId).and(FORMER_MEMBERS_ROLES.USER.eq(userId)))
                .fetch();
    }

    private void deleteBackup(long guildId, long userId) {

        DSLContext ctx = DBManager.getContext();

        ctx.deleteFrom(FORMER_MEMBERS_ROLES)
                .where(FORMER_MEMBERS_ROLES.GUILD.eq(guildId).and(FORMER_MEMBERS_ROLES.USER.eq(userId)))
                .execute();
    }
}