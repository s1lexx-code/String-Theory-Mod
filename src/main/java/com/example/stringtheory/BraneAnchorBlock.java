package com.example.stringtheory;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Якорь браны. MODE = 0 (не настроен) или 1..5 (мода вставленной струны). */
public class BraneAnchorBlock extends BaseEntityBlock {
    public static final MapCodec<BraneAnchorBlock> CODEC = simpleCodec(BraneAnchorBlock::new);
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 5);

    public BraneAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MODE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    /** Зеркальные якоря (в измерении Калаби-Яу) нельзя настраивать и они ничего не роняют. */
    protected boolean isMirror() {
        return false;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BraneAnchorBlockEntity(pos, state);
    }

    /** Возвращает моду (1..5), если предмет — настроенная струна, иначе 0. */
    private static int tunedMode(ItemStack stack) {
        for (int i = 0; i < ModItems.TUNED_STRINGS.size(); i++) {
            if (stack.is(ModItems.TUNED_STRINGS.get(i).get())) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (isMirror()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int mode = tunedMode(stack);
        if (mode > 0 && state.getValue(MODE) == 0) {
            if (level instanceof ServerLevel server) {
                level.setBlock(pos, state.setValue(MODE, mode), Block.UPDATE_ALL);
                BraneNetwork.get(server.getServer()).add(mode, GlobalPos.of(level.dimension(), pos));
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.6F + 0.2F * mode);
                player.displayClientMessage(Component.translatable("msg." + StringTheoryMod.MODID + ".attuned",
                        Component.translatable(stack.getDescriptionId())), true);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int mode = state.getValue(MODE);
        if (isMirror() || mode == 0) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            level.setBlock(pos, state.setValue(MODE, 0), Block.UPDATE_ALL);
            BraneNetwork.get(server.getServer()).remove(mode, GlobalPos.of(level.dimension(), pos));
            ItemStack string = new ItemStack(ModItems.TUNED_STRINGS.get(mode - 1).get());
            if (!player.getInventory().add(string)) {
                player.drop(string, false);
            }
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !isMirror()) {
            int mode = state.getValue(MODE);
            if (mode > 0 && level instanceof ServerLevel server) {
                BraneNetwork.get(server.getServer()).remove(mode, GlobalPos.of(level.dimension(), pos));
                popResource(level, pos, new ItemStack(ModItems.TUNED_STRINGS.get(mode - 1).get()));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, ModBlockEntities.BRANE_ANCHOR.get(), BraneAnchorBlockEntity::serverTick);
    }
}
