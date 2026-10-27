package com.example.stringtheory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ResonatorMenu extends AbstractContainerMenu {
    private static final int[] BUTTON_DELTAS = {-10, -1, 1, 10};
    public static final int FORK_BUTTON = 4;
    private static final int FORK_COOLDOWN = 60;

    private final ResonatorBlockEntity blockEntity;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    /** Клиентский конструктор. */
    public ResonatorMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory,
                (ResonatorBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(4));
    }

    /** Серверный конструктор. */
    public ResonatorMenu(int id, Inventory inventory, ResonatorBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.RESONATOR.get(), id);
        this.blockEntity = blockEntity;
        this.data = data;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        ItemStackHandler handler = blockEntity.getItems();
        addSlot(new SlotItemHandler(handler, 0, 44, 74));
        addSlot(new SlotItemHandler(handler, 1, 116, 74) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 166));
        }

        addDataSlots(data);
    }

    public int getFrequency() {
        return data.get(0);
    }

    public int getProximity() {
        return data.get(1);
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getHint() {
        return data.get(3);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == FORK_BUTTON) {
            Item fork = ModItems.TUNING_FORK.get();
            if (player.getInventory().countItem(fork) > 0 && !player.getCooldowns().isOnCooldown(fork)) {
                player.getCooldowns().addCooldown(fork, FORK_COOLDOWN);
                blockEntity.strikeFork(player);
            }
            return true;
        }
        if (id < 0 || id >= BUTTON_DELTAS.length) {
            return false;
        }
        blockEntity.adjustFrequency(BUTTON_DELTAS[id]);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RESONATOR.get());
    }
}
