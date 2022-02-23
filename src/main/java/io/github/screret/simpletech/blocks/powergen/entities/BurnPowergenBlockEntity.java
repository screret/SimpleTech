package io.github.screret.simpletech.blocks.powergen.entities;

import io.github.screret.simpletech.blocks.enitites.BaseMachineBlockEntity;
import io.github.screret.simpletech.recipes.power.BurnRecipe;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.concurrent.atomic.AtomicInteger;

public class BurnPowergenBlockEntity extends BaseMachineBlockEntity {

    public BurnPowergenBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BURN_GENERATOR_BE.get(), pos, state);
        recipeType = ModRegistry.BURN_RECIPE_TYPE;
    }

    public BurnPowergenBlockEntity(BlockPos pos, BlockState state, int maxCapacity, int maxTransfer, int maxGenerate) {
        super(ModRegistry.BURN_GENERATOR_BE.get(), pos, state, maxCapacity, maxTransfer, maxGenerate);
        recipeType = ModRegistry.BURN_RECIPE_TYPE;
    }

    // region CHANGE THESE IN SUBCLASSES
    public void tickServer() {
        ItemStack stack = itemHandler.getStackInSlot(0);
        BurnRecipe recipe = (BurnRecipe) level.getRecipeManager().getRecipeFor(recipeType, new SimpleContainer(stack), level).orElse(null);
        if(recipe == null){
            counter = 0;
            enabledTime = 0;
        }
        if (counter > 0) {
            if(energyStorage.getEnergyStored() < maxCapacity){
                energyStorage.addEnergy(maxGenerate * MODIFIER_GENERATE);
            }
            counter--;
            enabledTime++;
            setChanged();
        }

        if (counter <= 0) {
            if (recipe != null && recipe.getUsageTime() > 0 && energyStorage.getEnergyStored() < maxCapacity) {
                itemHandler.extractItem(0, 1, false);
                counter = recipe.getUsageTime();
                enabledTime = 0;
                setChanged();
            }
        }


        BlockState blockState = level.getBlockState(worldPosition);
        if (blockState.getValue(BlockStateProperties.POWERED) != counter > 0) {
            level.setBlock(worldPosition, blockState.setValue(BlockStateProperties.POWERED, counter > 0),
                    Block.UPDATE_ALL);
        }

        sendOutPower();
    }

    public void tickClient(){
        ItemStack stack = itemHandler.getStackInSlot(0);
        BurnRecipe recipe = (BurnRecipe) level.getRecipeManager().getRecipeFor(recipeType, new SimpleContainer(stack), level).orElse(null);
        if(recipe == null){
            counter = 0;
            enabledTime = 0;
        }
        if (counter > 0) {
            energyStorage.addEnergy(maxGenerate * MODIFIER_GENERATE);
            counter--;
            enabledTime++;
        }

        if (counter <= 0) {
            if (recipe != null && recipe.getUsageTime() > 0) {
                counter = recipe.getUsageTime();
                enabledTime = 0;
            }
        }

    }

    public int getProcessingTime(){
        return counter;
    }
    public int getEnabledTime(){
        return enabledTime;
    }

    private void sendOutPower() {
        AtomicInteger capacity = new AtomicInteger(energyStorage.getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), maxOutput), false);
                                    capacity.addAndGet(-received);
                                    energyStorage.consumeEnergy(received);
                                    setChanged();
                                    return capacity.get() > 0;
                                } else {
                                    return true;
                                }
                            }
                    ).orElse(true);
                    if (!doContinue) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        if (tag.contains("Info")) {
            counter = tag.getCompound("Info").getInt("Counter");
            maxCapacity = tag.getCompound("Info").getInt("MaxCapacity");
            maxGenerate = tag.getCompound("Info").getInt("MaxGenerate");
            maxOutput = tag.getCompound("Info").getInt("MaxOutput");
        }
        if(tag.contains("CustomName")){
            name = Component.Serializer.fromJson(tag.getString("CustomName"));
        }

        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.put("Energy", energyStorage.serializeNBT());
        if(this.name != null){
            tag.putString("CustomName", Component.Serializer.toJson(this.name));
        }

        CompoundTag infoTag = new CompoundTag();
        infoTag.putInt("Counter", counter);
        infoTag.putInt("MaxCapacity", maxCapacity);
        infoTag.putInt("MaxGenerate", maxGenerate);
        infoTag.putInt("MaxOutput", maxOutput);
        tag.put("Info", infoTag);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("block.simpletech.burn_generator");
    }
    // endregion
}
