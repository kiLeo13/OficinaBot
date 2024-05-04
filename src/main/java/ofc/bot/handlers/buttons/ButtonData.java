package ofc.bot.handlers.buttons;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;

public record ButtonData(
        String type,
        Permission permission,

        // this can be any value or even null or a JSON
        Object payload,
        int valueInt,
        long entity
) {
    public boolean isPermitted(IPermissionHolder entity) {
        return permission == null || entity.hasPermission(permission);
    }

    public <T> T payload(Class<T> type) {
        return type.cast(this.payload);
    }
}