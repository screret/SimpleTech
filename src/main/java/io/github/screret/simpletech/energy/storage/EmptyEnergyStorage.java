package io.github.screret.simpletech.energy.storage;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

public class EmptyEnergyStorage implements IEnergyContainer {

    public static final EmptyEnergyStorage INSTANCE = new EmptyEnergyStorage();

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    @Override
    public void setEnergy(int energy) {

    }

    @Override
    public void addEnergy(int energy) {

    }

    @Override
    public void consumeEnergy(int energy) {

    }

    @Override
    public LazyOptional<IEnergyContainer> getEnergyCapability() {
        return null;
    }
}
