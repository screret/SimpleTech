package io.github.screret.simpletech.blocks.powergen;

import io.github.screret.simpletech.blocks.BaseMachineBlock;
import io.github.screret.simpletech.blocks.powergen.entities.BurnPowergenBlockEntity;
import io.github.screret.simpletech.container.powergen.BurnPowergenContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BurnPowergenBlock extends BaseMachineBlock {

    public BurnPowergenBlock(Properties properties, int maxCapacity, int maxTransfer, int maxGenerate) {
        super(properties, maxCapacity, maxTransfer, maxGenerate);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BurnPowergenBlockEntity blockEntity) {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return blockEntity.getDisplayName();
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new BurnPowergenContainer(windowId, pos, playerInventory, playerEntity);
                    }
                };
                NetworkHooks.openGui((ServerPlayer) player, containerProvider, be.getBlockPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BurnPowergenBlockEntity(pos, state, this.maxCapacity, this.maxTransfer, this.maxGenerate);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof BurnPowergenBlockEntity tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t) -> {
            if (t instanceof BurnPowergenBlockEntity tile) {
                tile.tickServer();
            }
        };
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof BurnPowergenBlockEntity baseMachineBlockEntity) {
            if (!level.isClientSide && !(new RecipeWrapper((IItemHandlerModifiable) baseMachineBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().get()).isEmpty())) {
                ItemStack itemstack = new ItemStack(this);
                blockentity.saveToItem(itemstack);
                if (baseMachineBlockEntity.hasCustomName()) {
                    itemstack.setHoverName(baseMachineBlockEntity.getCustomName());
                }

                ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder lootContextBuilder) {
        BlockEntity blockentity = lootContextBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockentity instanceof BurnPowergenBlockEntity baseMachineBlockEntity) {
            ItemStackHandler handler = (ItemStackHandler) baseMachineBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().get();
            lootContextBuilder = lootContextBuilder.withDynamicDrop(CONTENTS, (p_56218_, p_56219_) -> {
                for(int i = 0; i < handler.getSlots(); ++i) {
                    p_56219_.accept(handler.getStackInSlot(i));
                }

            });
        }

        return super.getDrops(state, lootContextBuilder);
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof BurnPowergenBlockEntity baseMachineBlockEntity) {
                baseMachineBlockEntity.setCustomName(stack.getHoverName());
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
