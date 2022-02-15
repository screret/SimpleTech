package io.github.screret.simpletech.blocks.enitites;

import io.github.screret.simpletech.energy.storage.CustomEnergyStorage;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BaseMachineBlockEntity extends BlockEntity {

    public static int MAX_CAPACITY; // Max capacity
    public static int MAX_GENERATE;    // Generation per tick
    public static int MAX_OUTPUT;       // Power to send out per tick

    // Never create lazy optionals in getCapability. Always place them as fields in the tile entity:
    private final ItemStackHandler itemHandler = createHandler();
    private final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);

    private final CustomEnergyStorage energyStorage = createEnergy();
    private final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    public BaseMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.BASE_MACHINE_BLOCK_ENTITY.get(), pos, state);
        MAX_CAPACITY = 50000;
        MAX_OUTPUT = 200;
        MAX_GENERATE = 60;
    }

    public BaseMachineBlockEntity(BlockPos pos, BlockState state, int maxCapacity, int maxTransfer, int maxGenerate) {
        super(ModRegistry.BASE_MACHINE_BLOCK_ENTITY.get(), pos, state);
        MAX_OUTPUT = maxTransfer;
        MAX_CAPACITY = maxCapacity;
        MAX_GENERATE = maxGenerate;
    }

    public void tick(){

    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        handler.invalidate();
        energy.invalidate();
    }

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(1) {

            @Override
            protected void onContentsChanged(int slot) {
                // To make sure the TE persists when the chunk is saved later we need to
                // mark it dirty every time the item handler changes
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
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
        return new CustomEnergyStorage(MAX_CAPACITY, MAX_OUTPUT) {
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

}
