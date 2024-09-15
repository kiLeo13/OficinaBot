package ofc.bot.store;

import net.dv8tion.jda.api.entities.Member;
import ofc.bot.util.Annotations;
import ofc.bot.util.content.annotations.store.ShopItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class StoreItem {
    private final String id;
    private final String name;
    private final String description;
    private final int price;

    public abstract boolean onItemBuy(Member member);

    public StoreItem() {
        this.id = getValue(ShopItem::id);
        this.name = getValue(ShopItem::name);
        this.description = getValue(ShopItem::description);
        this.price = getValue(ShopItem::price);
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public String getDescription() {
        return this.description;
    }

    public int getPrice() {
        return this.price;
    }

    private static <T> T getValue(Function<ShopItem, T> mapper) {
        return Annotations.get(StoreItem.class, ShopItem.class, mapper);
    }
}