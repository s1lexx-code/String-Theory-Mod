package com.example.stringtheory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, StringTheoryMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResonatorBlockEntity>> RESONATOR =
            TYPES.register("resonator", () -> BlockEntityType.Builder
                    .of(ResonatorBlockEntity::new, ModBlocks.RESONATOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BraneAnchorBlockEntity>> BRANE_ANCHOR =
            TYPES.register("brane_anchor", () -> BlockEntityType.Builder
                    .of(BraneAnchorBlockEntity::new, ModBlocks.BRANE_ANCHOR.get())
                    .build(null));
}
