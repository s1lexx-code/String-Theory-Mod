package com.example.stringtheory;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StringTheoryMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + StringTheoryMod.MODID))
                    .icon(() -> ModItems.STRING_SHARD.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.STRING_SHARD.get());
                        output.accept(ModItems.TUNING_FORK.get());
                        output.accept(ModItems.STRING_ORE.get());
                        output.accept(ModItems.CALABI_STONE.get());
                        output.accept(ModItems.RESONATOR.get());
                    })
                    .build());
}
