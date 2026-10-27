package com.example.stringtheory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, StringTheoryMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ResonatorMenu>> RESONATOR =
            MENUS.register("resonator", () -> IMenuTypeExtension.create(ResonatorMenu::new));
}
