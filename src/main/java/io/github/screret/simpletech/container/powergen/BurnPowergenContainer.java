package io.github.screret.simpletech.container.powergen;

import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.blocks.powergen.entities.BurnPowergenBlockEntity;
import io.github.screret.simpletech.container.BaseContainer;
import io.github.screret.simpletech.energy.storage.CustomEnergyStorage;
import io.github.screret.simpletech.recipes.power.BurnRecipe;
import io.github.screret.simpletech.recipes.power.DecompositionRecipe;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class BurnPowergenContainer extends BaseContainer {

    protected final BurnPowergenBlockEntity blockEntity;
    protected final Player playerEntity;

    public BurnPowergenContainer(int windowId, BlockPos pos, Inventory playerInventory, Player player) {
        super(ModRegistry.BURN_GENERATOR_CONTAINER.get(), windowId, new InvWrapper(playerInventory));
        if(player.getCommandSenderWorld().getBlockEntity(pos) instanceof BurnPowergenBlockEntity){
            blockEntity = (BurnPowergenBlockEntity) player.getCommandSenderWorld().getBlockEntity(pos);
        }else{
            throw new IllegalStateException("blockentity at pos " + pos + " is not the correct type");
        }
        this.playerEntity = player;

        if (blockEntity != null) {
            blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                addSlot(new SlotItemHandler(h, 0, 44, 36));
                addSlot(new SlotItemHandler(h, 1, 96, 30));
                addSlot(new SlotItemHandler(h, 2, 114, 30));
                addSlot(new SlotItemHandler(h, 3, 96, 48));
                addSlot(new SlotItemHandler(h, 4, 114, 48));
            });
        }
        layoutPlayerInventorySlots(8, 104);
        trackPower();
    }

    // Setup syncing of power from server to client so that the GUI can show the amount of power in the block
    private void trackPower() {
        // Unfortunatelly on a dedicated server ints are actually truncated to short so we need
        // to split our integer here (split our 32 bit integer into two 16 bit integers)
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return getEnergy() & 0xffff;
            }

            @Override
            public void set(int value) {
                blockEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
                    int energyStored = h.getEnergyStored() & 0xffff0000;
                    ((CustomEnergyStorage)h).setEnergy(energyStored + (value & 0xffff));
                });
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (getEnergy() >> 16) & 0xffff;
            }

            @Override
            public void set(int value) {
                blockEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
                    int energyStored = h.getEnergyStored() & 0x0000ffff;
                    ((CustomEnergyStorage)h).setEnergy(energyStored | (value << 16));
                });
            }
        });
    }

    public boolean isOn(){
        return this.blockEntity.getProcessingTime() > 0;
    }

    public int getProgress() {
        int i = this.blockEntity.getProcessingTime();
        if (i == 0) {
            i = 200;
        }
        return this.getEnergy() / this.blockEntity.maxCapacity;
    }

    public int getEnergy() {
        return blockEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return blockEntity.hasLevel() && stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), playerIn, ModRegistry.BURN_GENERATOR_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, itemstack);
            } else {
                BurnRecipe recipe = playerIn.getCommandSenderWorld().getRecipeManager().getRecipeFor(ModRegistry.BURN_RECIPE_TYPE, new SimpleContainer(itemstack), playerIn.getCommandSenderWorld()).orElse(null);
                if (recipe != null && recipe.getUsageTime() > 0) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.moveItemStackTo(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !this.moveItemStackTo(stack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stack);
        }

        return itemstack;
    }
}
