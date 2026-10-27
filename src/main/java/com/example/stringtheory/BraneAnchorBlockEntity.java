package com.example.stringtheory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public class BraneAnchorBlockEntity extends BlockEntity {
    public static final int CHARGE_TICKS = 40; // 2 секунды стояния на якоре

    private int charge;
    private boolean armed = true; // после перехода якорь «разряжен», пока игрок с него не сойдёт

    /** Только у зеркальных якорей: откуда пришёл игрок. */
    private GlobalPos origin;

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
            String failure = be.activate(player, server, mode);
            if (failure != null) {
                be.armed = false; // чтобы не повторять сообщение, пока игрок стоит на месте
                player.displayClientMessage(Component.translatable("msg." + StringTheoryMod.MODID + "." + failure), true);
            }
        }
    }

    /** Возвращает null при успехе, иначе суффикс ключа сообщения об ошибке. */
    private String activate(ServerPlayer player, ServerLevel level, int mode) {
        if (origin != null) {
            return returnToOrigin(player, level);
        }
        if (player.isShiftKeyDown() && level.dimension() != ModDimensions.CALABI_YAU) {
            return foldToCalabi(player, level, mode);
        }
        return teleportAlongNetwork(player, level, mode) ? null : "no_partner";
    }

    private boolean teleportAlongNetwork(ServerPlayer player, ServerLevel level, int mode) {
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

    /** Shift на якоре: «сворачивание» в измерение Калаби-Яу в той же точке (с учётом замыкания мира). */
    private String foldToCalabi(ServerPlayer player, ServerLevel level, int mode) {
        ServerLevel calabi = level.getServer().getLevel(ModDimensions.CALABI_YAU);
        if (calabi == null) {
            return "no_dimension";
        }
        BlockPos target = new BlockPos(ModDimensions.wrap(worldPosition.getX()), ModDimensions.FLOOR_Y,
                ModDimensions.wrap(worldPosition.getZ()));

        calabi.setBlock(target, ModBlocks.MIRROR_ANCHOR.get().defaultBlockState()
                .setValue(BraneAnchorBlock.MODE, mode), Block.UPDATE_ALL);
        if (calabi.getBlockEntity(target) instanceof BraneAnchorBlockEntity mirror) {
            mirror.origin = GlobalPos.of(level.dimension(), worldPosition);
            mirror.disarm();
            mirror.setChanged();
        }

        effects(level, worldPosition);
        player.teleportTo(calabi, target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5,
                player.getYRot(), player.getXRot());
        effects(calabi, target);
        player.displayClientMessage(Component.translatable("msg." + StringTheoryMod.MODID + ".folded"), true);
        return null;
    }

    /** Стоя на зеркальном якоре: возврат к якорю, из которого пришли (или в мир, если его уже нет). */
    private String returnToOrigin(ServerPlayer player, ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerLevel destination = server.getLevel(origin.dimension());
        double x;
        double y;
        double z;
        if (destination != null && destination.getBlockState(origin.pos()).is(ModBlocks.BRANE_ANCHOR.get())) {
            BlockPos p = origin.pos();
            if (destination.getBlockEntity(p) instanceof BraneAnchorBlockEntity other) {
                other.disarm();
            }
            x = p.getX() + 0.5;
            y = p.getY() + 1.0;
            z = p.getZ() + 0.5;
        } else {
            destination = server.overworld();
            BlockPos spawn = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, destination.getSharedSpawnPos());
            x = spawn.getX() + 0.5;
            y = spawn.getY();
            z = spawn.getZ() + 0.5;
        }
        effects(level, worldPosition);
        player.teleportTo(destination, x, y, z, player.getYRot(), player.getXRot());
        effects(destination, BlockPos.containing(x, y, z));
        player.displayClientMessage(Component.translatable("msg." + StringTheoryMod.MODID + ".unfolded"), true);
        return null;
    }

    private static void effects(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.8F, 0.6F);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                20, 0.3, 0.6, 0.3, 0.05);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (origin != null) {
            tag.putString("OriginDim", origin.dimension().location().toString());
            tag.putInt("OriginX", origin.pos().getX());
            tag.putInt("OriginY", origin.pos().getY());
            tag.putInt("OriginZ", origin.pos().getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("OriginDim")) {
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("OriginDim")));
            origin = GlobalPos.of(dim, new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ")));
        } else {
            origin = null;
        }
    }
}
