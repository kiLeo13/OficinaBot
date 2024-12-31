package ofc.bot.commands.groups.channel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import ofc.bot.Main;
import ofc.bot.domain.entity.OficinaGroup;
import ofc.bot.domain.entity.enums.StoreItemType;
import ofc.bot.domain.sqlite.repository.OficinaGroupRepository;
import ofc.bot.handlers.interactions.buttons.contexts.ButtonContextFactory;
import ofc.bot.handlers.interactions.commands.contexts.impl.SlashCommandContext;
import ofc.bot.handlers.interactions.commands.responses.states.InteractionResult;
import ofc.bot.handlers.interactions.commands.responses.states.Status;
import ofc.bot.handlers.interactions.commands.slash.abstractions.SlashSubcommand;
import ofc.bot.util.content.annotations.commands.DiscordCommand;

import java.util.List;

@DiscordCommand(
        name = "group channel create",
        description = "Cria um novo canal ao grupo.",
        cooldown = 10
)
public class CreateGroupChannelCommand extends SlashSubcommand {
    private final OficinaGroupRepository grpRepo;

    public CreateGroupChannelCommand(OficinaGroupRepository grpRepo) {
        this.grpRepo = grpRepo;
    }

    @Override
    public InteractionResult onSlashCommand(SlashCommandContext ctx) {
        long userId = ctx.getUserId();
        OficinaGroup group = grpRepo.findByOwnerId(userId);
        ChannelType chanType = ctx.getSafeEnumOption("type", ChannelType.class);
        Guild guild = ctx.getGuild();

        if (group == null)
            return Status.YOU_DO_NOT_OWN_A_GROUP;

        if (hitMaxChannels(guild))
            return Status.GROUP_CHANNELS_CANNOT_BE_CREATED;

        // Err... is this even possible?
        if (chanType != ChannelType.TEXT && chanType != ChannelType.VOICE)
            return Status.INVALID_CHANNEL_TYPE.args(chanType);

        if (hasChannelOfType(group, chanType))
            return Status.GROUP_ALREADY_HAS_THE_PROVIDED_CHANNEL;

        Category category = resolveChannelCategory(chanType);
        StoreItemType itemType = resolveStoreItem(chanType);
        int price = group.hasFreeAccess() ? 0 : itemType.getPrice();

        if (category == null)
            return Status.CHANNEL_CATEGORY_NOT_FOUND;

        Button confirmButton = ButtonContextFactory.createGroupChannelConfirmationButton(group, price, category, itemType);
        return ctx.create()
                .setContent(Status.CONFIRM_GROUP_CHANNEL_CREATION.args(chanType))
                .setActionRow(confirmButton)
                .send();
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "type", "O tipo de canal a ser criado", true)
                        .addChoice("🔊 Voice", "VOICE")
                        .addChoice("📖 Text", "TEXT")
        );
    }

    private Category resolveChannelCategory(ChannelType type) {
        JDA api = Main.getApi();
        return type == ChannelType.TEXT
                ? api.getCategoryById(OficinaGroup.TEXT_CATEGORY_ID)
                : api.getCategoryById(OficinaGroup.VOICE_CATEGORY_ID);
    }

    private boolean hasChannelOfType(OficinaGroup group, ChannelType type) {
        return type == ChannelType.TEXT
                ? group.hasTextChannel()
                : group.hasVoiceChannel();
    }

    private boolean hitMaxChannels(Guild guild) {
        int chanSize = guild.getChannels().size();
        // Yes, this is a magic value.
        // Since JDA does not provide a constant value for this, I am keeping it as a magic value.
        return chanSize >= 500;
    }

    private StoreItemType resolveStoreItem(ChannelType type) {
        return type == ChannelType.TEXT
                ? StoreItemType.GROUP_TEXT_CHANNEL
                : StoreItemType.GROUP_VOICE_CHANNEL;
    }
}