package io.github.screret.simpletech.multiblock.fissionreactor;

import io.github.screret.simpletech.multiblock.fissionreactor.block.ReactorFrameBlock;
import io.github.screret.simpletech.multiblock.logic.IMasterLogic;
import io.github.screret.simpletech.multiblock.logic.IServantLogic;
import io.github.screret.simpletech.multiblock.logic.ServantBlockEntity;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ReactorComponentBlockEntity extends ServantBlockEntity {

    public ReactorComponentBlockEntity(BlockPos pos, BlockState state) {
        this(ModRegistry.REACTOR_COMPONENT_BE.get(), pos, state);
    }

    protected ReactorComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
        // update the master
        super.setMaster(master, block);

        // update the active state
        if (level != null) {
            BlockState currentState = getBlockState();
            boolean hasMaster = getMasterPos() != null;
            if (currentState.hasProperty(ReactorFrameBlock.IN_STRUCTURE) && currentState.getValue(ReactorFrameBlock.IN_STRUCTURE) != hasMaster) {
                level.setBlock(worldPosition, getBlockState().setValue(ReactorFrameBlock.IN_STRUCTURE, hasMaster), Block.UPDATE_CLIENTS);
            }
        }
    }

    /**
     * Block method to update neighbors of a reactor component when a new one is placed
     * @param world  World instance
     * @param pos    Location of new smeltery component
     */
    public static void updateNeighbors(Level world, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            // if the neighbor is a master, notify it we exist
            BlockEntity tileEntity = world.getBlockEntity(pos.relative(direction));
            if (tileEntity instanceof IMasterLogic) {
                BlockEntity servant = world.getBlockEntity(pos);
                if (servant instanceof IServantLogic) {
                    ((IMasterLogic) tileEntity).notifyChange((IServantLogic) servant, pos, state);
                    break;
                }
                // if the neighbor is a servant, notify its master we exist
            } else if (tileEntity instanceof ReactorComponentBlockEntity component) {
                if (component.hasMaster()) {
                    component.notifyMasterOfChange(pos, state);
                    break;
                }
            }
        }
    }
}
