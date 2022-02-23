package io.github.screret.simpletech.multiblock.blockentity.tank;

import com.google.common.collect.Lists;
import io.github.screret.simpletech.packets.TankUpdatePacket;
import io.github.screret.simpletech.registry.PacketManager;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import slimeknights.mantle.block.entity.MantleBlockEntity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.ListIterator;

/**
 * Fluid handler implementation for the smeltery
 */
public class Tank<T extends MantleBlockEntity & ITankHandler> implements IFluidHandler {
    private final T parent;
    /** Fluids actually contained in the tank */
    @Getter
    private final List<FluidStack> fluids;
    /** Maximum capacity of the smeltery */
    private int capacity;
    /** Current amount of fluid in the tank */
    @Getter
    private int contained;

    public Tank(T parent) {
        fluids = Lists.newArrayList();
        capacity = 0;
        contained = 0;
        this.parent = parent;
    }

    /**
     * Called when the fluids change to sync to client
     */
    public void syncFluids() {
        Level world = parent.getLevel();
        if (world != null && !world.isClientSide) {
            BlockPos pos = parent.getBlockPos();
            PacketManager.getInstance().sendToClientsAround(new TankUpdatePacket(pos, fluids), world, pos);
        }
    }


    /* Capacity and space */

    /**
     * Updates the maximum tank capacity
     * @param maxCapacity  New max capacity
     */
    public void setCapacity(int maxCapacity) {
        this.capacity = maxCapacity;
    }

    /**
     * Gets the maximum amount of space in the smeltery tank
     * @return  Tank capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Gets the amount of empty space in the tank
     * @return  Remaining space in the tank
     */
    public int getRemainingSpace() {
        if (contained >= capacity) {
            return 0;
        }
        return capacity - contained;
    }


    /* Fluids */

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }

    @Override
    public int getTanks() {
        if (contained < capacity) {
            return fluids.size() + 1;
        }
        return fluids.size();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank < 0 || tank >= fluids.size()) {
            return FluidStack.EMPTY;
        }
        return fluids.get(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank < 0) {
            return 0;
        }
        // index of the tank size means the "empty" segment
        int remaining = capacity - contained;
        if (tank == fluids.size()) {
            return remaining;
        }
        // any valid index, return the amount contained and the extra space
        return fluids.get(tank).getAmount() + remaining;
    }

    /**
     * Moves the fluid with the passed index to the beginning/bottom of the fluid tank stack
     * @param index  Index to move
     */
    public void moveFluidToBottom(int index) {
        if (index < fluids.size()) {
            FluidStack fluid = fluids.get(index);
            fluids.remove(index);
            fluids.add(0, fluid);
            parent.notifyFluidsChanged(ITankHandler.FluidChange.CHANGED, FluidStack.EMPTY);
        }
    }


    /* Filling and draining */

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // if full or nothing being filled, do nothing
        if (contained >= capacity || resource.isEmpty()) {
            return 0;
        }

        // determine how much we can fill
        int usable = Math.min(capacity - contained, resource.getAmount());
        // could be negative if the smeltery size changes then you try filling it
        if (usable <= 0) {
            return 0;
        }

        // done here if just simulating
        if (action.simulate()) {
            return usable;
        }

        // add contained fluid amount
        contained += usable;

        // check if we already have the given liquid
        for (FluidStack fluid : fluids) {
            if (fluid.isFluidEqual(resource)) {
                // yup. add it
                fluid.grow(usable);
                parent.notifyFluidsChanged(ITankHandler.FluidChange.CHANGED, fluid);
                return usable;
            }
        }

        // not present yet, add it
        resource = resource.copy();
        resource.setAmount(usable);
        fluids.add(resource);
        parent.notifyFluidsChanged(ITankHandler.FluidChange.ADDED, resource);
        return usable;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (fluids.isEmpty()) {
            return FluidStack.EMPTY;
        }

        // simply drain the first one
        FluidStack fluid = fluids.get(0);
        int drainable = Math.min(maxDrain, fluid.getAmount());

        // copy contained fluid to return for accuracy
        FluidStack ret = fluid.copy();
        ret.setAmount(drainable);

        // remove the fluid from the tank
        if (action.execute()) {
            fluid.shrink(drainable);
            contained -= drainable;
            // if now empty, remove from the list
            if (fluid.getAmount() <= 0) {
                fluids.remove(fluid);
                parent.notifyFluidsChanged(ITankHandler.FluidChange.REMOVED, fluid);
            } else {
                parent.notifyFluidsChanged(ITankHandler.FluidChange.CHANGED, fluid);
            }
        }

        // return drained fluid
        return ret;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack toDrain, FluidAction action) {
        // search for the resource
        ListIterator<FluidStack> iter = fluids.listIterator();
        while (iter.hasNext()) {
            FluidStack fluid = iter.next();
            if (fluid.isFluidEqual(toDrain)) {
                // if found, determine how much we can drain
                int drainable = Math.min(toDrain.getAmount(), fluid.getAmount());

                // copy contained fluid to return for accuracy
                FluidStack ret = fluid.copy();
                ret.setAmount(drainable);

                // update tank if executing
                if (action.execute()) {
                    fluid.shrink(drainable);
                    contained -= drainable;
                    // if now empty, remove from the list
                    if (fluid.getAmount() <= 0) {
                        iter.remove();
                        parent.notifyFluidsChanged(ITankHandler.FluidChange.REMOVED, fluid);
                    } else {
                        parent.notifyFluidsChanged(ITankHandler.FluidChange.CHANGED, fluid);
                    }
                }

                return ret;
            }
        }

        // nothing drained
        return FluidStack.EMPTY;
    }

    /* Saving and loading */

    private static final String TAG_FLUIDS = "fluids";
    private static final String TAG_CAPACITY = "capacity";

    /**
     * Updates fluids in the tank, typically from a packet
     * @param fluids  List of fluids
     */
    public void setFluids(List<FluidStack> fluids) {
        FluidStack oldFirst = getFluidInTank(0);
        this.fluids.clear();
        this.fluids.addAll(fluids);
        contained = fluids.stream().mapToInt(FluidStack::getAmount).reduce(0, Integer::sum);
        FluidStack newFirst = getFluidInTank(0);
        if (!oldFirst.isFluidEqual(newFirst)) {
            parent.notifyFluidsChanged(ITankHandler.FluidChange.ORDER_CHANGED, newFirst);
        }
    }

    /** Writes the tank to NBT */
    public CompoundTag write(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (FluidStack liquid : fluids) {
            CompoundTag fluidTag = new CompoundTag();
            liquid.writeToNBT(fluidTag);
            list.add(fluidTag);
        }
        nbt.put(TAG_FLUIDS, list);
        nbt.putInt(TAG_CAPACITY, capacity);
        return nbt;
    }

    /** Reads the tank from NBT */
    public void read(CompoundTag tag) {
        ListTag list = tag.getList(TAG_FLUIDS, Tag.TAG_COMPOUND);
        fluids.clear();
        contained = 0;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag fluidTag = list.getCompound(i);
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (!fluid.isEmpty()) {
                fluids.add(fluid);
                contained += fluid.getAmount();
            }
        }
        capacity = tag.getInt(TAG_CAPACITY);
    }
}