package io.github.screret.simpletech.registry;

import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.blocks.BaseMachineBlock;
import io.github.screret.simpletech.blocks.enitites.BaseMachineBlockEntity;
import io.github.screret.simpletech.blocks.powergen.BurnPowergenBlock;
import io.github.screret.simpletech.blocks.powergen.DecompositionPowergenBlock;
import io.github.screret.simpletech.container.powergen.FissionReactorContainer;
import io.github.screret.simpletech.multiblock.block.ControllerBlock;
import io.github.screret.simpletech.multiblock.fissionreactor.*;
import io.github.screret.simpletech.multiblock.fissionreactor.block.FissionReactorCoreBlock;
import io.github.screret.simpletech.blocks.powergen.entities.BurnPowergenBlockEntity;
import io.github.screret.simpletech.blocks.powergen.entities.DecompositionPowergenBlockEntity;
import io.github.screret.simpletech.container.BaseMachineContainer;
import io.github.screret.simpletech.container.powergen.BurnPowergenContainer;
import io.github.screret.simpletech.container.powergen.DecompositionPowergenContainer;
import io.github.screret.simpletech.multiblock.fissionreactor.block.OrientableReactorBlock;
import io.github.screret.simpletech.multiblock.fissionreactor.block.ReactorFrameBlock;
import io.github.screret.simpletech.multiblock.fissionreactor.block.ReactorGlassBlock;
import io.github.screret.simpletech.recipes.power.BurnRecipe;
import io.github.screret.simpletech.recipes.power.DecompositionRecipe;
import io.github.screret.simpletech.recipes.power.FissionFuel;
import io.github.screret.simpletech.recipes.power.FusionRecipe;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SimpleTech.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, SimpleTech.MOD_ID);
    public static final DeferredRegister<Attribute> ENTITY_ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, SimpleTech.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SimpleTech.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, SimpleTech.MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, SimpleTech.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SimpleTech.MOD_ID);


    //blocks
    public static final RegistryObject<Block> BASE_MACHINE_BLOCK = BLOCKS.register("base_machine", () -> new BaseMachineBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(2.0f).lightLevel(state -> state.getValue(BlockStateProperties.POWERED) ? 14 : 0).requiresCorrectToolForDrops(), 500, 200, 6));
    public static final RegistryObject<Block> BURN_GENERATOR_BLOCK = BLOCKS.register("burn_generator", () -> new BurnPowergenBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(2.0f).lightLevel(state -> state.getValue(BlockStateProperties.POWERED) ? 14 : 0).requiresCorrectToolForDrops(), 500, 200, 6));
    public static final RegistryObject<Block> DECOMPOSITION_GENERATOR_BLOCK = BLOCKS.register("rot_generator", () -> new DecompositionPowergenBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(2.0f).lightLevel(state -> state.getValue(BlockStateProperties.POWERED) ? 14 : 0).requiresCorrectToolForDrops(), 500, 200, 6));
    public static final RegistryObject<Block> FISSION_REACTOR_CORE_BLOCK = BLOCKS.register("fission_reactor_core", () -> new FissionReactorCoreBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(3.0f).lightLevel(state -> state.getValue(ControllerBlock.ACTIVE) ? 14 : 0).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> FISSION_REACTOR_FRAME_BLOCK = BLOCKS.register("fission_reactor_frame", () -> new ReactorFrameBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(2.0f).requiresCorrectToolForDrops().isValidSpawn((s, r, p, e) -> !s.hasProperty(ReactorFrameBlock.IN_STRUCTURE) || !s.getValue(ReactorFrameBlock.IN_STRUCTURE))));
    public static final RegistryObject<Block> FISSION_REACTOR_GLASS_BLOCK = BLOCKS.register("fission_reactor_glass", () -> new ReactorGlassBlock(BlockBehaviour.Properties.of(Material.BUILDABLE_GLASS).sound(SoundType.GLASS).strength(0.75f).requiresCorrectToolForDrops().noOcclusion().isValidSpawn(Blocks::never).isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never)));
    public static final RegistryObject<Block> REACTOR_CHUTE_BLOCK = BLOCKS.register("reactor_chute", () -> new OrientableReactorBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(6.0F, 18.0F), ReactorInputOutputBlockEntity.ReactorChuteBlockEntity::new));
    public static final RegistryObject<Block> REACTOR_POWER_IO_BLOCK = BLOCKS.register("reactor_power_io", () -> new OrientableReactorBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(6.0F, 18.0F), ReactorInputOutputBlockEntity.ReactorChuteBlockEntity::new));
    public static final RegistryObject<Block> REACTOR_DRAIN_BLOCK = BLOCKS.register("reactor_drain", () -> new OrientableReactorBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(6.0F, 18.0F), ReactorInputOutputBlockEntity.ReactorChuteBlockEntity::new));

    //items
    public static final RegistryObject<Item> BASE_MACHINE_ITEM = ITEMS.register("base_machine", () -> new BlockItem(BASE_MACHINE_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> BURN_GENERATOR_ITEM = ITEMS.register("burn_generator", () -> new BlockItem(BURN_GENERATOR_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> DECOMPOSITION_GENERATOR_ITEM = ITEMS.register("rot_generator", () -> new BlockItem(DECOMPOSITION_GENERATOR_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> FISSION_REACTOR_CORE_ITEM = ITEMS.register("fission_reactor_core", () -> new BlockItem(FISSION_REACTOR_CORE_BLOCK.get(), new Item.Properties().stacksTo(8).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> FISSION_REACTOR_FRAME_ITEM = ITEMS.register("fission_reactor_frame", () -> new BlockItem(FISSION_REACTOR_FRAME_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> FISSION_REACTOR_GLASS_ITEM = ITEMS.register("fission_reactor_glass", () -> new BlockItem(FISSION_REACTOR_GLASS_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> REACTOR_CHUTE_ITEM = ITEMS.register("reactor_chute", () -> new BlockItem(REACTOR_CHUTE_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));
    public static final RegistryObject<Item> REACTOR_POWER_IO_ITEM = ITEMS.register("reactor_power_io", () -> new BlockItem(REACTOR_POWER_IO_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));

    //block entities
    public static final RegistryObject<BlockEntityType<BaseMachineBlockEntity>> BASE_MACHINE_BE = BLOCK_ENTITIES.register("base_machine", () -> BlockEntityType.Builder.of(BaseMachineBlockEntity::new, BASE_MACHINE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BurnPowergenBlockEntity>> BURN_GENERATOR_BE = BLOCK_ENTITIES.register("burn_generator", () -> BlockEntityType.Builder.of(BurnPowergenBlockEntity::new, BURN_GENERATOR_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<DecompositionPowergenBlockEntity>> DECOMPOSITION_GENERATOR_BE = BLOCK_ENTITIES.register("rot_generator", () -> BlockEntityType.Builder.of(DecompositionPowergenBlockEntity::new, DECOMPOSITION_GENERATOR_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<FissionReactorCoreBlockEntity>> FISSION_REACTOR_CORE_BE = BLOCK_ENTITIES.register("fission_reactor_core", () -> BlockEntityType.Builder.of(FissionReactorCoreBlockEntity::new, FISSION_REACTOR_CORE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<ReactorComponentBlockEntity>> REACTOR_COMPONENT_BE = BLOCK_ENTITIES.register("fission_reactor_part", () -> BlockEntityType.Builder.of(ReactorComponentBlockEntity::new,
            FISSION_REACTOR_FRAME_BLOCK.get(),
            FISSION_REACTOR_GLASS_BLOCK.get()
    ).build(null));
    public static final RegistryObject<BlockEntityType<ReactorInputOutputBlockEntity.ReactorChuteBlockEntity>> REACTOR_CHUTE_BE = BLOCK_ENTITIES.register("reactor_chute", () -> BlockEntityType.Builder.of(ReactorInputOutputBlockEntity.ReactorChuteBlockEntity::new, REACTOR_CHUTE_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<DrainBlockEntity>> REACTOR_DRAIN_BE = BLOCK_ENTITIES.register("reactor_drain", () -> BlockEntityType.Builder.of(DrainBlockEntity::new, REACTOR_DRAIN_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<ReactorEnergyIOBlockEntity>> REACTOR_POWER_IO_BE = BLOCK_ENTITIES.register("reactor_power_io", () -> BlockEntityType.Builder.of(ReactorEnergyIOBlockEntity::new, REACTOR_POWER_IO_BLOCK.get()).build(null));

    //containers
    public static final RegistryObject<MenuType<BaseMachineContainer>> BASE_MACHINE_CONTAINER = CONTAINERS.register("base_machine", () -> IForgeMenuType.create((windowId, inv, data) -> new BaseMachineContainer(windowId, data.readBlockPos(), inv, inv.player)));
    public static final RegistryObject<MenuType<BurnPowergenContainer>> BURN_GENERATOR_CONTAINER = CONTAINERS.register("burn_generator", () -> IForgeMenuType.create((windowId, inv, data) -> new BurnPowergenContainer(windowId, data.readBlockPos(), inv, inv.player)));
    public static final RegistryObject<MenuType<DecompositionPowergenContainer>> DECOMPOSITION_GENERATOR_CONTAINER = CONTAINERS.register("rot_generator", () -> IForgeMenuType.create((windowId, inv, data) -> new DecompositionPowergenContainer(windowId, data.readBlockPos(), inv, inv.player)));
    public static final RegistryObject<MenuType<FissionReactorContainer>> FISSION_REACTOR_CONTAINER = CONTAINERS.register("fission_reactor", () -> IForgeMenuType.create((windowId, inv, data) -> new FissionReactorContainer(windowId, data.readBlockPos(), inv, inv.player)));

    //recipes
    public static final RegistryObject<RecipeSerializer<BurnRecipe>> BURN_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("burn", BurnRecipe.Serializer::new);
    public static RecipeType<BurnRecipe> BURN_RECIPE_TYPE = null;
    public static final RegistryObject<RecipeSerializer<DecompositionRecipe>> DECOMPOSITION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("rot", DecompositionRecipe.Serializer::new);
    public static RecipeType<DecompositionRecipe> DECOMPOSITION_RECIPE_TYPE = null;
    public static final RegistryObject<RecipeSerializer<FissionFuel>> FISSION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("fission", FissionFuel.Serializer::new);
    public static RecipeType<FissionFuel> FISSION_RECIPE_TYPE = null;
    public static final RegistryObject<RecipeSerializer<FusionRecipe>> FUSION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("fusion", FusionRecipe.Serializer::new);
    public static RecipeType<FusionRecipe> FUSION_RECIPE_TYPE = null;

}
