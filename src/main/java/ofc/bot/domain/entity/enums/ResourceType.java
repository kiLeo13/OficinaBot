package ofc.bot.domain.entity.enums;

/**
 * Represents different types of resources that can be used in the system.
 * <p>
 * Each {@code ResourceType} has an associated data type, indicating the expected format
 * of the value that represents the resource. This is not the actual Java class/interface
 * for the entity itself (e.g., {@link net.dv8tion.jda.api.entities.Role Role} for {@link #ROLE}),
 * but rather the type of data used to reference it.
 * </p>
 * <p>
 * For example:
 * <ul>
 *   <li>{@link #USER}, {@link #ROLE}, and {@link #CHANNEL} use a {@code long} value,
 *   which corresponds to their respective Discord ID.</li>
 *   <li>{@link #LINK} uses a {@code String}, as it represents a URL.</li>
 * </ul>
 * </p>
 */
public enum ResourceType {
    USER,
    ROLE,
    CHANNEL,
    LINK;
}