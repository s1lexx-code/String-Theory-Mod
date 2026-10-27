package com.example.stringtheory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Компактификация: дойдя до края измерения Калаби-Яу, игрок оказывается на противоположной стороне. */
@EventBusSubscriber(modid = StringTheoryMod.MODID)
public class CalabiEvents {
    private static final String SEEN_KEY = "stringtheory_wrapped";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().dimension() != ModDimensions.CALABI_YAU) {
            return;
        }
        double x = player.getX();
        double z = player.getZ();
        double newX = ModDimensions.wrap(x);
        double newZ = ModDimensions.wrap(z);
        if (newX == x && newZ == z) {
            return;
        }

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.7F, 0.5F);
        player.connection.teleport(newX, player.getY(), newZ, player.getYRot(), player.getXRot());
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7F, 0.6F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, newX, player.getY() + 1.0, newZ, 20, 0.4, 0.8, 0.4, 0.05);

        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(SEEN_KEY)) {
            data.putBoolean(SEEN_KEY, true);
            player.displayClientMessage(Component.translatable("msg." + StringTheoryMod.MODID + ".wrapped"), true);
        }
    }
}
