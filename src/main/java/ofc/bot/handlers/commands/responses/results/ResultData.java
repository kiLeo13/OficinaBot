package ofc.bot.handlers.commands.responses.results;

public class ResultData implements CommandResult {

    public static final Object[] EMPTY_ARGS = new Object[0];

    private final Status data;
    private final Object[] args;
    private boolean ephemeral;

    public ResultData(Status data, Object[] args, boolean ephemeral) {
        this.data = data;
        this.args = args;
        this.ephemeral = ephemeral;
    }

    public ResultData(Status data, Object[] args) {
        this(data, args, data.isEphemeral());
    }

    @Override
    public String getContent() {
        String format = data.getContent();
        return format == null ? null : String.format(format, args);
    }

    @Override
    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public Status getStatus() {
        return this.data;
    }

    @Override
    public CommandResult setEphm(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    @Override
    public boolean isEphemeral() {
        return this.ephemeral;
    }
}