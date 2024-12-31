package ofc.bot.domain.entity.enums;

public enum RentStatus {
    TRIAL(  "Isenção Inicial"),
    FREE(   "Isento"),
    PAID(   "Pago"),
    PENDING("Pendente");

    private final String displayStatus;

    RentStatus(final String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus() {
        return this.displayStatus;
    }

    public static RentStatus fromName(String name) {
        for (RentStatus status : RentStatus.values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }

        return null;
    }
}