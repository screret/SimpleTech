package io.github.screret.simpletech.multiblock.blockentity;

import io.github.screret.simpletech.multiblock.fissionreactor.FissionReactorCoreBlockEntity;
import io.github.screret.simpletech.registry.ModTags;
import net.minecraft.world.level.block.Block;

public class ReactorMultiblock extends StructureMultiblock<FissionReactorCoreBlockEntity> {
    public ReactorMultiblock(FissionReactorCoreBlockEntity smeltery) {
        super(smeltery, true, true, true);
    }

    @Override
    protected boolean isValidBlock(Block block) {
        return ModTags.Blocks.REACTOR.contains(block);
    }

    @Override
    protected boolean isValidFloor(Block block) {
        return ModTags.Blocks.REACTOR_FLOOR.contains(block);
    }

    @Override
    protected boolean isValidTank(Block block) {
        return ModTags.Blocks.REACTOR_TANKS.contains(block);
    }

    @Override
    protected boolean isValidWall(Block block) {
        return ModTags.Blocks.REACTOR_WALL.contains(block);
    }

}
