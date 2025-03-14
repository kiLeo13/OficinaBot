package ofc.bot.handlers.interactions.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import ofc.bot.domain.entity.CommandHistory;
import ofc.bot.domain.sqlite.repository.AppUserBanRepository;
import ofc.bot.domain.sqlite.repository.CommandHistoryRepository;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.SlashCommandsRegistryManager;
import ofc.bot.handlers.interactions.commands.slash.abstractions.ICommand;
import ofc.bot.util.Bot;
import ofc.bot.util.content.annotations.listeners.DiscordEventHandler;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@DiscordEventHandler
public class SlashCommandsGateway extends ListenerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlashCommandsGateway.class);
    private static final ExecutorService EXECUTOR = ForkJoinPool.commonPool();
    private final CommandHistoryRepository cmdRepo;
    private final AppUserBanRepository appBanRepo;

    public SlashCommandsGateway(CommandHistoryRepository cmdRepo, AppUserBanRepository appBanRepo) {
        this.cmdRepo = cmdRepo;
        this.appBanRepo = appBanRepo;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        Guild guild = e.getGuild();
        String fullName = e.getFullCommandName();
        ICommand<SlashCommandContext> cmd = SlashCommandsRegistryManager.getCommand(fullName);
        Member member = e.getMember();

        if (guild == null || member == null) return;

        if (cmd == null) {
            e.reply("""
            > :x: Este comando não foi encontrado ou não existe mais.
            Por favor, reabra o seu aplicativo e tente novamente.\
            Se estiver no computador, apenas pressione `Ctrl + R`.
            """).setEphemeral(true).queue();
            return; // Command not found by name
        }

        SlashCommandContext ctx = new SlashCommandContext(e);
        User user = ctx.getUser();
        long userId = user.getIdLong();

        // Is the user banned from our application?
        if (appBanRepo.isBanned(userId)) {
            ctx.reply(Status.YOU_ARE_BANNED_FROM_THIS_BOT);
            return;
        }

        long remains = cmd.cooldownRemain(userId);
        if (!member.hasPermission(Permission.MANAGE_SERVER) && cmd.inCooldown(userId)) {
            ctx.reply(Status.PLEASE_WAIT_COOLDOWN.args(Bot.parsePeriod(remains)));
            LOGGER.warn("User '@{} [{}]' hit command \"/{}\" rate-limit, time remain: {}s", user.getName(), userId, fullName, remains);
            return;
        }

        handleCommand(ctx, cmd);
        cmd.tickCooldown(userId);
    }

    private void handleCommand(SlashCommandContext ctx, ICommand<SlashCommandContext> cmd) {
        SlashCommandInteraction itr = ctx.getInteraction();
        MessageChannel channel = ctx.getChannel();
        User user = ctx.getUser();
        String userName = user.getName();
        String cmdName = itr.getFullCommandName();
        long userId = user.getIdLong();
        long guildId = ctx.getGuildId();

        EXECUTOR.execute(() -> {
            try {
                // Not the best way of getting the response time,
                // but it's more about aesthetics than debugging itself :)
                long start = System.currentTimeMillis();
                InteractionResult res = cmd.onSlashCommand(ctx);
                Status status = res.getStatus();
                long end = System.currentTimeMillis();
                long duration = end - start;

                LOGGER.info("@{} issued \"/{}\" at \"#{}\": status {}, took {}ms",
                        userName, cmdName, channel.getName(), status, duration);

                if (res.getContent() != null) {
                    ctx.reply(res);
                }

                CommandHistory entry = new CommandHistory(cmdName, status, guildId, userId);
                cmdRepo.save(entry);
            } catch (DataAccessException e) {
                LOGGER.warn("Could not save Slash Command activity to database", e);
            } catch (Throwable e) {
                LOGGER.error("Command execution triggered by @{} [{}], as \"/{}\", at \"{}\" failed",
                        userName, userId, cmdName, ctx.getTimeCreated(), e
                );
                ctx.reply("Ocorreu um erro :/", true);
            }
        });
    }
}