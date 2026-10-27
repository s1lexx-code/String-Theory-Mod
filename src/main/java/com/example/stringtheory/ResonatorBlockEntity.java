package com.example.stringtheory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ResonatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int TOLERANCE = 3;       // насколько точно надо попасть в частоту
    public static final int AUDIBLE_RANGE = 60;  // с какого расстояния слышно «зов» частоты
    public static final int LOCK_TICKS = 60;     // 3 секунды удержания
    public static final int MAX_FREQUENCY = 999;

    private final ItemStackHandler items = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.is(ModItems.STRING_SHARD.get());
        }
    };

    private int frequency = 500;
    private int proximity;
    private int progress;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> frequency;
                case 1 -> proximity;
                case 2 -> progress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> frequency = value;
                case 1 -> proximity = value;
                case 2 -> progress = value;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public ResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONATOR.get(), pos, state);
    }

    /** Пять скрытых частот мира: по одной в своей полосе, зависят от seed мира. */
    public static int[] modeFrequencies(long seed) {
        int[] result = new int[5];
        for (int i = 0; i < 5; i++) {
            RandomSource random = RandomSource.create(seed * 31L + i * 7919L);
            result[i] = 100 + i * 170 + random.nextInt(150);
        }
        return result;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public void adjustFrequency(int delta) {
        frequency = Mth.clamp(frequency + delta, 0, MAX_FREQUENCY);
        setChanged();
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(),
                    worldPosition.getZ(), items.getStackInSlot(i));
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonatorBlockEntity be) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int[] targets = modeFrequencies(server.getSeed());
        int best = 0;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < targets.length; i++) {
            int dist = Math.abs(be.frequency - targets[i]);
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        be.proximity = Math.max(0, 100 - bestDist * 100 / AUDIBLE_RANGE);

        boolean canWork = !be.items.getStackInSlot(0).isEmpty() && be.items.getStackInSlot(1).isEmpty();
        if (bestDist <= TOLERANCE && canWork) {
            be.progress++;
            if (be.progress % 20 == 0) {
                server.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS,
                        1.0F, 0.6F + best * 0.2F);
            }
            if (be.progress >= LOCK_TICKS) {
                be.finish(server, pos, best);
            }
        } else if (be.progress > 0) {
            be.progress = Math.max(0, be.progress - 2);
        }
    }

    private void finish(ServerLevel server, BlockPos pos, int mode) {
        items.extractItem(0, 1, false);
        items.setStackInSlot(1, new ItemStack(ModItems.TUNED_STRINGS.get(mode).get()));
        progress = 0;
        server.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 0.9F);
        server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                12, 0.25, 0.2, 0.25, 0.02);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block." + StringTheoryMod.MODID + ".resonator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ResonatorMenu(containerId, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Frequency", frequency);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        if (tag.contains("Frequency")) {
            frequency = tag.getInt("Frequency");
        }
    }
}
