package io.github.screret.simpletech.registry;

import io.github.screret.simpletech.SimpleTech;
import io.github.screret.simpletech.blocks.BaseMachineBlock;
import io.github.screret.simpletech.blocks.enitites.BaseMachineBlockEntity;
import io.github.screret.simpletech.container.BaseMachineContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
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
    public static final RegistryObject<Block> BASE_MACHINE_BLOCK = BLOCKS.register("base_machine", () -> new BaseMachineBlock(BlockBehaviour.Properties.of(Material.HEAVY_METAL).sound(SoundType.METAL).strength(2.0f).lightLevel(state -> state.getValue(BlockStateProperties.POWERED) ? 14 : 0).requiresCorrectToolForDrops(), 50000, 200, 60));

    //items
    public static final RegistryObject<Item> BASE_MACHINE_ITEM = ITEMS.register("base_machine", () -> new BlockItem(BASE_MACHINE_BLOCK.get(), new Item.Properties().stacksTo(64).tab(SimpleTech.MOD_TAB)));

    //block entities
    public static final RegistryObject<BlockEntityType<BaseMachineBlockEntity>> BASE_MACHINE_BLOCK_ENTITY = BLOCK_ENTITIES.register("base_machine", () -> BlockEntityType.Builder.of(BaseMachineBlockEntity::new, BASE_MACHINE_BLOCK.get()).build(null));

    //containers
    public static final RegistryObject<MenuType<BaseMachineContainer>> BASE_MACHINE_CONTAINER = CONTAINERS.register("base_machine", () -> IForgeMenuType.create((windowId, inv, data) -> new BaseMachineContainer(windowId, data.readBlockPos(), inv, inv.player)));
}
