package io.github.screret.simpletech.multiblock.block;

import io.github.screret.simpletech.multiblock.blockentity.MultiblockResult;
import io.github.screret.simpletech.multiblock.blockentity.StructureBlockEntity;
import io.github.screret.simpletech.multiblock.fissionreactor.FissionReactorCoreBlockEntity;
import io.github.screret.simpletech.registry.PacketManager;
import io.github.screret.simpletech.packets.StructureErrorPositionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.util.BlockEntityHelper;

/**
 * Multiblock that displays the error from the tile entity on right click
 */
public abstract class CoreControllerBlock extends ControllerBlock {
    protected CoreControllerBlock(Properties builder) {
        super(builder);
    }

    @Override
    protected boolean openGui(Player player, Level world, BlockPos pos) {
        super.openGui(player, world, pos);
        // only need to update if holding the proper items
        if (!world.isClientSide) {
            BlockEntityHelper.get(FissionReactorCoreBlockEntity.class, world, pos).ifPresent(te -> {
                MultiblockResult result = te.getStructureResult();

                if (!result.isSuccess() && te.showDebugBlockBorder(player)) {
                    PacketManager.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), player);
                }
            });
        }
        return true;
    }

    @Override
    protected boolean displayStatus(Player player, Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide) {
            BlockEntityHelper.get(StructureBlockEntity.class, world, pos).ifPresent(te -> {
                MultiblockResult result = te.getStructureResult();
                if (!result.isSuccess()) {
                    player.displayClientMessage(result.getMessage(), true);
                    PacketManager.getInstance().sendTo(new StructureErrorPositionPacket(pos, result.getPos()), player);
                }
            });
        }
        return true;
    }
}