package io.github.screret.simpletech.blocks.enitites;

import io.github.screret.simpletech.energy.storage.CustomEnergyStorage;
import io.github.screret.simpletech.recipes.power.BurnRecipe;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseMachineBlockEntity extends BlockEntity implements Nameable {

    public int maxCapacity;     // Max capacity
    public int maxGenerate;     // Generation per tick
    public int maxOutput;       // Power to send out per tick

    public static int BASE_MAX_CAPACITY = 500; // Max capacity
    public static int BASE_MAX_GENERATE = 200; // Generation per tick
    public static int BASE_MAX_OUTPUT = 6;     // Power to send out per tick

    public static int MODIFIER_GENERATE = 1;

    // Never create lazy optionals in getCapability. Always place them as fields in the tile entity:
    protected final ItemStackHandler itemHandler = createHandler(5);
    protected final LazyOptional<IItemHandlerModifiable> handler = LazyOptional.of(() -> itemHandler);

    protected final CustomEnergyStorage energyStorage = createEnergy();
    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    protected Component name;
    protected RecipeType<? extends Recipe<Container>> recipeType;

    public BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.maxCapacity = BASE_MAX_CAPACITY;
        this.maxOutput = BASE_MAX_OUTPUT;
        this.maxGenerate = BASE_MAX_GENERATE;
        recipeType = ModRegistry.BURN_RECIPE_TYPE;
    }

    public BaseMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BASE_MACHINE_BE.get(), pos, state);
        this.maxCapacity = BASE_MAX_CAPACITY;
        this.maxOutput = BASE_MAX_OUTPUT;
        this.maxGenerate = BASE_MAX_GENERATE;
        recipeType = ModRegistry.BURN_RECIPE_TYPE;
    }

    public BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCapacity, int maxTransfer, int maxGenerate) {
        super(type, pos, state);
        this.maxOutput = maxTransfer;
        this.maxCapacity = maxCapacity;
        this.maxGenerate = maxGenerate;
        recipeType = ModRegistry.BURN_RECIPE_TYPE;
    }

    protected int counter, enabledTime;

    // region CHANGE THESE IN SUBCLASSES
    public void tickServer() {
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
            setChanged();
        }

        if (counter <= 0) {
            if (recipe != null && recipe.getUsageTime() > 0) {
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
    // endregion

    @Override
    public void setRemoved() {
        super.setRemoved();
        handler.invalidate();
        energy.invalidate();
    }

    private ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size) {

            @Override
            protected void onContentsChanged(int slot) {
                // To make sure the TE persists when the chunk is saved later we need to
                // mark it dirty every time the item handler changes
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return true;
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) <= 0) {
                    return stack;
                }
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(maxCapacity, maxOutput) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return handler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : this.getDefaultName();
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    protected Component getDefaultName() {
        return new TranslatableComponent("block.simpletech.base_machine");
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Component getCustomName() {
        return Nameable.super.getCustomName();
    }
}
