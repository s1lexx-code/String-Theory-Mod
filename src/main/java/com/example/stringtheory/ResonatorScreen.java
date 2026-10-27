package com.example.stringtheory;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class ResonatorScreen extends AbstractContainerScreen<ResonatorMenu> {
    private int soundTimer;
    private Button forkButton;
    private static final String[] HINT_KEYS = {"", "unison", "higher_near", "higher_far", "lower_near", "lower_far"};

    public ResonatorScreen(ResonatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.inventoryLabelY = 97;
    }

    @Override
    protected void init() {
        super.init();
        addButton(0, "-10", 8);
        addButton(1, "-1", 38);
        addButton(2, "+1", 110);
        addButton(3, "+10", 140);

        forkButton = Button.builder(Component.translatable("gui.stringtheory.fork"), b -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ResonatorMenu.FORK_BUTTON);
            }
        }).bounds(leftPos + 62, topPos + 90, 52, 14)
          .tooltip(Tooltip.create(Component.translatable("gui.stringtheory.fork.tip")))
          .build();
        addRenderableWidget(forkButton);
    }

    private void addButton(int id, String label, int relX) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
            }
        }).bounds(leftPos + relX, topPos + 50, 28, 14).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (forkButton != null && minecraft != null && minecraft.player != null) {
            forkButton.active = minecraft.player.getInventory().countItem(ModItems.TUNING_FORK.get()) > 0;
        }
        int proximity = menu.getProximity();
        if (proximity > 0 && ++soundTimer >= 8 && minecraft != null) {
            soundTimer = 0;
            float pitch = 0.5F + menu.getFrequency() / 1000.0F * 1.5F;
            float volume = 0.1F + 0.9F * proximity / 100.0F;
            minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_RESONATE, pitch, volume));
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF14101F);
        g.renderOutline(x, y, imageWidth, imageHeight, 0xFF5A3FA0);

        for (Slot slot : menu.slots) {
            g.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xFF2A2140);
            g.fill(x + slot.x, y + slot.y, x + slot.x + 16, y + slot.y + 16, 0xFF0B0814);
        }

        // Осциллограф: при удалении от частоты — шум, при приближении — чистая волна.
        g.fill(x + 8, y + 18, x + 168, y + 46, 0xFF05030A);
        float proximity = menu.getProximity() / 100.0F;
        float time = (Util.getMillis() % 100000L) / 50.0F * 0.35F;
        int color = blend(0xFF7040C0, 0xFF60F0FF, proximity);
        int prev = 0;
        for (int i = 0; i < 160; i++) {
            double a = i * 0.22;
            double clean = Math.sin(a - time);
            double noise = Math.sin(a * 2.7 + time * 1.9) * 0.6 + Math.sin(a * 5.3 - time * 1.3) * 0.4;
            double v = clean * proximity + noise * (1.0 - proximity) * 0.8;
            int py = Mth.clamp(y + 32 + (int) Math.round(v * 10.0), y + 19, y + 44);
            int top = i == 0 ? py : Math.min(prev, py);
            int bottom = i == 0 ? py : Math.max(prev, py);
            g.fill(x + 8 + i, top, x + 9 + i, bottom + 1, color);
            prev = py;
        }

        // Полоска прогресса настройки
        g.fill(x + 68, y + 80, x + 108, y + 84, 0xFF2A2140);
        int width = 40 * menu.getProgress() / ResonatorBlockEntity.LOCK_TICKS;
        g.fill(x + 68, y + 80, x + 68 + width, y + 84, 0xFF60F0FF);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFB9A8FF, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF8A7BC0, false);
        g.drawCenteredString(font, Component.translatable("gui.stringtheory.hz", menu.getFrequency()),
                88, 54, 0xFFFFFFFF);

        int hint = menu.getHint();
        if (hint > 0 && hint < HINT_KEYS.length) {
            g.drawCenteredString(font, Component.translatable("hint.stringtheory." + HINT_KEYS[hint]),
                    88, 65, hint == 1 ? 0xFF60F0FF : 0xFFC9B8FF);
        }
    }

    private static int blend(int a, int b, float t) {
        int r = (int) Mth.lerp(t, (a >> 16) & 255, (b >> 16) & 255);
        int g = (int) Mth.lerp(t, (a >> 8) & 255, (b >> 8) & 255);
        int bl = (int) Mth.lerp(t, a & 255, b & 255);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }
}
