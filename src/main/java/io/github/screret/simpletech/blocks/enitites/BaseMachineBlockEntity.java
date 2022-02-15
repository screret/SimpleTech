package io.github.screret.simpletech.blocks.enitites;

import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.EnergyStorage;

public class BaseMachineBlockEntity extends BlockEntity {

    public EnergyStorage energyStorage;
    protected int processMax, process;
    protected int processTick = 20;
    protected final int maxTransfer, maxCapacity;

    public BaseMachineBlockEntity(BlockPos pos, BlockState state, int maxCapacity, int maxTransfer) {
        super(ModRegistry.BASE_MACHINE_BLOCK_ENTITY.get(), pos, state);
        this.maxTransfer = maxTransfer;
        this.maxCapacity = maxCapacity;
        energyStorage = new EnergyStorage(maxCapacity, maxTransfer);
    }

    public void tick(){

    }

    // region PROCESS
    protected boolean canProcessStart() {

        if (energyStorage.getEnergyStored() - process < processTick) {
            return false;
        }
        if (!validateInputs()) {
            return false;
        }
        return validateOutputs();
    }

    protected boolean canProcessFinish() {

        return process <= 0;
    }

    protected void processStart() {

        processTick = baseProcessTick;
        int energy = curRecipe.getEnergy(this);
        energy += process;                  // Apply extra energy to next process
        process = processMax = energy;
        if (cacheRenderFluid()) {
            TileStatePacket.sendToClient(this);
        }
    }

    protected void processFinish() {

        if (!validateInputs()) {
            processOff();
            return;
        }
        resolveOutputs();
        resolveInputs();
        markDirtyFast();
    }

    protected void processOff() {

        process = 0;
        isActive = false;
        wasActive = true;
        clearRecipe();
        if (level != null) {
            timeTracker.markTime(level);
        }
    }

    protected int processTick() {

        if (process <= 0) {
            return 0;
        }
        energyStorage.modify(-processTick);
        process -= processTick;
        return processTick;
    }
    // endregion
    // region HELPERS
    protected void chargeEnergy() {

        if (!chargeSlot.isEmpty()) {
            chargeSlot.getItemStack()
                    .getCapability(ThermalEnergyHelper.getBaseEnergySystem(), null)
                    .ifPresent(c -> energyStorage.receiveEnergy(c.extractEnergy(Math.min(energyStorage.getMaxReceive(), energyStorage.getSpace()), false), false));
        }
    }

    protected boolean cacheRecipe() {

        return false;
    }

    protected void clearRecipe() {

        curRecipe = null;
        curCatalyst = null;
        itemInputCounts = new ArrayList<>();
        fluidInputCounts = new ArrayList<>();
    }

    protected boolean validateInputs() {

        if (!cacheRecipe()) {
            return false;
        }
        List<? extends ItemStorageCoFH> slotInputs = inputSlots();
        for (int i = 0; i < slotInputs.size() && i < itemInputCounts.size(); ++i) {
            int inputCount = itemInputCounts.get(i);
            if (inputCount > 0 && slotInputs.get(i).getItemStack().getCount() < inputCount) {
                return false;
            }
        }
        List<? extends FluidStorageCoFH> tankInputs = inputTanks();
        for (int i = 0; i < tankInputs.size() && i < fluidInputCounts.size(); ++i) {
            int inputCount = fluidInputCounts.get(i);
            FluidStack input = tankInputs.get(i).getFluidStack();
            if (inputCount > 0 && (input.isEmpty() || input.getAmount() < inputCount)) {
                return false;
            }
        }
        return true;
    }

