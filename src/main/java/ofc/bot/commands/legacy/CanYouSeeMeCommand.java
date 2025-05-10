package ofc.bot.commands.legacy;

import ofc.bot.handlers.commands.contexts.impl.MessageCommandContext;
import ofc.bot.handlers.commands.legacy.abstractions.MessageCommand;
import ofc.bot.handlers.commands.options.ArgumentMapper;
import ofc.bot.handlers.commands.responses.states.InteractionResult;
import ofc.bot.handlers.commands.responses.states.Status;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@DiscordCommand(name = "cysm <ip> <port>")
public class CanYouSeeMeCommand extends MessageCommand {

    @Override
    public InteractionResult onCommand(@NotNull MessageCommandContext ctx) {
        String ip = ctx.getSafeOption("ip", ArgumentMapper::getAsString);
        int port = ctx.getSafeOption("port", ArgumentMapper::getAsInt);
        int timeout = ctx.getSafeOption("timeout", ArgumentMapper::getAsInt);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeoutMillis);
            return true; // Port is open
        } catch (IOException e) {
            return false; // Port is closed or filtered
        }
        return Status.OK;
    }

    @NotNull
    @Override
    public  String getDescription() {
        return "Verifica o endereço fornecido está acessível.";
    }
}