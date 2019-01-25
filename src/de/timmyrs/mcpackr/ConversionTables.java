package de.timmyrs.mcpackr;

import java.util.Map;

/**
 * A utility class to keep track of the blockstate, model, and texture file name changes between two pack formats.
 */
@SuppressWarnings("WeakerAccess")
public class ConversionTables
{
	final Map<String, String> blockstates;
	final Map<String, String> models;
	final Map<String, String> textures;

	private ConversionTables()
	{
		this.blockstates = new BiMap<>();
		this.models = new BiMap<>();
		this.textures = new BiMap<>();
	}

	public static ConversionTables get(int sourcePackFormat, int targetPackFormat)
	{
		final ConversionTables ct = new ConversionTables();
		if(sourcePackFormat >= 4 && targetPackFormat < 4)
		{
			for(String woodType : new String[]{"acacia", "birch", "dark_oak", "jungle", "oak", "spruce"})
			{
				ct.textures.put(woodType + "_door", "door_" + (woodType.equals("oak") ? "wood" : woodType));
				ct.textures.put(woodType + "_door_top", "door_" + (woodType.equals("oak") ? "wood" : woodType) + "_upper");
				ct.textures.put(woodType + "_door_bottom", "door_" + (woodType.equals("oak") ? "wood" : woodType) + "_lower");
				ct.models.put(woodType + "_door_top_hinge", (woodType.equals("oak") ? "wooden" : woodType) + "_door_top_rh");
				ct.models.put(woodType + "_door_bottom_hinge", (woodType.equals("oak") ? "wooden" : woodType) + "_door_bottom_rh");
				ct.textures.put(woodType + "_leaves", "leaves_" + (woodType.equals("dark_oak") ? "big_oak" : woodType));
				ct.textures.put(woodType + "_log", "log_" + (woodType.equals("dark_oak") ? "big_oak" : woodType));
				ct.textures.put(woodType + "_log_top", "log_" + (woodType.equals("dark_oak") ? "big_oak" : woodType) + "_top");
				ct.textures.put(woodType + "_planks", "planks_" + (woodType.equals("dark_oak") ? "big_oak" : woodType));
				ct.textures.put("stripped_" + woodType + "_log", "");
				ct.textures.put("stripped_" + woodType + "_log_top", "");
				ct.textures.put(woodType + "_sapling", "sapling_" + (woodType.equals("dark_oak") ? "roofed_oak" : woodType));
				if(!woodType.equals("oak"))
				{
					ct.textures.put(woodType + "_trapdoor", "");
					ct.models.put(woodType + "_trapdoor_bottom", "");
					ct.models.put(woodType + "_trapdoor_open", "");
					ct.models.put(woodType + "_trapdoor_top", "");
				}
			}
			for(String color : new String[]{"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "lime", "magenta", "orange", "pink", "purple", "red", "light_gray", "white", "yellow"})
			{
				String color_ = (color.equals("light_gray") ? "silver" : color);
				ct.textures.put(color + "_concrete", "concrete_" + color_);
				ct.textures.put(color + "_concrete_powder", "concrete_powder_" + color_);
				ct.textures.put(color + "_wool", "wool_colored_" + color_);
				ct.textures.put(color + "_stained_glass", "glass_" + color_);
				ct.textures.put(color + "_stained_glass_pane_top", "glass_pane_top_" + color_);
				ct.textures.put(color + "_shulker_box", "shulker_top_" + color_);
				ct.textures.put(color + "_terracotta", "hardened_clay_stained_" + color_);
				ct.textures.put(color + "_glazed_terracotta", "terracotta_glazed_" + color_);
			}
			ct.textures.put("oak_trapdoor", "trapdoor");
			ct.models.put("oak_trapdoor_bottom", "wooden_trapdoor_bottom");
			ct.models.put("oak_trapdoor_open", "wooden_trapdoor_open");
			ct.models.put("oak_trapdoor_top", "wooden_trapdoor_top");
			ct.textures.put("iron_door_top", "door_iron_upper");
			ct.textures.put("iron_door_bottom", "door_iron_lower");
			ct.models.put("oak_door_top", "wooden_door_top");
			ct.models.put("oak_door_bottom", "wooden_door_bottom");
			ct.models.put("iron_door_top_hinge", "iron_door_top_rh");
			ct.models.put("iron_door_bottom_hinge", "iron_door_bottom_rh");
			ct.textures.put("granite", "stone_granite");
			ct.textures.put("polished_granite", "stone_granite_smooth");
			ct.textures.put("diorite", "stone_diorite");
			ct.textures.put("polished_diorite", "stone_diorite_smooth");
			ct.textures.put("andesite", "stone_andesite");
			ct.textures.put("polished_andesite", "stone_andesite_smooth");
			ct.textures.put("grass", "tallgrass");
			ct.textures.put("grass_block_side", "grass_side");
			ct.textures.put("grass_block_snow", "grass_side_snowed");
			ct.textures.put("grass_block_side_overlay", "grass_side_overlay");
			ct.textures.put("grass_block_top", "grass_top");
			ct.textures.put("podzol_side", "dirt_podzol_side");
			ct.textures.put("podzol_top", "dirt_podzol_top");
			ct.textures.put("tall_grass_top", "double_plant_grass_top");
			ct.textures.put("tall_grass_bottom", "double_plant_grass_bottom");
			ct.textures.put("cut_sandstone", "sandstone_smooth");
			ct.textures.put("cut_red_sandstone", "red_sandstone_smooth");
			ct.textures.put("chiseled_sandstone", "sandstone_carved");
			ct.textures.put("chiseled_red_sandstone", "red_sandstone_carved");
			ct.textures.put("terracotta", "hardened_clay");
			ct.textures.put("furnace_front", "furnace_front_off");
			ct.textures.put("sandstone", "sandstone_normal");
			ct.textures.put("red_sandstone", "red_sandstone_normal");
			ct.textures.put("nether_quarz_ore", "quarz_ore");
			ct.textures.put("chiseled_quartz_block", "quarz_block_chiseled");
			ct.textures.put("chiseled_quartz_block_top", "quarz_block_chiseled_top");
			ct.textures.put("quartz_pillar", "quarz_block_lines");
			ct.textures.put("quartz_pillar_top", "quarz_block_lines_top");
			ct.textures.put("melon_stem", "melon_stem_disconnected");
			ct.textures.put("attached_melon_stem", "melon_stem_connected");
			ct.textures.put("pumpkin_stem", "pumpkin_stem_disconnected");
			ct.textures.put("attached_pumpkin_stem", "pumpkin_stem_connected");
			ct.textures.put("brown_mushroom", "mushroom_brown");
			ct.textures.put("brown_mushroom_block", "mushroom_block_skin_brown");
			ct.textures.put("red_mushroom", "mushroom_red");
			ct.textures.put("red_mushroom_block", "mushroom_block_skin_red");
			ct.textures.put("mushroom_stem", "mushroom_block_skin_stem");
			ct.textures.put("activator_rail", "rail_activator");
			ct.textures.put("activator_rail_on", "rail_activator_powered");
			ct.textures.put("detector_rail", "rail_detector");
			ct.textures.put("detector_rail_on", "rail_detector_powered");
			ct.textures.put("powered_rail", "rail_golden");
			ct.textures.put("powered_rail_on", "rail_golden_powered");
			ct.blockstates.put("powered_rail", "golden_rail");
			ct.models.put("powered_rail", "golden_rail_flat");
			ct.models.put("powered_rail_raised_ne", "golden_rail_raised_ne");
			ct.models.put("powered_rail_raised_sw", "golden_rail_raised_sw");
			ct.models.put("powered_rail_on", "golden_rail_active_flat");
			ct.models.put("powered_rail_on_raised_ne", "golden_rail_active_raised_ne");
			ct.models.put("powered_rail_on_raised_sw", "golden_rail_active_raised_sw");
			ct.textures.put("rail", "rail_normal");
			ct.textures.put("rail_corner", "rail_normal_turned");
			ct.textures.put("allium", "flower_allium");
			ct.textures.put("blue_orchid", "flower_blue_orchid");
			ct.textures.put("dandelion", "flower_dandelion");
			ct.textures.put("azure_bluet", "flower_houstonia");
			ct.textures.put("oxeye_daisy", "flower_oxeye_daisy");
			ct.textures.put("poppy", "flower_rose");
			ct.textures.put("orange_tulip", "flower_tulip_orange");
			ct.textures.put("pink_tulip", "flower_tulip_pink");
			ct.textures.put("red_tulip", "flower_tulip_red");
			ct.textures.put("white_tulip", "flower_tulip_white");
			ct.textures.put("cobweb", "web");
			ct.textures.put("beetroots_stage0", "beetroots_stage_0");
			ct.textures.put("beetroots_stage1", "beetroots_stage_1");
			ct.textures.put("beetroots_stage2", "beetroots_stage_2");
			ct.textures.put("beetroots_stage3", "beetroots_stage_3");
			ct.textures.put("carrots_stage0", "carrots_stage_0");
			ct.textures.put("carrots_stage1", "carrots_stage_1");
			ct.textures.put("carrots_stage2", "carrots_stage_2");
			ct.textures.put("carrots_stage3", "carrots_stage_3");
			ct.textures.put("cocoa_stage0", "cocoa_stage_0");
			ct.textures.put("cocoa_stage1", "cocoa_stage_1");
			ct.textures.put("cocoa_stage2", "cocoa_stage_2");
			ct.textures.put("nether_wart_stage0", "nether_wart_stage_0");
			ct.textures.put("nether_wart_stage1", "nether_wart_stage_1");
			ct.textures.put("nether_wart_stage2", "nether_wart_stage_2");
			ct.textures.put("potatoes_stage0", "potatoes_stage_0");
			ct.textures.put("potatoes_stage1", "potatoes_stage_1");
			ct.textures.put("potatoes_stage2", "potatoes_stage_2");
			ct.textures.put("potatoes_stage3", "potatoes_stage_3");
			ct.textures.put("wheat_stage0", "wheat_stage_0");
			ct.textures.put("wheat_stage1", "wheat_stage_1");
			ct.textures.put("wheat_stage2", "wheat_stage_2");
			ct.textures.put("wheat_stage3", "wheat_stage_3");
			ct.textures.put("wheat_stage4", "wheat_stage_4");
			ct.textures.put("wheat_stage5", "wheat_stage_5");
			ct.textures.put("wheat_stage6", "wheat_stage_6");
			ct.textures.put("wheat_stage7", "wheat_stage_7");
			ct.textures.put("comparator", "comparator_off");
			ct.textures.put("repeater", "repeater_off");
			ct.textures.put("redstone_torch", "redstone_torch_on");
			ct.textures.put("redstone_lamp", "redstone_lamp_off");
			ct.textures.put("dispenser_front", "dispenser_front_horizontal");
			ct.textures.put("dropper_front", "dropper_front_horizontal");
			ct.textures.put("torch", "torch_on");
			ct.textures.put("bricks", "brick");
			ct.textures.put("chiseled_stone_bricks", "stonebrick_carved");
			ct.textures.put("cracked_stone_bricks", "stonebrick_cracked");
			ct.textures.put("end_stone_bricks", "end_bricks");
			ct.textures.put("mossy_stone_bricks", "stonebrick_mossy");
			ct.textures.put("nether_bricks", "nether_brick");
			ct.textures.put("red_nether_bricks", "red_nether_brick");
			ct.textures.put("stone_bricks", "stonebrick");
			ct.textures.put("mossy_cobblestone", "cobblestone_mossy");
			ct.textures.put("anvil", "anvil_base");
			ct.textures.put("anvil_top", "anvil_top_damaged_0");
			ct.textures.put("chipped_anvil_top", "anvil_top_damaged_1");
			ct.textures.put("damaged_anvil_top", "anvil_top_damaged_2");
			ct.textures.put("piston_top", "piston_top_normal");
			ct.textures.put("lily_pad", "waterlily");
			ct.textures.put("ink_sac", "dye_powder_black");
			ct.textures.put("lapis_lazuli", "dye_powder_blue");
			ct.textures.put("cocoa_beans", "dye_powder_brown");
			ct.textures.put("cyan_dye", "dye_powder_cyan");
			ct.textures.put("gray_dye", "dye_powder_gray");
			ct.textures.put("cactus_green", "dye_powder_green");
			ct.textures.put("light_blue_dye", "dye_powder_light_blue");
			ct.textures.put("lime_dye", "dye_powder_lime");
			ct.textures.put("magenta_dye", "dye_powder_magenta");
			ct.textures.put("orange_dye", "dye_powder_orange");
			ct.textures.put("pink_dye", "dye_powder_pink");
			ct.textures.put("purple_dye", "dye_powder_purple");
			ct.textures.put("rose_red", "dye_powder_red");
			ct.textures.put("light_gray_dye", "dye_powder_silver");
			ct.textures.put("bone_meal", "dye_powder_white");
			ct.textures.put("dandelion_yellow", "dye_powder_yellow");
			ct.textures.put("bucket", "bucket_empty");
			ct.textures.put("cod_bucket", "");
			ct.textures.put("lava_bucket", "bucket_lava");
			ct.textures.put("milk_bucket", "bucket_milk");
			ct.textures.put("pufferfish_bucket", "");
			ct.textures.put("salmon_bucket", "");
			ct.textures.put("tropical_fish_bucket", "");
			ct.textures.put("water_bucket", "bucket_water");
			ct.textures.put("chest_minecart", "minecart_chest");
			ct.textures.put("command_block_minecart", "minecart_command_block");
			ct.textures.put("furnace_minecart", "minecart_furnace");
			ct.textures.put("hopper_minecart", "minecart_hopper");
			ct.textures.put("minecart", "minecart_normal");
			ct.textures.put("tnt_minecart", "minecart_tnt");
			ct.textures.put("beef", "beef_raw");
			ct.textures.put("bow", "bow_standby");
			ct.textures.put("cooked_beef", "beef_cooked");
			ct.textures.put("chicken", "chicken_raw");
			ct.textures.put("cooked_chicken", "chicken_cooked");
			ct.textures.put("cod", "fish_cod_raw");
			ct.textures.put("cooked_cod", "fish_cod_cooked");
			ct.textures.put("mutton", "mutton_raw");
			ct.textures.put("cooked_mutton", "mutton_cooked");
			ct.textures.put("porkchop", "porkchop_raw");
			ct.textures.put("cooked_porkchop", "porkchop_cooked");
			ct.textures.put("rabbit", "rabbit_raw");
			ct.textures.put("cooked_rabbit", "rabbit_cooked");
			ct.textures.put("salmon", "fish_salmon_raw");
			ct.textures.put("cooked_salmon", "fish_salmon_cooked");
			ct.textures.put("tropical_fish", "fish_clownfish_raw");
			ct.textures.put("pufferfish", "fish_pufferfish_raw");
			ct.textures.put("fishing_rod", "fishing_rod_uncast");
			ct.textures.put("book", "book_normal");
			ct.textures.put("enchanted_book", "book_enchanted");
			ct.textures.put("writable_book", "book_writable");
			ct.textures.put("written_book", "book_written");
			ct.textures.put("fermented_spider_eye", "spider_eye_fermented");
			ct.textures.put("map", "map_empty");
			ct.textures.put("slime_ball", "slimeball");
			ct.textures.put("trident", "");
			ct.textures.put("turtle_egg", "");
			ct.textures.put("turtle_helmet", "");
			ct.textures.put("melon_seeds", "seeds_melon");
			ct.textures.put("pumpkin_seeds", "seeds_pumpkin");
			ct.textures.put("wheat_seeds", "seeds_wheat");
			ct.textures.put("sugar_cane", "reeds");
			ct.textures.put("redstone", "redstone_dust");
			ct.textures.put("armor_stand", "wooden_armorstand");
			ct.textures.put("wooden_axe", "wood_axe");
			ct.textures.put("wooden_hoe", "wood_hoe");
			ct.textures.put("wooden_pickaxe", "wood_pickaxe");
			ct.textures.put("wooden_shovel", "wood_shovel");
			ct.textures.put("wooden_sword", "wood_sword");
			ct.textures.put("golden_apple", "apple_golden");
			ct.textures.put("golden_axe", "gold_axe");
			ct.textures.put("golden_boots", "gold_boots");
			ct.textures.put("golden_carrot", "carrot_golden");
			ct.textures.put("golden_chestplate", "gold_chestplate");
			ct.textures.put("golden_helmet", "gold_helmet");
			ct.textures.put("golden_hoe", "gold_hoe");
			ct.textures.put("golden_horse_armor", "gold_horse_armor");
			ct.textures.put("golden_leggings", "gold_leggings");
			ct.textures.put("golden_pickaxe", "gold_pickaxe");
			ct.textures.put("golden_shovel", "gold_shovel");
			ct.textures.put("golden_sword", "gold_sword");
			ct.textures.put("music_disc_13", "record_13");
			ct.textures.put("music_disc_cat", "record_cat");
			ct.textures.put("music_disc_blocks", "record_blocks");
			ct.textures.put("music_disc_chirp", "record_chirp");
			ct.textures.put("music_disc_far", "record_far");
			ct.textures.put("music_disc_mall", "record_mall");
			ct.textures.put("music_disc_mellohi", "record_mellohi");
			ct.textures.put("music_disc_stal", "record_stal");
			ct.textures.put("music_disc_strad", "record_strad");
			ct.textures.put("music_disc_ward", "record_ward");
			ct.textures.put("music_disc_11", "record_11");
			ct.textures.put("music_disc_wait", "record_wait");
		}
		else if(sourcePackFormat < 4 && targetPackFormat >= 4)
		{
			ConversionTables ct_ = ConversionTables.get(targetPackFormat, sourcePackFormat);
			ct.blockstates.putAll(((BiMap<String, String>) ct_.blockstates).reverse);
			ct.models.putAll(((BiMap<String, String>) ct_.models).reverse);
			ct.textures.putAll(((BiMap<String, String>) ct_.textures).reverse);
		}
		return ct;
	}
}
