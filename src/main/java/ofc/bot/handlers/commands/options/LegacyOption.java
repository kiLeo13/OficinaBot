package ofc.bot.handlers.commands.options;

public class LegacyOption {
    private final String name;
    private boolean required;

    public LegacyOption(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }
}