package io.github.screret.simpletech.blocks;

import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.blocks.enitites.BaseMachineBlockEntity;
import io.github.screret.simpletech.container.BaseMachineContainer;
import io.github.screret.simpletech.registry.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

public class BaseMachineBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final ResourceLocation CONTENTS = new ResourceLocation(SimpleTech.MOD_ID, "contents");
    public static final String MESSAGE_MAX_GEN = "message.simpletech.max_generate";
    public static final String MESSAGE_MAX_CAP = "message.simpletech.max_capacity";
    public static final String MESSAGE_MAX_OUT = "message.simpletech.max_output";
    protected final int maxCapacity, maxTransfer, maxGenerate;

    public BaseMachineBlock(Properties properties, int maxCapacity, int maxTransfer, int maxGenerate) {
        super(properties);
        this.maxCapacity = maxCapacity;
        this.maxTransfer = maxTransfer;
        this.maxGenerate = maxGenerate;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWERED, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(BlockStateProperties.POWERED, false).setValue(FACING, context.getHorizontalDirection());
    }

    // region CHANGE THESE IN SUBCLASSES
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BaseMachineBlockEntity blockEntity) {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return blockEntity.getDisplayName();
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new BaseMachineContainer(windowId, pos, playerInventory, playerEntity);
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
        return new BaseMachineBlockEntity(ModRegistry.BURN_GENERATOR_BE.get(), pos, state, this.maxCapacity, this.maxTransfer, this.maxGenerate);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof BaseMachineBlockEntity tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t) -> {
            if (t instanceof BaseMachineBlockEntity tile) {
                tile.tickServer();
            }
        };
    }

    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof BaseMachineBlockEntity baseMachineBlockEntity) {
            if (!level.isClientSide && !player.isCreative() && !(new RecipeWrapper((IItemHandlerModifiable) baseMachineBlockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve().get()).isEmpty())) {
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
        if (blockentity instanceof BaseMachineBlockEntity baseMachineBlockEntity) {
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
            if (blockentity instanceof BaseMachineBlockEntity baseMachineBlockEntity) {
                baseMachineBlockEntity.setCustomName(stack.getHoverName());
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
    // endregion

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter reader, List<Component> list, TooltipFlag flags) {
        CompoundTag tag = (CompoundTag) stack.serializeNBT().get("BlockEntityTag");
        String genPerTick = String.valueOf(BaseMachineBlockEntity.BASE_MAX_GENERATE);
        String capacity = String.valueOf(BaseMachineBlockEntity.BASE_MAX_CAPACITY);
        String maxOutput = String.valueOf(BaseMachineBlockEntity.BASE_MAX_OUTPUT);
        if(tag != null && tag.contains("Info")){
            tag = tag.getCompound("Info");
            if(tag.contains("MaxGenerate")){
                genPerTick = tag.get("MaxGenerate").getAsString();
            }
            if(tag.contains("MaxCapacity")){
                capacity = tag.get("MaxCapacity").getAsString();
            }
            if(tag.contains("MaxOutput")){
                maxOutput = tag.get("MaxOutput").getAsString();
            }
        }
        list.add(new TranslatableComponent(MESSAGE_MAX_GEN, genPerTick)
                .withStyle(ChatFormatting.BLUE));
        list.add(new TranslatableComponent(MESSAGE_MAX_CAP, capacity)
                .withStyle(ChatFormatting.BLUE));
        list.add(new TranslatableComponent(MESSAGE_MAX_OUT, maxOutput)
                .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction direction) {
        return super.canConnectRedstone(state, world, pos, direction);
    }
}

