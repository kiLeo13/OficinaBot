package ofc.bot.store.items;

import net.dv8tion.jda.api.entities.Member;
import ofc.bot.store.StoreItem;
import ofc.bot.util.content.annotations.store.ShopItem;

@ShopItem(
        id = "marriage_slot",
        name = "💍 Slot de Casamento",
        description = "Aumente seu limite de casamentos.",
        price = 80000
)
public class MarriageSlot extends StoreItem {

    @Override
    public boolean onItemBuy(Member member) {

        return true;
    }
}