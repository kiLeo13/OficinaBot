package ofc.bot.commands.slash;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;
import org.jetbrains.annotations.NotNull;

@DiscordCommand(name = "changelog", permissions = Permission.ADMINISTRATOR)
public class CreateChangelogEntryCommand extends SlashCommand {

    @Override
    public InteractionResult onCommand(@NotNull SlashCommandContext ctx) {
        TextInput content = TextInput.create("content", "Content", TextInputStyle.PARAGRAPH)
                .setMaxLength(Message.MAX_CONTENT_LENGTH)
                .build();
        TextInput attachments = TextInput.create("attachments", "Attachments", TextInputStyle.PARAGRAPH)
                .setRequired(false)
                .build();

        Modal modal = Modal.create("changelog-entry", "✈️ New Changelog Entry")
                .addActionRow(content)
                .addActionRow(attachments)
                .build();

        return ctx.replyModal(modal);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Cria um novo registro no changelog.";
    }
}