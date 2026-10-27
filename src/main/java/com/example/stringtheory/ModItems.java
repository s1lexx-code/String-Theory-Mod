package com.example.stringtheory;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(StringTheoryMod.MODID);

    public static final DeferredItem<Item> STRING_SHARD = ITEMS.registerSimpleItem("string_shard");
    public static final DeferredItem<Item> TUNING_FORK =
            ITEMS.registerSimpleItem("tuning_fork", new Item.Properties().stacksTo(1));

    public static final DeferredItem<BlockItem> STRING_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.STRING_ORE);
    public static final DeferredItem<BlockItem> CALABI_STONE = ITEMS.registerSimpleBlockItem(ModBlocks.CALABI_STONE);
    public static final DeferredItem<BlockItem> RESONATOR = ITEMS.registerSimpleBlockItem(ModBlocks.RESONATOR);
}
