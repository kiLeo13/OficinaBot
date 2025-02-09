package ofc.bot.domain.entity.enums;

import java.util.Arrays;
import java.util.List;

public enum TransactionType {
    MONEY_TRANSFERRED(false, "Quantia Transferida"),
    BALANCE_UPDATED(  false, "Saldo Actualizado"),
    BALANCE_SET(      false, "Saldo Definido"),
    DAILY_COLLECTED(  false, "Daily Coletado"),
    WORK_EXECUTED(    false, "Trabalhou"),
    CHAT_MONEY(       false, "Chat Money"),
    MARRIAGE_CREATED( false, "Casamento"),
    FEE_PAID(         false, "Taxa Paga"),
    INVOICE_PAID(     false, "Fatura Paga"),
    ITEM_BOUGHT(      true,  "Item Comprado"),
    ITEM_SOLD(        true,  "Item Vendido");

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

    public static List<TransactionType> allExcept(TransactionType... types) {
        List<TransactionType> typesList = List.of(types);
        return Arrays.stream(values())
                .filter(t -> !typesList.contains(t))
                .toList();
    }
}