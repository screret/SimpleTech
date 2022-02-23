package io.github.screret.simpletech.multiblock.fissionreactor;

import io.github.screret.simpletech.capabilities.CapabilityEnergyContainer;
import io.github.screret.simpletech.energy.storage.CustomEnergyStorage;
import io.github.screret.simpletech.energy.storage.EmptyEnergyStorage;
import io.github.screret.simpletech.energy.storage.IEnergyContainer;
import io.github.screret.simpletech.multiblock.blockentity.tank.ITankHandler;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import slimeknights.mantle.inventory.EmptyItemHandler;
import slimeknights.mantle.util.WeakConsumerWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Shared logic between drains and ducts
 */
public abstract class ReactorInputOutputBlockEntity<T> extends ReactorComponentBlockEntity {
    /** Capability this TE watches */
    private final Capability<T> capability;
    /** Empty capability for in case the valid capability becomes invalid without invalidating */
    protected final T emptyInstance;
    /** Listener to attach to consumed capabilities */
    protected final NonNullConsumer<LazyOptional<T>> listener = new WeakConsumerWrapper<>(this, (te, cap) -> te.clearHandler());
    @Nullable
    private LazyOptional<T> capabilityHolder = null;

    protected ReactorInputOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Capability<T> capability, T emptyInstance) {
        super(type, pos, state);
        this.capability = capability;
        this.emptyInstance = emptyInstance;
    }

    /** Clears all cached capabilities */
    private void clearHandler() {
        if (capabilityHolder != null) {
            capabilityHolder.invalidate();
            capabilityHolder = null;
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        clearHandler();
    }

    @Override
    protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
        assert level != null;

        // if we have a new master, invalidate handlers
        boolean masterChanged = false;
        if (!Objects.equals(getMasterPos(), master)) {
            clearHandler();
            masterChanged = true;
        }
        super.setMaster(master, block);
        // notify neighbors of the change (state change skips the notify flag)
        if (masterChanged) {
            level.blockUpdated(worldPosition, getBlockState().getBlock());
        }
    }

    /**
     * Gets the capability to store in this IO block. Capability parent should have the proper listeners attached
     * @param parent  Parent tile entity
     * @return  Capability from parent, or empty if absent
     */
    protected LazyOptional<T> getCapability(BlockEntity parent) {
        LazyOptional<T> handler = parent.getCapability(capability);
        if (handler.isPresent()) {
            handler.addListener(listener);

            return LazyOptional.of(() -> handler.orElse(emptyInstance));
        }
        return LazyOptional.empty();
    }

    /**
     * Fetches the capability handlers if missing
     */
    private LazyOptional<T> getCachedCapability() {
        if (capabilityHolder == null) {
            if (validateMaster()) {
                BlockPos master = getMasterPos();
                if (master != null && this.level != null) {
                    BlockEntity te = level.getBlockEntity(master);
                    if (te != null) {
                        capabilityHolder = getCapability(te);
                        return capabilityHolder;
                    }
                }
            }
            capabilityHolder = LazyOptional.empty();
        }
        return capabilityHolder;
    }

    @Nonnull
    @Override
    public <C> LazyOptional<C> getCapability(Capability<C> capability, @Nullable Direction facing) {
        if (capability == this.capability) {
            return getCachedCapability().cast();
        }
        return super.getCapability(capability, facing);
    }

    /** Fluid implementation of smeltery IO */
    public static abstract class ReactorFluidIO extends ReactorInputOutputBlockEntity<IFluidHandler> {
        protected ReactorFluidIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EmptyFluidHandler.INSTANCE);
        }

        /** Wraps the given capability */
        protected LazyOptional<IFluidHandler> makeWrapper(LazyOptional<IFluidHandler> capability) {
            return LazyOptional.of(() -> capability.orElse(emptyInstance));
        }

        @Override
        protected LazyOptional<IFluidHandler> getCapability(BlockEntity parent) {
            // fluid capability is not exposed directly in the smeltery
            if (parent instanceof ITankHandler) {
                LazyOptional<IFluidHandler> capability = ((ITankHandler) parent).getFluidCapability();
                if (capability.isPresent()) {
                    capability.addListener(listener);
                    return makeWrapper(capability);
                }
            }
            return LazyOptional.empty();
        }
    }

    /** Power implementation of smeltery IO */
    public static abstract class ReactorEnergyIO extends ReactorInputOutputBlockEntity<IEnergyContainer> {
        protected ReactorEnergyIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state, CapabilityEnergyContainer.ENERGY, EmptyEnergyStorage.INSTANCE);
        }

        /** Wraps the given capability */
        protected LazyOptional<IEnergyContainer> makeWrapper(LazyOptional<IEnergyContainer> capability) {
            return LazyOptional.of(() -> capability.orElse(emptyInstance));
        }

        @Override
        protected LazyOptional<IEnergyContainer> getCapability(BlockEntity parent) {
            // energy capability is not exposed directly in the reactor
            if (parent instanceof IEnergyContainer energyContainer) {
                LazyOptional<IEnergyContainer> capability = energyContainer.getEnergyCapability();
                if (capability.isPresent()) {
                    capability.addListener(listener);
                    return makeWrapper(capability);
                }
            }
            return LazyOptional.empty();
        }
    }

    /** Item implementation of reactor IO */
    public static class ReactorChuteBlockEntity extends ReactorInputOutputBlockEntity<IItemHandler> {
        public ReactorChuteBlockEntity(BlockPos pos, BlockState state) {
            this(ModRegistry.REACTOR_CHUTE_BE.get(), pos, state);
        }

        protected ReactorChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EmptyItemHandler.INSTANCE);
        }
    }

}