package ofc.bot.handlers.games.uno;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utility class, mapping each existing {@link Card} to an
 * {@link net.dv8tion.jda.api.entities.emoji.ApplicationEmoji ApplicationEmoji}.
 */
public final class CardRenderer {
    private static final Map<String, Emoji> emojis = new HashMap<>();

    private CardRenderer() {}

    @NotNull
    public static Emoji viewCard(Card card) {
        return viewCard(card.toString());
    }

    @NotNull
    public static Emoji viewCard(String name) {
        Emoji view = emojis.get(name);

        if (view == null)
            throw new NoSuchElementException("There is no such renderable card for name \"" + name + '"');

        return view;
    }

    static {
        // Number cards
        emojis.put("BLUE_1",   Emoji.fromFormatted("<:blue_1:1348890522478968914>"));
        emojis.put("BLUE_2",   Emoji.fromFormatted("<:blue_2:1348890523418628180>"));
        emojis.put("BLUE_3",   Emoji.fromFormatted("<:blue_3:1348890524865527828>"));
        emojis.put("BLUE_4",   Emoji.fromFormatted("<:blue_4:1348890526144794707>"));
        emojis.put("BLUE_5",   Emoji.fromFormatted("<:blue_5:1348890527319195660>"));
        emojis.put("BLUE_6",   Emoji.fromFormatted("<:blue_6:1348890529345310730>"));
        emojis.put("BLUE_7",   Emoji.fromFormatted("<:blue_7:1348890530620248105>"));
        emojis.put("BLUE_8",   Emoji.fromFormatted("<:blue_8:1348890531685732384>"));
        emojis.put("BLUE_9",   Emoji.fromFormatted("<:blue_9:1348890533208264705>"));
        emojis.put("GREEN_1",  Emoji.fromFormatted("<:green_1:1348890538820112415>"));
        emojis.put("GREEN_2",  Emoji.fromFormatted("<:green_2:1348890540070146120>"));
        emojis.put("GREEN_3",  Emoji.fromFormatted("<:green_3:1348890541319917609>"));
        emojis.put("GREEN_4",  Emoji.fromFormatted("<:green_4:1348890542599180348>"));
        emojis.put("GREEN_5",  Emoji.fromFormatted("<:green_5:1348890543748550698>"));
        emojis.put("GREEN_6",  Emoji.fromFormatted("<:green_6:1348890545086271541>"));
        emojis.put("GREEN_7",  Emoji.fromFormatted("<:green_7:1348890546755862571>"));
        emojis.put("GREEN_8",  Emoji.fromFormatted("<:green_8:1348890548072747060>"));
        emojis.put("GREEN_9",  Emoji.fromFormatted("<:green_9:1348890549285027896>"));
        emojis.put("RED_1",    Emoji.fromFormatted("<:red_1:1348890554553073764>"));
        emojis.put("RED_2",    Emoji.fromFormatted("<:red_2:1348890555488403540>"));
        emojis.put("RED_3",    Emoji.fromFormatted("<:red_3:1348890557161934858>"));
        emojis.put("RED_4",    Emoji.fromFormatted("<:red_4:1348890558176694354>"));
        emojis.put("RED_5",    Emoji.fromFormatted("<:red_5:1348890560034897971>"));
        emojis.put("RED_6",    Emoji.fromFormatted("<:red_6:1348890561226084362>"));
        emojis.put("RED_7",    Emoji.fromFormatted("<:red_7:1348890562501148704>"));
        emojis.put("RED_8",    Emoji.fromFormatted("<:red_8:1348890563885404170>"));
        emojis.put("RED_9",    Emoji.fromFormatted("<:red_9:1348890565021798470>"));
        emojis.put("YELLOW_1", Emoji.fromFormatted("<:yellow_1:1348890573129519197>"));
        emojis.put("YELLOW_2", Emoji.fromFormatted("<:yellow_2:1348890574807105547>"));
        emojis.put("YELLOW_3", Emoji.fromFormatted("<:yellow_3:1348890576115863553>"));
        emojis.put("YELLOW_4", Emoji.fromFormatted("<:yellow_4:1348890577512697917>"));
        emojis.put("YELLOW_5", Emoji.fromFormatted("<:yellow_5:1348890580482265159>"));
        emojis.put("YELLOW_6", Emoji.fromFormatted("<:yellow_6:1348890582063251517>"));
        emojis.put("YELLOW_7", Emoji.fromFormatted("<:yellow_7:1348890583309094934>"));
        emojis.put("YELLOW_8", Emoji.fromFormatted("<:yellow_8:1348890584550604862>"));
        emojis.put("YELLOW_9", Emoji.fromFormatted("<:yellow_9:1348890585708232774>"));

        // Draw two
        emojis.put("BLUE_DRAW_TWO",   Emoji.fromFormatted("<:blue_draw_two:1348890534764351500>"));
        emojis.put("GREEN_DRAW_TWO",  Emoji.fromFormatted("<:green_draw_two:1348890550606233644>"));
        emojis.put("RED_DRAW_TWO",    Emoji.fromFormatted("<:red_draw_two:1348890566204592219>"));
        emojis.put("YELLOW_DRAW_TWO", Emoji.fromFormatted("<:yellow_draw_two:1348890586760871939>"));

        // Reverse
        emojis.put("BLUE_REVERSE",   Emoji.fromFormatted("<:blue_reverse:1348890536148467773>"));
        emojis.put("GREEN_REVERSE",  Emoji.fromFormatted("<:green_reverse:1348890551688237147>"));
        emojis.put("RED_REVERSE",    Emoji.fromFormatted("<:red_reverse:1348890567945224273>"));
        emojis.put("YELLOW_REVERSE", Emoji.fromFormatted("<:yellow_reverse:1348890587868172300>"));

        // Skip
        emojis.put("BLUE_SKIP",   Emoji.fromFormatted("<:blue_skip:1348890537587118080>"));
        emojis.put("GREEN_SKIP",  Emoji.fromFormatted("<:green_skip:1348890553286398002>"));
        emojis.put("RED_SKIP",    Emoji.fromFormatted("<:red_skip:1348890568939540602>"));
        emojis.put("YELLOW_SKIP", Emoji.fromFormatted("<:yellow_skip:1348890589311275079>"));

        // Wild
        emojis.put("WILD",           Emoji.fromFormatted("<:wild:1348890570487107606>"));
        emojis.put("WILD_DRAW_FOUR", Emoji.fromFormatted("<:wild_draw_four:1348890571904909343>"));
    }
}
