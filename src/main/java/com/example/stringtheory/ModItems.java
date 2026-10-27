package com.example.stringtheory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StringTheoryMod.MODID);

    public static final DeferredItem<Item> STRING_SHARD = ITEMS.registerSimpleItem("string_shard");
    public static final DeferredItem<Item> TUNING_FORK =
            ITEMS.registerSimpleItem("tuning_fork", new Item.Properties().stacksTo(1));

    /** Пять настроенных струн: Тип I, IIA, IIB, SO(32), E8xE8. Индекс 0 = мода 1. */
    public static final List<DeferredItem<Item>> TUNED_STRINGS = new ArrayList<>();
    static {
        for (int i = 1; i <= 5; i++) {
            TUNED_STRINGS.add(ITEMS.registerSimpleItem("tuned_string_" + i));
        }
    }

    public static final DeferredItem<BlockItem> STRING_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.STRING_ORE);
    public static final DeferredItem<BlockItem> CALABI_STONE = ITEMS.registerSimpleBlockItem(ModBlocks.CALABI_STONE);
    public static final DeferredItem<BlockItem> RESONATOR = ITEMS.registerSimpleBlockItem(ModBlocks.RESONATOR);
}
