package de.timmyrs.mcpackr;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import de.timmyrs.mcpackr.logging.MCPackrLogger;
import de.timmyrs.mcpackr.logging.MCPackrStdoutLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public class MCPackr
{
	private static File resourcePackFolder;
	private static ArrayList<String> files;

	public static void main(String[] args) throws IOException
	{
		final File workingDirectory = new File(System.getProperty("user.dir"));
		packResourcePack(workingDirectory, workingDirectory, new MCPackrStdoutLogger());
	}

	/**
	 * Packs the resource pack at `resourcePackFolder` into zips in `outputFolder` and returns File objects of the zips that have been created.
	 *
	 * @param logger The logger. Use {@link de.timmyrs.mcpackr.logging.MCPackrNullLogger} for no logging, {@link MCPackrStdoutLogger} for logging to `stdout`, or create your own logger extending {@link MCPackrLogger}.
	 * @return The files that have been generated or empty on failure.
	 */
	public static HashMap<PackFormat, File> packResourcePack(File resourcePackFolder, File outputFolder, MCPackrLogger logger) throws IOException
	{
		final HashMap<PackFormat, File> res = new HashMap<>();
		final File packmetaFile = new File(resourcePackFolder.getPath() + "/pack.mcmeta");
		if(!packmetaFile.exists() || !packmetaFile.isFile())
		{
			logger.log("[MCPackr] Resource pack is missing pack.mcmeta.\n");
			return res;
		}
		final JsonObject packmeta = Json.parse(new FileReader(packmetaFile)).asObject().get("pack").asObject();
		if(packmeta.get("pack_format").asInt() != PackFormat.latest.id)
		{
			logger.log("[MCPackr] Your resource pack has to be compatible with " + PackFormat.latest.mcversions + " (pack format " + PackFormat.latest.id + ").\n");
			return res;
		}
		final File resourcesRoot = new File(resourcePackFolder.getPath() + "/assets/minecraft");
		if(!resourcesRoot.exists() || !resourcesRoot.isDirectory())
		{
			logger.log("[MCPackr] Your resource pack is missing `assets/minecraft/`.\n");
			return res;
		}
		final String packName = resourcePackFolder.getName();
		for(PackFormat packFormat : PackFormat.values())
		{
			final File outputFile = new File(outputFolder.getPath() + "/" + packName + " (" + packFormat.mcversions + ").zip");
			if(outputFile.exists() && outputFile.isFile() && !outputFile.delete())
			{
				logger.log("[MCPackr] Failed to delete " + outputFile.getPath() + "\n");
				return res;
			}
		}
		logger.log("[MCPackr] Indexing " + packName + "...\n");
		files = new ArrayList<>();
		MCPackr.resourcePackFolder = resourcePackFolder;
		recursivelyIndex(resourcePackFolder);
		logger.log("[MCPackr] Resolving version-specific files...\n");
		final HashMap<Integer, ArrayList<String>> versions = new HashMap<>();
		for(PackFormat packFormat : PackFormat.values())
		{
			versions.put(packFormat.id, new ArrayList<>());
		}
		for(final String file : files)
		{
			if(file.substring(file.length() - 2, file.length() - 1).equals("@"))
			{
				versions.get(Integer.valueOf(file.substring(file.length() - 1))).add(file);
			}
			else
			{
				for(PackFormat packFormat : PackFormat.values())
				{
					if(!files.contains(file + "@" + packFormat.id))
					{
						versions.get(packFormat.id).add(file);
					}
				}
			}
		}
		logger.log("[MCPackr] Building conversion tables...\n");
		final HashMap<String, String> blockstateNameConversion = new HashMap<>();
		final HashMap<String, String> modelNameConversions = new HashMap<>();
		final HashMap<String, String> textureNameConversions = new HashMap<>();
		for(String woodType : new String[]{"acacia", "birch", "dark_oak", "jungle", "oak", "spruce"})
		{
			textureNameConversions.put(woodType + "_door", "door_" + (woodType.equals("oak") ? "wood" : woodType));
			textureNameConversions.put(woodType + "_door_top", "door_" + (woodType.equals("oak") ? "wood" : woodType) + "_upper");
			textureNameConversions.put(woodType + "_door_bottom", "door_" + (woodType.equals("oak") ? "wood" : woodType) + "_lower");
			modelNameConversions.put(woodType + "_door_top_hinge", (woodType.equals("oak") ? "wooden" : woodType) + "_door_top_rh");
			modelNameConversions.put(woodType + "_door_bottom_hinge", (woodType.equals("oak") ? "wooden" : woodType) + "_door_bottom_rh");
			textureNameConversions.put(woodType + "_leaves", "leaves_" + (woodType.equals("dark_oak") ? "big_oak" : woodType));
			textureNameConversions.put(woodType + "_log", "log_" + (woodType.equals("dark_oak") ? "big_oak" : woodType));
			textureNameConversions.put(woodType + "_log_top", "log_" + (woodType.equals("dark_oak") ? "big_oak" : woodType) + "_top");
			textureNameConversions.put(woodType + "_planks", "planks_" + (woodType.equals("dark_oak") ? "big_oak" : woodType));
			textureNameConversions.put("stripped_" + woodType + "_log", "");
			textureNameConversions.put("stripped_" + woodType + "_log_top", "");
			textureNameConversions.put(woodType + "_sapling", "sapling_" + (woodType.equals("dark_oak") ? "roofed_oak" : woodType));
			if(!woodType.equals("oak"))
			{
				textureNameConversions.put(woodType + "_trapdoor", "");
				modelNameConversions.put(woodType + "_trapdoor_bottom", "");
				modelNameConversions.put(woodType + "_trapdoor_open", "");
				modelNameConversions.put(woodType + "_trapdoor_top", "");
			}
		}
		for(String color : new String[]{"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "lime", "magenta", "orange", "pink", "purple", "red", "light_gray", "white", "yellow"})
		{
			String color_ = (color.equals("light_gray") ? "silver" : color);
			textureNameConversions.put(color + "_concrete", "concrete_" + color_);
			textureNameConversions.put(color + "_concrete_powder", "concrete_powder_" + color_);
			textureNameConversions.put(color + "_wool", "wool_colored_" + color_);
			textureNameConversions.put(color + "_stained_glass", "glass_" + color_);
			textureNameConversions.put(color + "_stained_glass_pane_top", "glass_pane_top_" + color_);
			textureNameConversions.put(color + "_shulker_box", "shulker_top_" + color_);
			textureNameConversions.put(color + "_terracotta", "hardened_clay_stained_" + color_);
			textureNameConversions.put(color + "_glazed_terracotta", "terracotta_glazed_" + color_);
		}
		textureNameConversions.put("oak_trapdoor", "trapdoor");
		modelNameConversions.put("oak_trapdoor_bottom", "wooden_trapdoor_bottom");
		modelNameConversions.put("oak_trapdoor_open", "wooden_trapdoor_open");
		modelNameConversions.put("oak_trapdoor_top", "wooden_trapdoor_top");
		textureNameConversions.put("iron_door_top", "door_iron_upper");
		textureNameConversions.put("iron_door_bottom", "door_iron_lower");
		modelNameConversions.put("oak_door_top", "wooden_door_top");
		modelNameConversions.put("oak_door_bottom", "wooden_door_bottom");
		modelNameConversions.put("iron_door_top_hinge", "iron_door_top_rh");
		modelNameConversions.put("iron_door_bottom_hinge", "iron_door_bottom_rh");
		textureNameConversions.put("granite", "stone_granite");
		textureNameConversions.put("polished_granite", "stone_granite_smooth");
		textureNameConversions.put("diorite", "stone_diorite");
		textureNameConversions.put("polished_diorite", "stone_diorite_smooth");
		textureNameConversions.put("andesite", "stone_andesite");
		textureNameConversions.put("polished_andesite", "stone_andesite_smooth");
		textureNameConversions.put("grass", "tallgrass");
		textureNameConversions.put("grass_block_side", "grass_side");
		textureNameConversions.put("grass_block_snow", "grass_side_snowed");
		textureNameConversions.put("grass_block_side_overlay", "grass_side_overlay");
		textureNameConversions.put("grass_block_top", "grass_top");
		textureNameConversions.put("podzol_side", "dirt_podzol_side");
		textureNameConversions.put("podzol_top", "dirt_podzol_top");
		textureNameConversions.put("tall_grass_top", "double_plant_grass_top");
		textureNameConversions.put("tall_grass_bottom", "double_plant_grass_bottom");
		textureNameConversions.put("cut_sandstone", "sandstone_smooth");
		textureNameConversions.put("cut_red_sandstone", "red_sandstone_smooth");
		textureNameConversions.put("chiseled_sandstone", "sandstone_carved");
		textureNameConversions.put("chiseled_red_sandstone", "red_sandstone_carved");
		textureNameConversions.put("terracotta", "hardened_clay");
		textureNameConversions.put("furnace_front", "furnace_front_off");
		textureNameConversions.put("sandstone", "sandstone_normal");
		textureNameConversions.put("red_sandstone", "red_sandstone_normal");
		textureNameConversions.put("nether_quarz_ore", "quarz_ore");
		textureNameConversions.put("chiseled_quartz_block", "quarz_block_chiseled");
		textureNameConversions.put("chiseled_quartz_block_top", "quarz_block_chiseled_top");
		textureNameConversions.put("quartz_pillar", "quarz_block_lines");
		textureNameConversions.put("quartz_pillar_top", "quarz_block_lines_top");
		textureNameConversions.put("melon_stem", "melon_stem_disconnected");
		textureNameConversions.put("attached_melon_stem", "melon_stem_connected");
		textureNameConversions.put("pumpkin_stem", "pumpkin_stem_disconnected");
		textureNameConversions.put("attached_pumpkin_stem", "pumpkin_stem_connected");
		textureNameConversions.put("brown_mushroom", "mushroom_brown");
		textureNameConversions.put("brown_mushroom_block", "mushroom_block_skin_brown");
		textureNameConversions.put("red_mushroom", "mushroom_red");
		textureNameConversions.put("red_mushroom_block", "mushroom_block_skin_red");
		textureNameConversions.put("mushroom_stem", "mushroom_block_skin_stem");
		textureNameConversions.put("activator_rail", "rail_activator");
		textureNameConversions.put("activator_rail_on", "rail_activator_powered");
		textureNameConversions.put("detector_rail", "rail_detector");
		textureNameConversions.put("detector_rail_on", "rail_detector_powered");
		textureNameConversions.put("powered_rail", "rail_golden");
		textureNameConversions.put("powered_rail_on", "rail_golden_powered");
		blockstateNameConversion.put("powered_rail", "golden_rail");
		modelNameConversions.put("powered_rail", "golden_rail_flat");
		modelNameConversions.put("powered_rail_raised_ne", "golden_rail_raised_ne");
		modelNameConversions.put("powered_rail_raised_sw", "golden_rail_raised_sw");
		modelNameConversions.put("powered_rail_on", "golden_rail_active_flat");
		modelNameConversions.put("powered_rail_on_raised_ne", "golden_rail_active_raised_ne");
		modelNameConversions.put("powered_rail_on_raised_sw", "golden_rail_active_raised_sw");
		textureNameConversions.put("rail", "rail_normal");
		textureNameConversions.put("rail_corner", "rail_normal_turned");
		textureNameConversions.put("allium", "flower_allium");
		textureNameConversions.put("blue_orchid", "flower_blue_orchid");
		textureNameConversions.put("dandelion", "flower_dandelion");
		textureNameConversions.put("azure_bluet", "flower_houstonia");
		textureNameConversions.put("oxeye_daisy", "flower_oxeye_daisy");
		textureNameConversions.put("poppy", "flower_rose");
		textureNameConversions.put("orange_tulip", "flower_tulip_orange");
		textureNameConversions.put("pink_tulip", "flower_tulip_pink");
		textureNameConversions.put("red_tulip", "flower_tulip_red");
		textureNameConversions.put("white_tulip", "flower_tulip_white");
		textureNameConversions.put("cobweb", "web");
		textureNameConversions.put("beetroots_stage0", "beetroots_stage_0");
		textureNameConversions.put("beetroots_stage1", "beetroots_stage_1");
		textureNameConversions.put("beetroots_stage2", "beetroots_stage_2");
		textureNameConversions.put("beetroots_stage3", "beetroots_stage_3");
		textureNameConversions.put("carrots_stage0", "carrots_stage_0");
		textureNameConversions.put("carrots_stage1", "carrots_stage_1");
		textureNameConversions.put("carrots_stage2", "carrots_stage_2");
		textureNameConversions.put("carrots_stage3", "carrots_stage_3");
		textureNameConversions.put("cocoa_stage0", "cocoa_stage_0");
		textureNameConversions.put("cocoa_stage1", "cocoa_stage_1");
		textureNameConversions.put("cocoa_stage2", "cocoa_stage_2");
		textureNameConversions.put("nether_wart_stage0", "nether_wart_stage_0");
		textureNameConversions.put("nether_wart_stage1", "nether_wart_stage_1");
		textureNameConversions.put("nether_wart_stage2", "nether_wart_stage_2");
		textureNameConversions.put("potatoes_stage0", "potatoes_stage_0");
		textureNameConversions.put("potatoes_stage1", "potatoes_stage_1");
		textureNameConversions.put("potatoes_stage2", "potatoes_stage_2");
		textureNameConversions.put("potatoes_stage3", "potatoes_stage_3");
		textureNameConversions.put("wheat_stage0", "wheat_stage_0");
		textureNameConversions.put("wheat_stage1", "wheat_stage_1");
		textureNameConversions.put("wheat_stage2", "wheat_stage_2");
		textureNameConversions.put("wheat_stage3", "wheat_stage_3");
		textureNameConversions.put("wheat_stage4", "wheat_stage_4");
		textureNameConversions.put("wheat_stage5", "wheat_stage_5");
		textureNameConversions.put("wheat_stage6", "wheat_stage_6");
		textureNameConversions.put("wheat_stage7", "wheat_stage_7");
		textureNameConversions.put("comparator", "comparator_off");
		textureNameConversions.put("repeater", "repeater_off");
		textureNameConversions.put("redstone_torch", "redstone_torch_on");
		textureNameConversions.put("redstone_lamp", "redstone_lamp_off");
		textureNameConversions.put("dispenser_front", "dispenser_front_horizontal");
		textureNameConversions.put("dropper_front", "dropper_front_horizontal");
		textureNameConversions.put("torch", "torch_on");
		textureNameConversions.put("bricks", "brick");
		textureNameConversions.put("chiseled_stone_bricks", "stonebrick_carved");
		textureNameConversions.put("cracked_stone_bricks", "stonebrick_cracked");
		textureNameConversions.put("end_stone_bricks", "end_bricks");
		textureNameConversions.put("mossy_stone_bricks", "stonebrick_mossy");
		textureNameConversions.put("nether_bricks", "nether_brick");
		textureNameConversions.put("red_nether_bricks", "red_nether_brick");
		textureNameConversions.put("stone_bricks", "stonebrick");
		textureNameConversions.put("mossy_cobblestone", "cobblestone_mossy");
		textureNameConversions.put("anvil", "anvil_base");
		textureNameConversions.put("anvil_top", "anvil_top_damaged_0");
		textureNameConversions.put("chipped_anvil_top", "anvil_top_damaged_1");
		textureNameConversions.put("damaged_anvil_top", "anvil_top_damaged_2");
		textureNameConversions.put("piston_top", "piston_top_normal");
		textureNameConversions.put("lily_pad", "waterlily");
		textureNameConversions.put("ink_sac", "dye_powder_black");
		textureNameConversions.put("lapis_lazuli", "dye_powder_blue");
		textureNameConversions.put("cocoa_beans", "dye_powder_brown");
		textureNameConversions.put("cyan_dye", "dye_powder_cyan");
		textureNameConversions.put("gray_dye", "dye_powder_gray");
		textureNameConversions.put("cactus_green", "dye_powder_green");
		textureNameConversions.put("light_blue_dye", "dye_powder_light_blue");
		textureNameConversions.put("lime_dye", "dye_powder_lime");
		textureNameConversions.put("magenta_dye", "dye_powder_magenta");
		textureNameConversions.put("orange_dye", "dye_powder_orange");
		textureNameConversions.put("pink_dye", "dye_powder_pink");
		textureNameConversions.put("purple_dye", "dye_powder_purple");
		textureNameConversions.put("rose_red", "dye_powder_red");
		textureNameConversions.put("light_gray_dye", "dye_powder_silver");
		textureNameConversions.put("bone_meal", "dye_powder_white");
		textureNameConversions.put("dandelion_yellow", "dye_powder_yellow");
		textureNameConversions.put("bucket", "bucket_empty");
		textureNameConversions.put("cod_bucket", "");
		textureNameConversions.put("lava_bucket", "bucket_lava");
		textureNameConversions.put("milk_bucket", "bucket_milk");
		textureNameConversions.put("pufferfish_bucket", "");
		textureNameConversions.put("salmon_bucket", "");
		textureNameConversions.put("tropical_fish_bucket", "");
		textureNameConversions.put("water_bucket", "bucket_water");
		textureNameConversions.put("chest_minecart", "minecart_chest");
		textureNameConversions.put("command_block_minecart", "minecart_command_block");
		textureNameConversions.put("furnace_minecart", "minecart_furnace");
		textureNameConversions.put("hopper_minecart", "minecart_hopper");
		textureNameConversions.put("minecart", "minecart_normal");
		textureNameConversions.put("tnt_minecart", "minecart_tnt");
		textureNameConversions.put("beef", "beef_raw");
		textureNameConversions.put("bow", "bow_standby");
		textureNameConversions.put("cooked_beef", "beef_cooked");
		textureNameConversions.put("chicken", "chicken_raw");
		textureNameConversions.put("cooked_chicken", "chicken_cooked");
		textureNameConversions.put("cod", "fish_cod_raw");
		textureNameConversions.put("cooked_cod", "fish_cod_cooked");
		textureNameConversions.put("mutton", "mutton_raw");
		textureNameConversions.put("cooked_mutton", "mutton_cooked");
		textureNameConversions.put("porkchop", "porkchop_raw");
		textureNameConversions.put("cooked_porkchop", "porkchop_cooked");
		textureNameConversions.put("rabbit", "rabbit_raw");
		textureNameConversions.put("cooked_rabbit", "rabbit_cooked");
		textureNameConversions.put("salmon", "fish_salmon_raw");
		textureNameConversions.put("cooked_salmon", "fish_salmon_cooked");
		textureNameConversions.put("tropical_fish", "fish_clownfish_raw");
		textureNameConversions.put("pufferfish", "fish_pufferfish_raw");
		textureNameConversions.put("fishing_rod", "fishing_rod_uncast");
		textureNameConversions.put("book", "book_normal");
		textureNameConversions.put("enchanted_book", "book_enchanted");
		textureNameConversions.put("writable_book", "book_writable");
		textureNameConversions.put("written_book", "book_written");
		textureNameConversions.put("fermented_spider_eye", "spider_eye_fermented");
		textureNameConversions.put("map", "map_empty");
		textureNameConversions.put("slime_ball", "slimeball");
		textureNameConversions.put("trident", "");
		textureNameConversions.put("turtle_egg", "");
		textureNameConversions.put("turtle_helmet", "");
		textureNameConversions.put("melon_seeds", "seeds_melon");
		textureNameConversions.put("pumpkin_seeds", "seeds_pumpkin");
		textureNameConversions.put("wheat_seeds", "seeds_wheat");
		textureNameConversions.put("sugar_cane", "reeds");
		textureNameConversions.put("redstone", "redstone_dust");
		textureNameConversions.put("golden_apple", "apple_golden");
		textureNameConversions.put("golden_axe", "gold_axe");
		textureNameConversions.put("golden_boots", "gold_boots");
		textureNameConversions.put("golden_carrot", "carrot_golden");
		textureNameConversions.put("golden_chestplate", "gold_chestplate");
		textureNameConversions.put("golden_helmet", "gold_helmet");
		textureNameConversions.put("golden_hoe", "gold_hoe");
		textureNameConversions.put("golden_horse_armor", "gold_horse_armor");
		textureNameConversions.put("golden_leggings", "gold_leggings");
		textureNameConversions.put("golden_pickaxe", "gold_pickaxe");
		textureNameConversions.put("golden_shovel", "gold_shovel");
		textureNameConversions.put("golden_sword", "gold_sword");
		textureNameConversions.put("music_disc_13", "record_13");
		textureNameConversions.put("music_disc_cat", "record_cat");
		textureNameConversions.put("music_disc_blocks", "record_blocks");
		textureNameConversions.put("music_disc_chirp", "record_chirp");
		textureNameConversions.put("music_disc_far", "record_far");
		textureNameConversions.put("music_disc_mall", "record_mall");
		textureNameConversions.put("music_disc_mellohi", "record_mellohi");
		textureNameConversions.put("music_disc_stal", "record_stal");
		textureNameConversions.put("music_disc_strad", "record_strad");
		textureNameConversions.put("music_disc_ward", "record_ward");
		textureNameConversions.put("music_disc_11", "record_11");
		textureNameConversions.put("music_disc_wait", "record_wait");
		for(PackFormat packFormat : PackFormat.values())
		{
			final ArrayList<String> modelPolyfills = new ArrayList<>();
			if(packFormat.id == 1)
			{
				modelPolyfills.add("block/block");
				modelPolyfills.add("block/thin_block");
			}
			final File zipFile = new File(resourcePackFolder + "/" + packName + " (" + packFormat.mcversions + ").zip");
			logger.log("[MCPackr] Building " + zipFile.getName() + "...\n");
			final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
			for(String file : versions.get(packFormat.id))
			{
				String output_name = file;
				if(file.substring(file.length() - 2, file.length() - 1).equals("@"))
				{
					output_name = file.substring(0, file.length() - 2);
				}
				if(file.equals("pack.mcmeta"))
				{
					final byte[] bytes = new JsonObject().add("pack", new JsonObject().add("pack_format", packFormat.id).add("description", packmeta.get("description").asString().replace("%mcversions%", packFormat.mcversions))).toString().getBytes();
					zip.putNextEntry(new ZipEntry(output_name));
					zip.write(bytes, 0, bytes.length);
					zip.closeEntry();
				}
				else
				{
					final String[] arr = output_name.split("/");
					String filename = arr[arr.length - 1];
					String foldername = output_name.substring(0, output_name.length() - filename.length());
					if(packFormat.id < PackFormat.latest.id)
					{
						if(foldername.equals("assets/minecraft/textures/block/") || foldername.equals("assets/minecraft/textures/item/"))
						{
							final String extensionless_name;
							final String extension;
							if(filename.endsWith(".png"))
							{
								extensionless_name = filename.substring(0, filename.length() - 4);
								extension = ".png";
							}
							else if(filename.endsWith(".png.mcmeta"))
							{
								extensionless_name = filename.substring(0, filename.length() - 11);
								extension = ".png.mcmeta";
							}
							else
							{
								extensionless_name = null;
								extension = null;
							}
							if(extensionless_name != null && textureNameConversions.containsKey(extensionless_name))
							{
								filename = textureNameConversions.get(extensionless_name) + extension;
								if(filename.equals(extension))
								{
									continue;
								}
							}
						}
						else if(filename.endsWith(".json"))
						{
							String extensionless_name = filename.substring(0, filename.length() - 5);
							if(foldername.equals("assets/minecraft/blockstates/"))
							{
								if(blockstateNameConversion.containsKey(extensionless_name))
								{
									filename = blockstateNameConversion.get(extensionless_name) + ".json";
									if(filename.equals(".json"))
									{
										continue;
									}
								}
							}
							else if(foldername.startsWith("assets/minecraft/models/") && modelNameConversions.containsKey(extensionless_name))
							{
								filename = modelNameConversions.get(extensionless_name) + ".json";
								if(filename.equals(".json"))
								{
									continue;
								}
							}
						}
						if(packFormat.id < 4)
						{
							if(packFormat.id == 1)
							{
								if(file.startsWith("assets/minecraft/textures/item/compass_"))
								{
									if(file.equals("assets/minecraft/textures/item/compass_00.png"))
									{
										BufferedImage img = new BufferedImage(16, 512, BufferedImage.TYPE_INT_ARGB);
										Graphics2D g = img.createGraphics();
										for(int i = 0; i < 32; i++)
										{
											String is = String.valueOf(i);
											if(is.length() == 1)
											{
												is = "0" + is;
											}
											g.drawImage(ImageIO.read(new File("assets/minecraft/textures/item/compass_" + is + ".png")), 0, i * 16, null);
										}
										g.dispose();
										zip.putNextEntry(new ZipEntry("assets/minecraft/textures/items/compass.png"));
										ImageIO.write(img, "png", zip);
										zip.closeEntry();
										final byte[] bytes = "{\"animation\":{}}".getBytes();
										zip.putNextEntry(new ZipEntry("assets/minecraft/textures/items/compass.png.mcmeta"));
										zip.write(bytes, 0, bytes.length);
										zip.closeEntry();
									}
									continue;
								}
								else if(file.startsWith("assets/minecraft/textures/item/clock_"))
								{
									if(file.equals("assets/minecraft/textures/item/clock_00.png"))
									{
										BufferedImage img = new BufferedImage(16, 1024, BufferedImage.TYPE_INT_ARGB);
										Graphics2D g = img.createGraphics();
										for(int i = 0; i < 64; i++)
										{
											String is = String.valueOf(i);
											if(is.length() == 1)
											{
												is = "0" + is;
											}
											g.drawImage(ImageIO.read(new File("assets/minecraft/textures/item/clock_" + is + ".png")), 0, i * 16, null);
										}
										g.dispose();
										zip.putNextEntry(new ZipEntry("assets/minecraft/textures/items/clock.png"));
										ImageIO.write(img, "png", zip);
										zip.closeEntry();
										final byte[] bytes = "{\"animation\":{}}".getBytes();
										zip.putNextEntry(new ZipEntry("assets/minecraft/textures/items/clock.png.mcmeta"));
										zip.write(bytes, 0, bytes.length);
										zip.closeEntry();
									}
									continue;
								}
							}
							if(output_name.equals("assets/minecraft/textures/particle/particles.png"))
							{
								zip.putNextEntry(new ZipEntry(output_name));
								ImageIO.write(ImageIO.read(new File(file)).getSubimage(0, 0, 128, 128), "png", zip);
								zip.closeEntry();
								continue;
							}
							if(foldername.equals("assets/minecraft/textures/block/"))
							{
								foldername = "assets/minecraft/textures/blocks/";
							}
							else if(foldername.equals("assets/minecraft/textures/item/"))
							{
								foldername = "assets/minecraft/textures/items/";
							}
							else if(foldername.startsWith("assets/minecraft/optifine/"))
							{
								foldername = "assets/minecraft/mcpatcher/" + foldername.substring(26);
							}
						}
						output_name = foldername + filename;
					}
					if(foldername.equals("assets/minecraft/blockstates/"))
					{
						JsonObject o = Json.parse(new FileReader(new File(file))).asObject();
						if(packFormat.id < 4)
						{
							for(JsonObject.Member member : o.get("variants").asObject())
							{
								JsonObject variant = member.getValue().asObject();
								if(variant.get("model") != null && variant.get("model").asString().startsWith("block/"))
								{
									String model = variant.get("model").asString().substring(6);
									variant.set("model", modelNameConversions.getOrDefault(model, model));
								}
							}
						}
						final byte[] bytes = o.toString().getBytes();
						zip.putNextEntry(new ZipEntry(output_name));
						zip.write(bytes, 0, bytes.length);
						zip.closeEntry();
					}
					else if(foldername.startsWith("assets/minecraft/models/"))
					{
						JsonObject o = Json.parse(new FileReader(new File(file))).asObject();
						if(packFormat.id < 4)
						{
							if(packFormat.id == 1 && o.get("parent") != null)
							{
								final String parent = o.get("parent").asString();
								if(modelPolyfills.contains(parent))
								{
									model_polyfill(parent, o);
									o.remove("parent");
								}
								else if(o.get("elements") != null)
								{
									logger.log(file + ": Models can't have `parent` and `elements` before 1.9.\n");
								}
							}
							JsonObject textures = o.get("textures").asObject();
							for(JsonObject.Member member : textures)
							{
								String path = member.getValue().asString();
								if(path.startsWith("block/"))
								{
									path = path.substring(6);
									if(textureNameConversions.containsKey(path))
									{
										path = "blocks/" + textureNameConversions.get(path);
									}
									else
									{
										path = "blocks/" + path;
									}
									textures.set(member.getName(), path);
								}
								else if(path.startsWith("item/"))
								{
									path = path.substring(5);
									if(textureNameConversions.containsKey(path))
									{
										path = "items/" + textureNameConversions.get(path);
									}
									else
									{
										path = "items/" + path;
									}
									textures.set(member.getName(), path);
								}
							}
						}
						final byte[] bytes = o.toString().getBytes();
						zip.putNextEntry(new ZipEntry(output_name));
						zip.write(bytes, 0, bytes.length);
						zip.closeEntry();
					}
					else
					{
						final byte[] bytes = Files.readAllBytes(new File(file).toPath());
						zip.putNextEntry(new ZipEntry(output_name));
						zip.write(bytes, 0, bytes.length);
						zip.closeEntry();
					}
				}
			}
			zip.close();
			res.put(packFormat, zipFile);
		}
		return res;
	}

	private static void model_polyfill(String name, JsonObject o)
	{
		final JsonObject display;
		if(o.get("display") == null)
		{
			display = new JsonObject();
			o.add("display", display);
		}
		else
		{
			display = o.get("display").asObject();
		}
		switch(name)
		{
			case "block/block":
				display.add("gui", new JsonObject()
						.add("rotation", new JsonArray().add(30).add(255).add(0))
						.add("translation", new JsonArray().add(0).add(0).add(0))
						.add("scale", new JsonArray().add(0.625).add(0.625).add(0.625))
				);
				display.add("ground", new JsonObject()
						.add("rotation", new JsonArray().add(0).add(0).add(0))
						.add("translation", new JsonArray().add(0).add(3).add(0))
						.add("scale", new JsonArray().add(0.25).add(0.25).add(0.25))
				);
				display.add("fixed", new JsonObject()
						.add("rotation", new JsonArray().add(0).add(0).add(0))
						.add("translation", new JsonArray().add(0).add(0).add(0))
						.add("scale", new JsonArray().add(0.5).add(0.5).add(0.5))
				);
				display.add("thirdperson", new JsonObject()
						.add("rotation", new JsonArray().add(75).add(45).add(0))
						.add("translation", new JsonArray().add(0).add(2.5).add(0))
						.add("scale", new JsonArray().add(0.375).add(0.375).add(0.375))
				);
				display.add("firstperson", new JsonObject()
						.add("rotation", new JsonArray().add(0).add(45).add(0))
						.add("translation", new JsonArray().add(0).add(0).add(0))
						.add("scale", new JsonArray().add(0.40).add(0.40).add(0.40))
				);
				break;

			case "block/thin_block":
				model_polyfill("block/block", o);
				display.set("thirdperson", new JsonObject()
						.add("rotation", new JsonArray().add(75).add(45).add(0))
						.add("translation", new JsonArray().add(0).add(2.5).add(2))
						.add("scale", new JsonArray().add(0.375).add(0.375).add(0.375))
				);
				display.set("firstperson", new JsonObject()
						.add("rotation", new JsonArray().add(0).add(45).add(0))
						.add("translation", new JsonArray().add(0).add(4.2).add(0))
						.add("scale", new JsonArray().add(0.40).add(0.40).add(0.40))
				);
				break;
		}
	}

	private static void recursivelyIndex(File folder)
	{
		final int offset = resourcePackFolder.getPath().length() + 1;
		for(File f : Objects.requireNonNull(folder.listFiles()))
		{
			if(f.isDirectory())
			{
				recursivelyIndex(f);
			}
			else if(!f.getName().equals("Thumbs.db"))
			{
				files.add(f.getPath().substring(offset).replace("\\", "/"));
			}
		}
	}
}
