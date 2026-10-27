package com.example.stringtheory;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(StringTheoryMod.MODID);

    public static final DeferredBlock<Block> STRING_ORE = BLOCKS.registerBlock("string_ore",
            props -> new DropExperienceBlock(UniformInt.of(1, 3), props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 3)
                    .sound(SoundType.AMETHYST));

    public static final DeferredBlock<Block> CALABI_STONE = BLOCKS.registerSimpleBlock("calabi_stone",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 1)
                    .sound(SoundType.AMETHYST_CLUSTER));

    /** Резонатор: теперь с интерфейсом и блок-сущностью. */
    public static final DeferredBlock<Block> RESONATOR = BLOCKS.registerBlock("resonator",
            ResonatorBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .sound(SoundType.AMETHYST));

    /** Якорь браны: телепорт между якорями с одинаково настроенной струной. */
    public static final DeferredBlock<Block> BRANE_ANCHOR = BLOCKS.registerBlock("brane_anchor",
            BraneAnchorBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(BraneAnchorBlock.MODE) > 0 ? 12 : 2)
                    .sound(SoundType.AMETHYST));
}
