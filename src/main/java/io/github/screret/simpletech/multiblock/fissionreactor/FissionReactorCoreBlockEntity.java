package io.github.screret.simpletech.multiblock.fissionreactor;

import io.github.screret.simpletech.container.powergen.FissionReactorContainer;
import io.github.screret.simpletech.energy.storage.CustomEnergyStorage;
import io.github.screret.simpletech.multiblock.block.ControllerBlock;
import io.github.screret.simpletech.multiblock.blockentity.tank.Tank;
import io.github.screret.simpletech.multiblock.fissionreactor.block.ReactorFrameBlock;
import io.github.screret.simpletech.multiblock.blockentity.MultiblockResult;
import io.github.screret.simpletech.multiblock.blockentity.ReactorMultiblock;
import io.github.screret.simpletech.multiblock.blockentity.StructureBlockEntity;
import io.github.screret.simpletech.multiblock.blockentity.StructureMultiblock;
import io.github.screret.simpletech.multiblock.logic.IServantLogic;
import io.github.screret.simpletech.packets.StructureUpdatePacket;
import io.github.screret.simpletech.registry.PacketManager;
import io.github.screret.simpletech.packets.StructureErrorPositionPacket;
import io.github.screret.simpletech.registry.ModRegistry;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class FissionReactorCoreBlockEntity extends StructureBlockEntity {
    /** Fluid capacity per internal block TODO: adjust size */
    private static final int CAPACITY_PER_BLOCK = 90 * 12;
    /** Number of wall blocks needed to increase the fuel cost by 1 */
    private static final int BLOCKS_PER_FUEL = 15;

    private final StructureMultiblock<?> multiblock = createMultiblock();

    protected final CustomEnergyStorage energyStorage = createEnergy();
    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    protected Component name;
    public int maxCapacity;
    public int maxOutput;
    public int maxGenerate;

    /** Position of the block causing the structure to not form */
    @Nullable
    private BlockPos errorPos;
    /** Number of ticks the error will remain visible for */
    private int errorVisibleFor = 0;
    /** Temporary hack until forge fixes {@link #onLoad()}, do a first tick listener here as drains don't tick */
    private boolean addedDrainListeners = false;

    /* Saved data, written to Tag */
    /** Current structure contents */
    @Nullable 
    protected StructureMultiblock.StructureData structure;
    /** Tank instance for this smeltery */
    @Getter
    protected final Tank<StructureBlockEntity> tank = new Tank<>(this);

    /** Inventory handling melting items */
    
    protected final ItemStackHandler inventory = createInventory();

    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> inventory);

    /* Instance data, this data is not written to Tag */
    /** Timer to allow delaying actions based on number of ticks alive */
    protected int tick = 0;

    public FissionReactorCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.FISSION_REACTOR_CORE_BE.get(), pos, state, new TranslatableComponent("block.simpletech.fission_reactor_core"));
        this.maxCapacity = 1000000;
        this.maxOutput = 10000;
        this.maxGenerate = 10000;
    }

    public FissionReactorCoreBlockEntity(BlockPos pos, BlockState state, int maxCapacity, int maxOutput, int maxGenerate) {
        super(ModRegistry.FISSION_REACTOR_CORE_BE.get(), pos, state, new TranslatableComponent("block.simpletech.fission_reactor_core"));
        this.maxCapacity = maxCapacity;
        this.maxOutput = maxOutput;
        this.maxGenerate = maxGenerate;
    }

    @Override
    protected StructureMultiblock<?> createMultiblock() {
        return new ReactorMultiblock(this);
    }

    protected ItemStackHandler createInventory() {
        return new ItemStackHandler(5);
    }


    protected void heat() {
        if (structure == null || level == null) {
            return;
        }

        // the next set of behaviors all require fuel, skip if no tanks
        if (structure.hasTanks()) {
            // every second, interact with entities, will consume fuel if needed
            boolean entityMelted = false;
            // run in four phases alternating each tick, so each thing runs once every 4 ticks
            switch (tick % 4) {
                // first tick, find fuel if needed
                case 0:
                    if (!fuelModule.hasFuel()) {
                        // if we melted something already, we need fuel
                        if (entityMelted) {
                            fuelModule.findFuel(true);
                        } else {
                            // both alloying and melting need to know the temperature
                            int possibleTemp = fuelModule.findFuel(false);
                            /*alloyTank.setTemperature(possibleTemp);
                            if (meltingInventory.canHeat(possibleTemp) || alloyingModule.canAlloy()) {
                                fuelModule.findFuel(true);
                            }*/
                        }
                    }
                    break;
                // second tick: melt items
                case 1:
                    /*if (fuelModule.hasFuel()) {
                        inventory.heatItems(fuelModule.getTemperature());
                    } else {
                        inventory.coolItems();
                    }*/
                    break;
                // third tick: alloy alloys
                case 2:
                    if (fuelModule.hasFuel()) {
                        //alloyTank.setTemperature(fuelModule.getTemperature());
                        //alloyingModule.doAlloy();
                    }
                    break;
                // fourth tick: consume fuel, update fluids
                case 3: {
                    // update the active state
                    boolean hasFuel = fuelModule.hasFuel();
                    BlockState state = getBlockState();
                    if (state.getValue(ControllerBlock.ACTIVE) != hasFuel) {
                        level.setBlockAndUpdate(worldPosition, state.setValue(ControllerBlock.ACTIVE, hasFuel));
                    }
                    fuelModule.decreaseFuel(fuelRate);
                    break;
                }
            }
        }
    }

    @Override
    protected void setStructure(@Nullable StructureMultiblock.StructureData structure) {
        super.setStructure(structure);
        if (structure != null) {
            int dx = structure.getInnerX(), dy = structure.getInnerY(), dz = structure.getInnerZ();
            int size = dx * dy * dz;
            tank.setCapacity(CAPACITY_PER_BLOCK * size);
            for (int i = 0; i < inventory.getSlots(); ++i){
                dropItem(inventory.getStackInSlot(i));
            }
            inventory.setSize(size);
            // fuel rate: every 15 blocks in the wall makes the fuel cost 1 more
            // perimeter: 2 of the X and the Z wall, one of the floor
            fuelRate = 1 + ((2 * (dx * dy) + 2 * (dy * dz) + (dx * dz))) / BLOCKS_PER_FUEL;
        }
    }

    private void updateErrorPos() {
        BlockPos oldErrorPos = this.errorPos;
        this.errorPos = multiblock.getLastResult().getPos();
        if (!Objects.equals(oldErrorPos, errorPos)) {
            PacketManager.getInstance().sendToClientsAround(new StructureErrorPositionPacket(worldPosition, errorPos), level, worldPosition);
        }
    }


    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(maxCapacity, maxOutput) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
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

    /* Structure */

    /**
     * Attempts to locate a valid smeltery structure
     */
    protected void checkStructure() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean wasFormed = getBlockState().getValue(ReactorFrameBlock.IN_STRUCTURE);
        StructureMultiblock.StructureData oldStructure = structure;
        StructureMultiblock.StructureData newStructure = multiblock.detectMultiblock(level, worldPosition, getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));

        // update block state
        boolean formed = newStructure != null;
        if (formed != wasFormed) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ReactorFrameBlock.IN_STRUCTURE, formed));
        }

        // structure info updates
        if (formed) {
            // sync size to the client
            PacketManager.getInstance().sendToClientsAround(
                    new StructureUpdatePacket(worldPosition, newStructure.getMinPos(), newStructure.getMaxPos(), newStructure.getTanks()), level, worldPosition);

            // set master positions
            newStructure.assignMaster(this, oldStructure);
            setStructure(newStructure);
        } else {

            // clear positions
            if (oldStructure != null) {
                oldStructure.clearMaster(this);
            }
            setStructure(null);
        }

        // update the error position, we do on both success and failure for the sake of expanding positions
        updateErrorPos();

        // clear expand counter either way
    }

    /**
     * Called when the controller is broken to invalidate the master in all servants
     */
    public void invalidateStructure() {
        if (structure != null) {
            structure.clearMaster(this);
            structure = null;
            errorPos = null;
        }
    }

    @Override
    public void notifyChange(IServantLogic servant, BlockPos pos, BlockState state) {
        // structure invalid? can ignore this, will automatically check later
        if (structure == null) {
            return;
        }

        assert level != null;
        if (multiblock.shouldUpdate(level, structure, pos, state)) {
            updateStructure();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new FissionReactorContainer(id, this.getBlockPos(), inv, player);
    }

    @Override
    protected boolean isDebugItem(ItemStack stack) {
        return false;
    }
}
