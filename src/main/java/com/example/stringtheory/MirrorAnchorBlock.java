package com.example.stringtheory;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.BaseEntityBlock;

/**
 * Зеркальный якорь в измерении Калаби-Яу. Создаётся автоматически при «сворачивании»,
 * не ломается, ничего не роняет и ведёт обратно к якорю, из которого пришёл игрок.
 */
public class MirrorAnchorBlock extends BraneAnchorBlock {
    public static final MapCodec<MirrorAnchorBlock> CODEC = simpleCodec(MirrorAnchorBlock::new);

    public MirrorAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isMirror() {
        return true;
    }
}
