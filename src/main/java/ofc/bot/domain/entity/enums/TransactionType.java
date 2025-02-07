package ofc.bot.domain.entity.enums;

public enum TransactionType {
    MONEY_TRANSFERRED(false, "🏧 Quantia Transferida"),
    BALANCE_UPDATED(  false, "📨 Saldo Actualizado"),
    BALANCE_SET(      false, "📩 Saldo Definido"),
    DAILY_COLLECTED(  false, "🏆 Daily Coletado"),
    WORK_EXECUTED(    false, "💼 Trabalhou"),
    CHAT_MONEY(       false, "💰 Chat Money"),
    MARRIAGE_CREATED( false, "💍 Casamento"),
    FEE_PAID(         false, "\uD83C\uDFE6 Taxa Paga"),
    INVOICE_PAID(     false, "\uD83E\uDDFE Fatura Paga"),
    ITEM_BOUGHT(      true,  "🛍 Item Comprado"),
    ITEM_SOLD(        true,  "🛒 Item Vendido");

    private final boolean applicableOnItems;
    private final String name;

    TransactionType(boolean applicableOnItems, String name) {
        this.applicableOnItems = applicableOnItems;
        this.name = name;
    }

    public boolean isApplicableOnItems() {
        return this.applicableOnItems;
    }

    public String getName() {
        return this.name;
    }

    public static TransactionType fromName(String name) {
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}