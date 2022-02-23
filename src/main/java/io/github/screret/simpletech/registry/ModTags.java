package io.github.screret.simpletech.registry;

import io.github.screret.simpletech.SimpleTech;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;

public class ModTags {
    public static void init() {
        Blocks.init();
        Items.init();
    }

    public static class Blocks {
        private static void init() {}
        /** Blocks that make up the REACTOR structure */
        public static final Tags.IOptionalNamedTag<Block> REACTOR = tag("reactor");
        /** Blocks valid as a REACTOR tank, required for fuel */
        public static final Tags.IOptionalNamedTag<Block> REACTOR_TANKS = tag("reactor/tanks");
        /** Blocks valid as a REACTOR floor */
        public static final Tags.IOptionalNamedTag<Block> REACTOR_FLOOR = tag("reactor/floor");
        /** Blocks valid in the REACTOR wall */
        public static final Tags.IOptionalNamedTag<Block> REACTOR_WALL = tag("reactor/wall");

        /** Makes a tag in the simpletech domain */
        public static Tags.IOptionalNamedTag<Block> tag(String name) {
            return BlockTags.createOptional(new ResourceLocation(SimpleTech.MOD_ID, name));
        }

        private static Tags.IOptionalNamedTag<Block> forgeTag(String name) {
            return BlockTags.createOptional(new ResourceLocation("forge", name));
        }
    }

    public static class Items {
        private static void init() {}
        public static final Tags.IOptionalNamedTag<Item> REACTOR = tag("reactor");
        /** Blocks valid as a REACTOR tank, required for fuel */
        public static final Tags.IOptionalNamedTag<Item> REACTOR_TANKS = tag("reactor/tanks");
        /** Blocks valid as a REACTOR floor */
        public static final Tags.IOptionalNamedTag<Item> REACTOR_FLOOR = tag("reactor/floor");
        /** Blocks valid in the REACTOR wall */
        public static final Tags.IOptionalNamedTag<Item> REACTOR_WALL = tag("reactor/wall");

        /** Makes a tag in the simpletech domain */
        private static Tags.IOptionalNamedTag<Item> tag(String name) {
            return ItemTags.createOptional(new ResourceLocation(SimpleTech.MOD_ID, name));
        }

        /** Makes a tag in the forge domain */
        public static Tags.IOptionalNamedTag<Item> forgeTag(String name) {
            return ItemTags.createOptional(new ResourceLocation("forge", name));
        }
    }
}
