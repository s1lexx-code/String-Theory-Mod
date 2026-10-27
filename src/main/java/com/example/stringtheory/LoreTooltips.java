package com.example.stringtheory;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = StringTheoryMod.MODID, value = Dist.CLIENT)
public class LoreTooltips {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        String key = event.getItemStack().getDescriptionId() + ".lore";
        if (I18n.exists(key)) {
            event.getToolTip().add(Component.translatable(key)
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
