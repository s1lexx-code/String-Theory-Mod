package com.example.stringtheory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BraneAnchorBlockEntity extends BlockEntity {
    public static final int CHARGE_TICKS = 40; // 2 секунды стояния на якоре

    private int charge;
    private boolean armed = true; // после перехода якорь «разряжен», пока игрок с него не сойдёт

    public BraneAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRANE_ANCHOR.get(), pos, state);
    }

    public void disarm() {
        armed = false;
        charge = 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BraneAnchorBlockEntity be) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int mode = state.getValue(BraneAnchorBlock.MODE);
        if (mode == 0) {
            be.charge = 0;
            return;
        }
        AABB zone = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        List<ServerPlayer> players = server.getEntitiesOfClass(ServerPlayer.class, zone);
        if (players.isEmpty()) {
            be.armed = true;
            be.charge = 0;
            return;
        }
        if (!be.armed) {
            return;
        }
        ServerPlayer player = players.get(0);
        be.charge++;
        if (be.charge % 2 == 0) {
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    4, 0.3, 0.5, 0.3, 0.05);
        }
        if (be.charge % 20 == 1) {
            server.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.6F + 0.2F * mode);
        }
        if (be.charge >= CHARGE_TICKS) {
            be.charge = 0;
            if (!be.teleport(player, server, mode)) {
                be.armed = false; // чтобы не повторять сообщение, пока игрок стоит на месте
                player.displayClientMessage(Component.translatable("msg." + StringTheoryMod.MODID + ".no_partner"), true);
            }
        }
    }

    private boolean teleport(ServerPlayer player, ServerLevel level, int mode) {
        MinecraftServer server = level.getServer();
        BraneNetwork network = BraneNetwork.get(server);
        GlobalPos self = GlobalPos.of(level.dimension(), worldPosition);
        List<GlobalPos> list = new ArrayList<>(network.get(mode));
        int size = list.size();
        int index = list.indexOf(self);

        for (int step = 1; step <= size; step++) {
            GlobalPos target = list.get(Math.floorMod(index + step, size));
            if (target.equals(self)) {
                continue;
            }
            ServerLevel destination = server.getLevel(target.dimension());
            if (destination == null) {
                network.remove(mode, target);
                continue;
            }
            BlockState targetState = destination.getBlockState(target.pos());
            if (!targetState.is(ModBlocks.BRANE_ANCHOR.get()) || targetState.getValue(BraneAnchorBlock.MODE) != mode) {
                network.remove(mode, target); // якорь исчез или перенастроен
                continue;
            }
            if (destination.getBlockEntity(target.pos()) instanceof BraneAnchorBlockEntity other) {
                other.disarm();
            }

            effects(level, worldPosition);
            player.teleportTo(destination, target.pos().getX() + 0.5, target.pos().getY() + 1.0,
                    target.pos().getZ() + 0.5, player.getYRot(), player.getXRot());
            effects(destination, target.pos());
            return true;
        }
        return false;
    }

    private static void effects(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8F, 0.6F);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                20, 0.3, 0.6, 0.3, 0.05);
    }
}
