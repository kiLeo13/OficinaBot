package ofc.bot.domain.entity.enums;

import java.util.List;

public enum StoreItemType {
    GROUP(              "Grupo",                600_000),
    GROUP_TEXT_CHANNEL( "Chat de Texto",        412_500),
    GROUP_VOICE_CHANNEL("Chat de Voz",          300_000),
    UPDATE_GROUP(       "Modificação de Grupo", 187_500),
    ADDITIONAL_BOT(     "Bot Adicional",        80_000),
    GROUP_SLOT(         "Vaga de Grupo",        75_000),
    GROUP_PERMISSION(   "Permissão de Grupo",   15_000),
    PIN_MESSAGE(        "Fixar Mensagem",       8_000);
    
    private final String name;
    private final int price;
    
    StoreItemType(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public int getPrice() {
        return this.price;
    }

    public static List<StoreItemType> getGroupRefundable() {
        return List.of(
                GROUP,
                GROUP_TEXT_CHANNEL,
                GROUP_VOICE_CHANNEL
        );
    }

    public static StoreItemType fromName(String name) {
        for (StoreItemType type : StoreItemType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }
}