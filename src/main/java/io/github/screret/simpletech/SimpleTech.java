package io.github.screret.simpletech;

import io.github.screret.simpletech.capabilities.CapabilityEnergyContainer;
import io.github.screret.simpletech.container.menu.BaseMachineScreen;
import io.github.screret.simpletech.container.powergen.menu.BurnPowergenScreen;
import io.github.screret.simpletech.container.powergen.menu.DecompositionPowergenScreen;
import io.github.screret.simpletech.container.powergen.menu.FissionReactorSceen;
import io.github.screret.simpletech.recipes.power.BurnRecipe;
import io.github.screret.simpletech.recipes.power.DecompositionRecipe;
import io.github.screret.simpletech.recipes.power.FissionFuel;
import io.github.screret.simpletech.recipes.power.FusionRecipe;
import io.github.screret.simpletech.registry.ModRegistry;
import io.github.screret.simpletech.registry.ModTags;
import io.github.screret.simpletech.registry.PacketManager;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SimpleTech.MOD_ID)
public class SimpleTech {

    public static final String MOD_ID = "simpletech";

    public static final CreativeModeTab MOD_TAB = new CreativeModeTab(SimpleTech.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModRegistry.BASE_MACHINE_BLOCK.get());
        }
    };

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public SimpleTech() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        // Register the capability setup method for modloading
        bus.addListener(this::registerCaps);
        // Register the enqueueIMC method for modloading
        bus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        bus.addListener(this::processIMC);

        bus.addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        ModRegistry.BLOCKS.register(bus);
        ModRegistry.ITEMS.register(bus);
        ModRegistry.BLOCK_ENTITIES.register(bus);
        ModRegistry.CONTAINERS.register(bus);
        ModRegistry.RECIPE_SERIALIZERS.register(bus);
        PacketManager.setup();
        ModTags.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        event.enqueueWork(() -> {
            ModRegistry.BURN_RECIPE_TYPE = RecipeType.<BurnRecipe>register("burn");
            ModRegistry.DECOMPOSITION_RECIPE_TYPE = RecipeType.<DecompositionRecipe>register("rot");
            ModRegistry.FISSION_RECIPE_TYPE = RecipeType.<FissionFuel>register("fission");
            ModRegistry.FUSION_RECIPE_TYPE = RecipeType.<FusionRecipe>register("fusion");
        });
    }

    private void registerCaps(final RegisterCapabilitiesEvent event){
        CapabilityEnergyContainer.register(event);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // some client preinit code
        event.enqueueWork(() -> {
            MenuScreens.register(ModRegistry.BASE_MACHINE_CONTAINER.get(), BaseMachineScreen::new);                     // Attach our screen to the container
            MenuScreens.register(ModRegistry.BURN_GENERATOR_CONTAINER.get(), BurnPowergenScreen::new);                  // Attach our screen to the container
            MenuScreens.register(ModRegistry.DECOMPOSITION_GENERATOR_CONTAINER.get(), DecompositionPowergenScreen::new);// Attach our screen to the container
            MenuScreens.register(ModRegistry.FISSION_REACTOR_CONTAINER.get(), FissionReactorSceen::new);                // Attach our screen to the container
            ItemBlockRenderTypes.setRenderLayer(ModRegistry.FISSION_REACTOR_GLASS_BLOCK.get(), RenderType.cutout());
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        //InterModComms.sendTo("SimpleTech", "helloworld", () -> {
        //    LOGGER.info("Hello world from the MDK");
        //    return "Hello world";
        //});
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        //LOGGER.info("Got IMC {}", event.getIMCStream().
        //        map(m -> m.messageSupplier().get()).
        //        collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
    }
}
