package ofc.bot.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import ofc.bot.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class Bot {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
    private static final Locale LOCALE = new Locale("pt", "BR");
    
    private Bot() {}

    public static Locale defaultLocale() {
        return LOCALE;
    }

    public static List<GatewayIntent> getIntents() {
        return List.of(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_EXPRESSIONS,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.AUTO_MODERATION_EXECUTION,
                GatewayIntent.SCHEDULED_EVENTS,
                GatewayIntent.DIRECT_MESSAGES
        );
    }

    public static int calcMaxPages(int total, int pageSize) {
        int maxPages = total / pageSize;

        if (total % pageSize > 0)
            maxPages++;

        return Math.max(maxPages, 1);
    }

    /**
     * Checks if {@code a + b} overflows either
     * {@link Long#MAX_VALUE} or {@link Long#MIN_VALUE}.
     *
     * @param a the first value.
     * @param b the second value.
     * @return {@code true} if the sum of both values overflows the {@code long}
     * datatype, {@code false} otherwise.
     */
    public static boolean overflows(long a, long b) {
        if (b > 0) return a > Long.MAX_VALUE - b;
        if (b < 0) return a < Long.MIN_VALUE - b;
        return false;
    }

    public static void delete(Message message) {
        message.delete()
                .queue(null, new ErrorHandler()
                        .ignore(ErrorResponse.UNKNOWN_MESSAGE));
    }

    public static String upperFirst(String str) {
        if (str == null || str.isEmpty()) return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static CacheRestAction<User> fetchUser(long userId) {
        return Main.getApi().retrieveUserById(userId);
    }

    public static String parsePeriod(long seconds) {
        if (seconds <= 0)
            return "0s";
        
        StringBuilder builder = new StringBuilder();
        Duration duration = Duration.ofSeconds(seconds);

        long day = duration.toDaysPart();
        int hrs = duration.toHoursPart();
        int min = duration.toMinutesPart();
        int sec = duration.toSecondsPart();

        if (day != 0) builder.append(String.format("%02dd, ", day));
        if (hrs != 0) builder.append(String.format("%02dh, ", hrs));
        if (min != 0) builder.append(String.format("%02dm, ", min));
        if (sec != 0) builder.append(String.format("%02ds, ", sec));

        String result = builder.toString().stripTrailing();
        return result.substring(0, result.length() - 1);
    }

    public static long unixNow() {
        return Instant.now().getEpochSecond();
    }

    public static void writeToFile(String content, File file) {
        try (
                OutputStream out = Files.newOutputStream(Path.of(file.getAbsolutePath()));
                Writer writer = new OutputStreamWriter(out)
        ) {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Could not write to file {}", file.getAbsolutePath(), e);
        }
    }

    public static String fmtNum(long value) {
        if (value == 0) return "0";

        if (value > -10 && value < 10) return String.format("%02d", value);

        NumberFormat currency = NumberFormat.getNumberInstance(LOCALE);

        return currency.format(value);
    }

    public static <T> String format(final List<T> values, final Function<T, String> format) {
        if (values.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();

        for (T value : values) {
            builder.append(format.apply(value));
        }

        return builder.toString().strip();
    }

    /**
     * Returns the {@code int} RGB color of this HEX string.
     *
     * @param hex the HEX color value.
     * @return the RGB value of the color, or {@code -1} if the argument is not of length {@code 6}.
     * @throws NumberFormatException if the {@code String} does not contain a parsable {@code int}.
     */
    public static int hexToRgb(String hex) {
        if (hex.length() != 6) return -1;

        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);

        return (red << 16) | (green << 8) | blue;
    }

    public static final class Colors {
        public static final Color DISCORD = new Color(88, 101, 242);
        public static final Color DEFAULT = new Color(170, 67, 254);
    }
}