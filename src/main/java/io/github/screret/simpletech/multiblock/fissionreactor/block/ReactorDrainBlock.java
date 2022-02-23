package io.github.screret.simpletech.multiblock.fissionreactor.block;

import io.github.screret.simpletech.multiblock.fissionreactor.DrainBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Extenson to include interaction behavior */
public class ReactorDrainBlock extends OrientableReactorBlock {
    public ReactorDrainBlock(Properties properties) {
        super(properties, DrainBlockEntity::new);
    }

    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        /*if (FluidTransferUtil.interactWithFluidItem(world, pos, player, hand, hit)) {
            return InteractionResult.SUCCESS;
        } else if (FluidTransferUtil.interactWithBucket(world, pos, player, hand, hit.getDirection(), state.getValue(FACING).getOpposite())) {
            return InteractionResult.SUCCESS;
        }*/
        return InteractionResult.PASS;
    }
}