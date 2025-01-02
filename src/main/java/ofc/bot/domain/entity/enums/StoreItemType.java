package ofc.bot.domain.entity.enums;

import java.util.List;

public enum StoreItemType {
    GROUP(              "Grupo",           800_000),
    GROUP_TEXT_CHANNEL( "Chat de Texto",   550_000),
    ADDITIONAL_BOT(     "Bot Adicional",   450_000),
    GROUP_VOICE_CHANNEL("Chat de Voz",     400_000),
    UPDATE_GROUP(       "Modificar Grupo", 250_000),
    GROUP_SLOT(         "Vaga de Grupo",   100_000);
    
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