    protected boolean validateOutputs() {

        if (curRecipe == null && !cacheRecipe()) {
            return false;
        }
        // ITEMS
        List<? extends ItemStorageCoFH> slotOutputs = outputSlots();
        List<ItemStack> recipeOutputItems = curRecipe.getOutputItems(this);
        boolean[] used = new boolean[outputSlots().size()];

        for (int j = 0; j < recipeOutputItems.size(); ++j) {
            ItemStack recipeOutput = recipeOutputItems.get(j);

            boolean matched = false;
            for (int i = 0; i < slotOutputs.size(); ++i) {
                if (used[i]) {
                    continue;
                }
                ItemStack output = slotOutputs.get(i).getItemStack();
                if (output.getCount() >= output.getMaxStackSize()) {
                    continue;
                }
                if (itemsEqualWithTags(output, recipeOutput)) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                for (int i = 0; i < slotOutputs.size(); ++i) {
                    if (used[i]) {
                        continue;
                    }
                    if (slotOutputs.get(i).isEmpty()) {
                        used[i] = true;
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched && (j == 0 || !secondaryNullFeature)) {
                return false;
            }
        }

        //        for (ItemStack recipeOutput : recipeOutputItems) {
        //            boolean matched = false;
        //            for (int i = 0; i < slotOutputs.size(); ++i) {
        //                if (used[i]) {
        //                    continue;
        //                }
        //                ItemStack output = slotOutputs.get(i).getItemStack();
        //                if (output.getCount() >= output.getMaxStackSize()) {
        //                    continue;
        //                }
        //                if (itemsEqualWithTags(output, recipeOutput)) {
        //                    used[i] = true;
        //                    matched = true;
        //                    break;
        //                }
        //            }
        //            if (!matched) {
        //                for (int i = 0; i < slotOutputs.size(); ++i) {
        //                    if (used[i]) {
        //                        continue;
        //                    }
        //                    if (slotOutputs.get(i).isEmpty()) {
        //                        used[i] = true;
        //                        matched = true;
        //                        break;
        //                    }
        //                }
        //            }
        //            if (!matched) {
        //                return false;
        //            }
        //        }

        // FLUIDS
        List<? extends FluidStorageCoFH> tankOutputs = outputTanks();
        List<FluidStack> recipeOutputFluids = curRecipe.getOutputFluids(this);
        used = new boolean[outputTanks().size()];
        for (FluidStack recipeOutput : recipeOutputFluids) {
            boolean matched = false;
            for (int i = 0; i < tankOutputs.size(); ++i) {
                FluidStack output = tankOutputs.get(i).getFluidStack();
                if (used[i] || tankOutputs.get(i).getSpace() < recipeOutput.getAmount()) {
                    continue;
                }
                if (fluidsEqual(output, recipeOutput)) {
                    used[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                for (int i = 0; i < tankOutputs.size(); ++i) {
                    if (used[i]) {
                        continue;
                    }
                    if (tankOutputs.get(i).isEmpty()) {
                        used[i] = true;
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    protected void resolveOutputs() {

        List<ItemStack> recipeOutputItems = curRecipe.getOutputItems(this);
        List<FluidStack> recipeOutputFluids = curRecipe.getOutputFluids(this);
        List<Float> recipeOutputChances = curRecipe.getOutputItemChances(this);

        // Output Items
        for (int i = 0; i < recipeOutputItems.size(); ++i) {
            ItemStack recipeOutput = recipeOutputItems.get(i);
            float chance = recipeOutputChances.get(i);
            int outputCount = chance <= BASE_CHANCE ? recipeOutput.getCount() : (int) chance;
            while (level.random.nextFloat() < chance) {
                boolean matched = false;
                for (ItemStorageCoFH slot : outputSlots()) {
                    ItemStack output = slot.getItemStack();
                    if (itemsEqualWithTags(output, recipeOutput) && output.getCount() < output.getMaxStackSize()) {
                        output.grow(outputCount);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    for (ItemStorageCoFH slot : outputSlots()) {
                        if (slot.isEmpty()) {
                            slot.setItemStack(cloneStack(recipeOutput, outputCount));
                            break;
                        }
                    }
                }
                chance -= BASE_CHANCE * outputCount;
                outputCount = 1;
            }
        }
        // Output Fluids
        for (FluidStack recipeOutput : recipeOutputFluids) {
            boolean matched = false;
            for (FluidStorageCoFH tank : outputTanks()) {
                FluidStack output = tank.getFluidStack();
                if (tank.getSpace() >= recipeOutput.getAmount() && fluidsEqual(output, recipeOutput)) {
                    output.setAmount(output.getAmount() + recipeOutput.getAmount());
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                for (FluidStorageCoFH tank : outputTanks()) {
                    if (tank.isEmpty()) {
                        tank.setFluidStack(recipeOutput.copy());
                        break;
                    }
                }
            }
        }
        // Xp
        xpStorage.receiveXPFloat(curRecipe.getXp(this), false);
    }

    protected void resolveInputs() {

        // Input Items
        for (int i = 0; i < itemInputCounts.size(); ++i) {
            inputSlots().get(i).consume(itemInputCounts.get(i));
        }
        // Input Fluids
        for (int i = 0; i < fluidInputCounts.size(); ++i) {
            inputTanks().get(i).modify(-fluidInputCounts.get(i));
        }
    }
    // endregion

}
