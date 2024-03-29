package net.minecraft.data.server;

import java.nio.file.Path;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockTagsProvider extends AbstractTagProvider<Block> {
   public BlockTagsProvider(DataGenerator dataGenerator) {
      super(dataGenerator, Registry.BLOCK);
   }

   protected void configure() {
      this.getOrCreateTagBuilder(BlockTags.WOOL).add((Object[])(Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL, Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL));
      this.getOrCreateTagBuilder(BlockTags.PLANKS).add((Object[])(Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.WARPED_PLANKS));
      this.getOrCreateTagBuilder(BlockTags.STONE_BRICKS).add((Object[])(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS).add((Object[])(Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON));
      this.getOrCreateTagBuilder(BlockTags.BUTTONS).addTag(BlockTags.WOODEN_BUTTONS).add((Object)Blocks.STONE_BUTTON).add((Object)Blocks.POLISHED_BLACKSTONE_BUTTON);
      this.getOrCreateTagBuilder(BlockTags.CARPETS).add((Object[])(Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET, Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET, Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_DOORS).add((Object[])(Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS).add((Object[])(Blocks.OAK_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.ACACIA_STAIRS, Blocks.DARK_OAK_STAIRS, Blocks.CRIMSON_STAIRS, Blocks.WARPED_STAIRS));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_SLABS).add((Object[])(Blocks.OAK_SLAB, Blocks.SPRUCE_SLAB, Blocks.BIRCH_SLAB, Blocks.JUNGLE_SLAB, Blocks.ACACIA_SLAB, Blocks.DARK_OAK_SLAB, Blocks.CRIMSON_SLAB, Blocks.WARPED_SLAB));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_FENCES).add((Object[])(Blocks.OAK_FENCE, Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE, Blocks.CRIMSON_FENCE, Blocks.WARPED_FENCE));
      this.getOrCreateTagBuilder(BlockTags.DOORS).addTag(BlockTags.WOODEN_DOORS).add((Object)Blocks.IRON_DOOR);
      this.getOrCreateTagBuilder(BlockTags.SAPLINGS).add((Object[])(Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING));
      this.getOrCreateTagBuilder(BlockTags.DARK_OAK_LOGS).add((Object[])(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD));
      this.getOrCreateTagBuilder(BlockTags.OAK_LOGS).add((Object[])(Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD));
      this.getOrCreateTagBuilder(BlockTags.ACACIA_LOGS).add((Object[])(Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_ACACIA_WOOD));
      this.getOrCreateTagBuilder(BlockTags.BIRCH_LOGS).add((Object[])(Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD));
      this.getOrCreateTagBuilder(BlockTags.JUNGLE_LOGS).add((Object[])(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD));
      this.getOrCreateTagBuilder(BlockTags.SPRUCE_LOGS).add((Object[])(Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_WOOD));
      this.getOrCreateTagBuilder(BlockTags.CRIMSON_STEMS).add((Object[])(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE));
      this.getOrCreateTagBuilder(BlockTags.WARPED_STEMS).add((Object[])(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE));
      this.getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).addTag(BlockTags.DARK_OAK_LOGS).addTag(BlockTags.OAK_LOGS).addTag(BlockTags.ACACIA_LOGS).addTag(BlockTags.BIRCH_LOGS).addTag(BlockTags.JUNGLE_LOGS).addTag(BlockTags.SPRUCE_LOGS);
      this.getOrCreateTagBuilder(BlockTags.LOGS).addTag(BlockTags.LOGS_THAT_BURN).addTag(BlockTags.CRIMSON_STEMS).addTag(BlockTags.WARPED_STEMS);
      this.getOrCreateTagBuilder(BlockTags.ANVIL).add((Object[])(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL));
      this.getOrCreateTagBuilder(BlockTags.SMALL_FLOWERS).add((Object[])(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE));
      this.getOrCreateTagBuilder(BlockTags.ENDERMAN_HOLDABLE).addTag(BlockTags.SMALL_FLOWERS).add((Object[])(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.TNT, Blocks.CACTUS, Blocks.CLAY, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN, Blocks.MELON, Blocks.MYCELIUM, Blocks.CRIMSON_FUNGUS, Blocks.CRIMSON_NYLIUM, Blocks.CRIMSON_ROOTS, Blocks.WARPED_FUNGUS, Blocks.WARPED_NYLIUM, Blocks.WARPED_ROOTS));
      this.getOrCreateTagBuilder(BlockTags.FLOWER_POTS).add((Object[])(Blocks.FLOWER_POT, Blocks.POTTED_POPPY, Blocks.POTTED_BLUE_ORCHID, Blocks.POTTED_ALLIUM, Blocks.POTTED_AZURE_BLUET, Blocks.POTTED_RED_TULIP, Blocks.POTTED_ORANGE_TULIP, Blocks.POTTED_WHITE_TULIP, Blocks.POTTED_PINK_TULIP, Blocks.POTTED_OXEYE_DAISY, Blocks.POTTED_DANDELION, Blocks.POTTED_OAK_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, Blocks.POTTED_BIRCH_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, Blocks.POTTED_ACACIA_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, Blocks.POTTED_RED_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, Blocks.POTTED_DEAD_BUSH, Blocks.POTTED_FERN, Blocks.POTTED_CACTUS, Blocks.POTTED_CORNFLOWER, Blocks.POTTED_LILY_OF_THE_VALLEY, Blocks.POTTED_WITHER_ROSE, Blocks.POTTED_BAMBOO, Blocks.POTTED_CRIMSON_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, Blocks.POTTED_CRIMSON_ROOTS, Blocks.POTTED_WARPED_ROOTS));
      this.getOrCreateTagBuilder(BlockTags.BANNERS).add((Object[])(Blocks.WHITE_BANNER, Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER, Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER, Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add((Object[])(Blocks.OAK_PRESSURE_PLATE, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.ACACIA_PRESSURE_PLATE, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE));
      this.getOrCreateTagBuilder(BlockTags.STONE_PRESSURE_PLATES).add((Object[])(Blocks.STONE_PRESSURE_PLATE, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE));
      this.getOrCreateTagBuilder(BlockTags.PRESSURE_PLATES).add((Object[])(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE)).addTag(BlockTags.WOODEN_PRESSURE_PLATES).addTag(BlockTags.STONE_PRESSURE_PLATES);
      this.getOrCreateTagBuilder(BlockTags.STAIRS).addTag(BlockTags.WOODEN_STAIRS).add((Object[])(Blocks.COBBLESTONE_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.NETHER_BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS, Blocks.BRICK_STAIRS, Blocks.PURPUR_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.RED_SANDSTONE_STAIRS, Blocks.PRISMARINE_BRICK_STAIRS, Blocks.PRISMARINE_STAIRS, Blocks.DARK_PRISMARINE_STAIRS, Blocks.POLISHED_GRANITE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS, Blocks.POLISHED_DIORITE_STAIRS, Blocks.MOSSY_COBBLESTONE_STAIRS, Blocks.END_STONE_BRICK_STAIRS, Blocks.STONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.GRANITE_STAIRS, Blocks.ANDESITE_STAIRS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.POLISHED_ANDESITE_STAIRS, Blocks.DIORITE_STAIRS, Blocks.BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.POLISHED_BLACKSTONE_STAIRS));
      this.getOrCreateTagBuilder(BlockTags.SLABS).addTag(BlockTags.WOODEN_SLABS).add((Object[])(Blocks.STONE_SLAB, Blocks.SMOOTH_STONE_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.SANDSTONE_SLAB, Blocks.PURPUR_SLAB, Blocks.QUARTZ_SLAB, Blocks.RED_SANDSTONE_SLAB, Blocks.BRICK_SLAB, Blocks.COBBLESTONE_SLAB, Blocks.NETHER_BRICK_SLAB, Blocks.PETRIFIED_OAK_SLAB, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE_BRICK_SLAB, Blocks.DARK_PRISMARINE_SLAB, Blocks.POLISHED_GRANITE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.POLISHED_DIORITE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.END_STONE_BRICK_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.GRANITE_SLAB, Blocks.ANDESITE_SLAB, Blocks.RED_NETHER_BRICK_SLAB, Blocks.POLISHED_ANDESITE_SLAB, Blocks.DIORITE_SLAB, Blocks.CUT_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB));
      this.getOrCreateTagBuilder(BlockTags.WALLS).add((Object[])(Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BRICK_WALL, Blocks.PRISMARINE_WALL, Blocks.RED_SANDSTONE_WALL, Blocks.MOSSY_STONE_BRICK_WALL, Blocks.GRANITE_WALL, Blocks.STONE_BRICK_WALL, Blocks.NETHER_BRICK_WALL, Blocks.ANDESITE_WALL, Blocks.RED_NETHER_BRICK_WALL, Blocks.SANDSTONE_WALL, Blocks.END_STONE_BRICK_WALL, Blocks.DIORITE_WALL, Blocks.BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_WALL));
      this.getOrCreateTagBuilder(BlockTags.CORAL_PLANTS).add((Object[])(Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL));
      this.getOrCreateTagBuilder(BlockTags.CORALS).addTag(BlockTags.CORAL_PLANTS).add((Object[])(Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN));
      this.getOrCreateTagBuilder(BlockTags.WALL_CORALS).add((Object[])(Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN));
      this.getOrCreateTagBuilder(BlockTags.SAND).add((Object[])(Blocks.SAND, Blocks.RED_SAND));
      this.getOrCreateTagBuilder(BlockTags.RAILS).add((Object[])(Blocks.RAIL, Blocks.POWERED_RAIL, Blocks.DETECTOR_RAIL, Blocks.ACTIVATOR_RAIL));
      this.getOrCreateTagBuilder(BlockTags.CORAL_BLOCKS).add((Object[])(Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK));
      this.getOrCreateTagBuilder(BlockTags.ICE).add((Object[])(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE));
      this.getOrCreateTagBuilder(BlockTags.VALID_SPAWN).add((Object[])(Blocks.GRASS_BLOCK, Blocks.PODZOL));
      this.getOrCreateTagBuilder(BlockTags.LEAVES).add((Object[])(Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES));
      this.getOrCreateTagBuilder(BlockTags.IMPERMEABLE).add((Object[])(Blocks.GLASS, Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS));
      this.getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS).add((Object[])(Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_TRAPDOOR));
      this.getOrCreateTagBuilder(BlockTags.TRAPDOORS).addTag(BlockTags.WOODEN_TRAPDOORS).add((Object)Blocks.IRON_TRAPDOOR);
      this.getOrCreateTagBuilder(BlockTags.UNDERWATER_BONEMEALS).add((Object)Blocks.SEAGRASS).addTag(BlockTags.CORALS).addTag(BlockTags.WALL_CORALS);
      this.getOrCreateTagBuilder(BlockTags.BAMBOO_PLANTABLE_ON).addTag(BlockTags.SAND).add((Object[])(Blocks.BAMBOO, Blocks.BAMBOO_SAPLING, Blocks.GRAVEL, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.COARSE_DIRT, Blocks.MYCELIUM));
      this.getOrCreateTagBuilder(BlockTags.STANDING_SIGNS).add((Object[])(Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.CRIMSON_SIGN, Blocks.WARPED_SIGN));
      this.getOrCreateTagBuilder(BlockTags.WALL_SIGNS).add((Object[])(Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_WALL_SIGN));
      this.getOrCreateTagBuilder(BlockTags.SIGNS).addTag(BlockTags.STANDING_SIGNS).addTag(BlockTags.WALL_SIGNS);
      this.getOrCreateTagBuilder(BlockTags.BEDS).add((Object[])(Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED));
      this.getOrCreateTagBuilder(BlockTags.FENCES).addTag(BlockTags.WOODEN_FENCES).add((Object)Blocks.NETHER_BRICK_FENCE);
      this.getOrCreateTagBuilder(BlockTags.DRAGON_IMMUNE).add((Object[])(Blocks.BARRIER, Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.MOVING_PISTON, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.END_STONE, Blocks.IRON_BARS, Blocks.RESPAWN_ANCHOR));
      this.getOrCreateTagBuilder(BlockTags.WITHER_IMMUNE).add((Object[])(Blocks.BARRIER, Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.MOVING_PISTON));
      this.getOrCreateTagBuilder(BlockTags.WITHER_SUMMON_BASE_BLOCKS).add((Object[])(Blocks.SOUL_SAND, Blocks.SOUL_SOIL));
      this.getOrCreateTagBuilder(BlockTags.TALL_FLOWERS).add((Object[])(Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH));
      this.getOrCreateTagBuilder(BlockTags.FLOWERS).addTag(BlockTags.SMALL_FLOWERS).addTag(BlockTags.TALL_FLOWERS);
      this.getOrCreateTagBuilder(BlockTags.BEEHIVES).add((Object[])(Blocks.BEE_NEST, Blocks.BEEHIVE));
      this.getOrCreateTagBuilder(BlockTags.CROPS).add((Object[])(Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM));
      this.getOrCreateTagBuilder(BlockTags.BEE_GROWABLES).addTag(BlockTags.CROPS).add((Object)Blocks.SWEET_BERRY_BUSH);
      this.getOrCreateTagBuilder(BlockTags.SHULKER_BOXES).add((Object[])(Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX));
      this.getOrCreateTagBuilder(BlockTags.PORTALS).add((Object[])(Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY));
      this.getOrCreateTagBuilder(BlockTags.FIRE).add((Object[])(Blocks.FIRE, Blocks.SOUL_FIRE));
      this.getOrCreateTagBuilder(BlockTags.NYLIUM).add((Object[])(Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM));
      this.getOrCreateTagBuilder(BlockTags.WART_BLOCKS).add((Object[])(Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK));
      this.getOrCreateTagBuilder(BlockTags.BEACON_BASE_BLOCKS).add((Object[])(Blocks.NETHERITE_BLOCK, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK));
      this.getOrCreateTagBuilder(BlockTags.SOUL_SPEED_BLOCKS).add((Object[])(Blocks.SOUL_SAND, Blocks.SOUL_SOIL));
      this.getOrCreateTagBuilder(BlockTags.WALL_POST_OVERRIDE).add((Object[])(Blocks.TORCH, Blocks.SOUL_TORCH, Blocks.REDSTONE_TORCH, Blocks.TRIPWIRE)).addTag(BlockTags.SIGNS).addTag(BlockTags.BANNERS).addTag(BlockTags.PRESSURE_PLATES);
      this.getOrCreateTagBuilder(BlockTags.CLIMBABLE).add((Object[])(Blocks.LADDER, Blocks.VINE, Blocks.SCAFFOLDING, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT));
      this.getOrCreateTagBuilder(BlockTags.PIGLIN_REPELLENTS).add((Object)Blocks.SOUL_FIRE).add((Object)Blocks.SOUL_TORCH).add((Object)Blocks.SOUL_LANTERN).add((Object)Blocks.SOUL_WALL_TORCH).add((Object)Blocks.SOUL_CAMPFIRE);
      this.getOrCreateTagBuilder(BlockTags.HOGLIN_REPELLENTS).add((Object)Blocks.WARPED_FUNGUS).add((Object)Blocks.POTTED_WARPED_FUNGUS).add((Object)Blocks.NETHER_PORTAL).add((Object)Blocks.RESPAWN_ANCHOR);
      this.getOrCreateTagBuilder(BlockTags.GOLD_ORES).add((Object[])(Blocks.GOLD_ORE, Blocks.NETHER_GOLD_ORE));
      this.getOrCreateTagBuilder(BlockTags.SOUL_FIRE_BASE_BLOCKS).add((Object[])(Blocks.SOUL_SAND, Blocks.SOUL_SOIL));
      this.getOrCreateTagBuilder(BlockTags.NON_FLAMMABLE_WOOD).add((Object[])(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.CRIMSON_PLANKS, Blocks.WARPED_PLANKS, Blocks.CRIMSON_SLAB, Blocks.WARPED_SLAB, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE, Blocks.CRIMSON_FENCE, Blocks.WARPED_FENCE, Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_TRAPDOOR, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE, Blocks.CRIMSON_STAIRS, Blocks.WARPED_STAIRS, Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON, Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR, Blocks.CRIMSON_SIGN, Blocks.WARPED_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_WALL_SIGN));
      this.getOrCreateTagBuilder(BlockTags.STRIDER_WARM_BLOCKS).add((Object)Blocks.LAVA);
      this.getOrCreateTagBuilder(BlockTags.CAMPFIRES).add((Object[])(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE));
      this.getOrCreateTagBuilder(BlockTags.GUARDED_BY_PIGLINS).add((Object[])(Blocks.GOLD_BLOCK, Blocks.BARREL, Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.GILDED_BLACKSTONE, Blocks.TRAPPED_CHEST)).addTag(BlockTags.SHULKER_BOXES).addTag(BlockTags.GOLD_ORES);
      this.getOrCreateTagBuilder(BlockTags.PREVENT_MOB_SPAWNING_INSIDE).addTag(BlockTags.RAILS);
      this.getOrCreateTagBuilder(BlockTags.FENCE_GATES).add((Object[])(Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE));
      this.getOrCreateTagBuilder(BlockTags.UNSTABLE_BOTTOM_CENTER).addTag(BlockTags.FENCE_GATES);
      this.getOrCreateTagBuilder(BlockTags.MUSHROOM_GROW_BLOCK).add((Object)Blocks.MYCELIUM).add((Object)Blocks.PODZOL).add((Object)Blocks.CRIMSON_NYLIUM).add((Object)Blocks.WARPED_NYLIUM);
      this.getOrCreateTagBuilder(BlockTags.INFINIBURN_OVERWORLD).add((Object[])(Blocks.NETHERRACK, Blocks.MAGMA_BLOCK));
      this.getOrCreateTagBuilder(BlockTags.INFINIBURN_NETHER).addTag(BlockTags.INFINIBURN_OVERWORLD);
      this.getOrCreateTagBuilder(BlockTags.INFINIBURN_END).addTag(BlockTags.INFINIBURN_OVERWORLD).add((Object)Blocks.BEDROCK);
      this.getOrCreateTagBuilder(BlockTags.BASE_STONE_OVERWORLD).add((Object)Blocks.STONE).add((Object)Blocks.GRANITE).add((Object)Blocks.DIORITE).add((Object)Blocks.ANDESITE);
      this.getOrCreateTagBuilder(BlockTags.BASE_STONE_NETHER).add((Object)Blocks.NETHERRACK).add((Object)Blocks.BASALT).add((Object)Blocks.BLACKSTONE);
   }

   protected Path getOutput(Identifier id) {
      return this.root.getOutput().resolve("data/" + id.getNamespace() + "/tags/blocks/" + id.getPath() + ".json");
   }

   public String getName() {
      return "Block Tags";
   }
}
