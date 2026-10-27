package com.example.stringtheory;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ModDimensions {
    public static final ResourceKey<Level> CALABI_YAU =
            ResourceKey.create(Registries.DIMENSION, StringTheoryMod.id("calabi_yau"));

    /** Мир замкнут: по каждой оси координаты лежат в [-HALF_SIZE, HALF_SIZE), период = 2 * HALF_SIZE. */
    public static final int HALF_SIZE = 512;
    public static final int PERIOD = HALF_SIZE * 2;

    /** Верхний слой плиты Калаби-Яу (см. dimension/calabi_yau.json: 62 воздуха, 1 бедрок, потом плита). */
    public static final int FLOOR_Y = 63;

    public static int wrap(int v) {
        return Math.floorMod(v + HALF_SIZE, PERIOD) - HALF_SIZE;
    }

    public static double wrap(double v) {
        if (v >= HALF_SIZE) {
            return v - PERIOD;
        }
        if (v < -HALF_SIZE) {
            return v + PERIOD;
        }
        return v;
    }
}
