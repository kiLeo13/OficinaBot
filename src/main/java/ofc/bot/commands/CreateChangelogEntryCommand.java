package ofc.bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashCommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

@DiscordCommand(name = "changelog", permission = Permission.ADMINISTRATOR)
public class CreateChangelogEntryCommand extends SlashCommand {

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
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

    @Override
    protected void init() {
        setDesc("Cria um novo registro no changelog.");
    }
}