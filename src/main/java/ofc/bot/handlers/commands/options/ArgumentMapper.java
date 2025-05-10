package ofc.bot.handlers.commands.options;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.Main;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ArgumentMapper {
    private final String value;
    private final JDA api;

    public ArgumentMapper(String value) {
        this.value = value;
        this.api = Main.getApi();
    }

    public String getAsString() {
        return this.value;
    }

    public byte getAsByte() {
        return Byte.parseByte(this.value);
    }

    public short getAsShort() {
        return Short.parseShort(this.value);
    }

    public int getAsInt() {
        return Integer.parseInt(this.value);
    }

    public long getAsLong() {
        return Long.parseLong(this.value);
    }

    public float getAsFloat() {
        return Float.parseFloat(this.value);
    }

    public double getAsDouble() {
        return Double.parseDouble(this.value);
    }

    public boolean getAsBoolean() {
        return Boolean.parseBoolean(this.value);
    }

    @NotNull
    public CacheRestAction<User> getAsUser() {
        Checks.isSnowflake(this.value);
        return api.retrieveUserById(this.value);
    }

    public User retrieveAsUser() {
        return getAsUser().complete();
    }

    public <T extends Enum<T>> T getAsEnum(@NotNull Class<T> enumClass) {
        return Enum.valueOf(enumClass, this.value);
    }

    public <T extends Enum<T>> T getAsEnum(@NotNull Function<String, T> mapper) {
        return mapper.apply(this.value);
    }

    public <T> T get(Function<String, T> mapper) {
        return mapper.apply(this.value);
    }
}