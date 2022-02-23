package io.github.screret.simpletech.multiblock.fissionreactor;

import io.github.screret.simpletech.energy.storage.CustomEnergyStorage;
import io.github.screret.simpletech.multiblock.blockentity.tank.IDisplayFluidListener;
import io.github.screret.simpletech.multiblock.blockentity.tank.ITankHandler;
import io.github.screret.simpletech.registry.ModRegistry;
import io.github.screret.simpletech.registry.PacketManager;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.client.model.data.SinglePropertyData;
import slimeknights.mantle.util.BlockEntityHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public class ReactorEnergyIOBlockEntity extends ReactorInputOutputBlockEntity.ReactorEnergyIO {
    @Getter
    private final IModelData modelData = new SinglePropertyData<>(IDisplayFluidListener.PROPERTY);
    @Getter
    private FluidStack displayFluid = FluidStack.EMPTY;

    public ReactorEnergyIOBlockEntity(BlockPos pos, BlockState state) {
        this(ModRegistry.REACTOR_POWER_IO_BE.get(), pos, state);
    }

    protected ReactorEnergyIOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /* Updating */

    /** Attaches this TE to the master as a display fluid listener */
    private void attachEnergyListener() {
        BlockPos masterPos = getMasterPos();
        if (masterPos != null && level != null && level.isClientSide) {
            //BlockEntityHelper.get(CustomEnergyStorage.class, level, masterPos).ifPresent(te -> te.addDisplayListener(this));
        }
    }

    // override instead of writeSynced to avoid writing master to the main tag twice
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        writeMaster(nbt);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        BlockPos oldMaster = getMasterPos();
        super.handleUpdateTag(tag);
        if (!Objects.equals(oldMaster, getMasterPos())) {
            attachEnergyListener();
        }
    }

    @Override
    protected boolean shouldSyncOnUpdate() {
        return true;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return PacketManager.createBEPacket(this, be -> be.writeMaster(new CompoundTag()));
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            BlockPos oldMaster = getMasterPos();
            readMaster(tag);
            if (!Objects.equals(oldMaster, getMasterPos())) {
                attachEnergyListener();
            }
        }
    }
}
