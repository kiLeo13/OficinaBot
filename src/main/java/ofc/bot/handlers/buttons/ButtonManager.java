package ofc.bot.handlers.buttons;

import net.dv8tion.jda.api.Permission;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ButtonManager {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final HashMap<String, ButtonData> buttons = new HashMap<>();

    private ButtonManager() {}

    public static Builder create(String id) {

        if (id == null)
            throw new IllegalArgumentException("Button ids cannot be null");

        return new Builder(id);
    }

    public static ButtonData get(String id) {
        return buttons.get(id);
    }

    public static String getType(String buttonId) {

        if (buttonId == null)
            return null;

        ButtonData button = buttons.get(buttonId);

        return button != null
                ? button.type()
                : null;
    }

    protected static void remove(String id) {
        buttons.remove(id);
    }

    private static void add(String id, ButtonData data, int delayMinutes) {

        if (id == null || data == null)
            throw new IllegalArgumentException("Neither ID or ButtonData may be null");

        buttons.put(id, data);
        scheduler.schedule(() -> buttons.remove(id), delayMinutes, TimeUnit.MINUTES);
    }

    public static class Builder {
        private final String id;
        private String taskType;
        private Permission perm = null;
        private String payload;
        private int valueInt = 0;
        private int expiresAfter = 2;
        private long entity;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setIdentity(String type) {
            this.taskType = type;
            return this;
        }

        public Builder setPermission(Permission perm) {
            this.perm = perm;
            return this;
        }

        public Builder setPayload(String data) {
            this.payload = data;
            return this;
        }

        public Builder setValueInt(int value) {
            this.valueInt = value;
            return this;
        }

        public Builder setEntity(long value) {
            this.entity = value;
            return this;
        }

        public Builder setExpires(int minutes) {

            if (minutes <= 0)
                throw new IllegalArgumentException("Minutes may not be less or equal to zero");

            this.expiresAfter = minutes;
            return this;
        }

        public void insert() {

            if (taskType == null)
                throw new IllegalArgumentException("TaskType cannot be null");

            add(id, new ButtonData(taskType, perm, payload, valueInt, entity), expiresAfter);
        }
    }
}