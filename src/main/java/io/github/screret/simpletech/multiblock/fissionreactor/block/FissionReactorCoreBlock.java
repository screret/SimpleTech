package io.github.screret.simpletech.multiblock.fissionreactor.block;

import io.github.screret.simpletech.multiblock.block.CoreControllerBlock;
import io.github.screret.simpletech.multiblock.fissionreactor.FissionReactorCoreBlockEntity;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.util.BlockEntityHelper;

import java.util.Random;

public class FissionReactorCoreBlock extends CoreControllerBlock {

    public FissionReactorCoreBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FissionReactorCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> type) {
        return FissionReactorCoreBlockEntity.getTicker(pLevel, type, ModRegistry.FISSION_REACTOR_CORE_BE.get());
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // check structure
        BlockEntityHelper.get(FissionReactorCoreBlockEntity.class, worldIn, pos).ifPresent(FissionReactorCoreBlockEntity::updateStructure);
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!newState.is(this)) {
            BlockEntityHelper.get(FissionReactorCoreBlockEntity.class, worldIn, pos).ifPresent(FissionReactorCoreBlockEntity::invalidateStructure);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
        if (state.getValue(ACTIVE)) {
            double x = pos.getX() + 0.5D;
            double y = (double) pos.getY() + (rand.nextFloat() * 6F + 2F) / 16F;
            double z = pos.getZ() + 0.5D;
            double frontOffset = 0.52D;
            double sideOffset = rand.nextDouble() * 0.6D - 0.3D;
            spawnFireParticles(world, state, x, y, z, frontOffset, sideOffset);
        }
    }

    /* No rotation if in a structure  */

    @Deprecated
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (state.getValue(IN_STRUCTURE)) {
            return state;
        }
        return super.rotate(state, rotation);
    }

    @Deprecated
    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        if (state.getValue(IN_STRUCTURE)) {
            return state;
        }
        return super.mirror(state, mirror);
    }
}
