package de.timmyrs.mcpackr;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess", "unused"})
public class MCPackr
{
	public static void main(String[] args) throws IOException
	{
		final File workingDirectory = new File(System.getProperty("user.dir"));
		packResourcePack(workingDirectory, workingDirectory);
	}

	/**
	 * Creates a version of a resource pack for each pack format.
	 *
	 * @param resourcePackFolder The base folder of the resource pack, which should include a pack.mcmeta file.
	 * @param outputFolder       The folder where the zips should be generated in.
	 * @return The files that have been generated or empty on failure.
	 * @throws IOException When there are some unexpected errors with the file system.
	 */
	public static Map<PackFormat, File> packResourcePack(File resourcePackFolder, File outputFolder) throws IOException
	{
		return packResourcePack(resourcePackFolder, outputFolder, Arrays.asList(PackFormat.values()));
	}

	/**
	 * Creates a version of a resource pack for the given pack format.
	 *
	 * @param resourcePackFolder The base folder of the resource pack, which must contain a pack.mcmeta file.
	 * @param outputFolder       The folder where the zips should be generated in.
	 * @return The file that has been generated or null on failure.
	 * @throws IOException When there are some unexpected errors with the file system.
	 */
	public static File packResourcePack(File resourcePackFolder, File outputFolder, PackFormat outputFormat) throws IOException
	{
		return packResourcePack(resourcePackFolder, outputFolder, Collections.singletonList(outputFormat)).get(outputFormat);
	}

	/**
	 * Creates a version of a resource pack for each given pack format.
	 *
	 * @param resourcePackFolder The base folder of the resource pack, which should include a pack.mcmeta file.
	 * @param outputFolder       The folder where the zips should be generated in.
	 * @param outputFormats      An array of versions you'd like to the resource pack to be compatible with.
	 * @return The files that have been generated or empty on failure.
	 * @throws IOException When there are some unexpected errors with the file system.
	 */
	public static Map<PackFormat, File> packResourcePack(File resourcePackFolder, File outputFolder, List<PackFormat> outputFormats) throws IOException
	{
		final Logger logger = LoggerFactory.getLogger(MCPackr.class);
		final HashMap<PackFormat, File> res = new HashMap<>();
		final File packmetaFile = new File(resourcePackFolder.getPath() + "/pack.mcmeta");
		final JsonObject packmeta;
		try
		{
			packmeta = Json.parse(new FileReader(packmetaFile)).asObject().get("pack").asObject();
		}
		catch(FileNotFoundException e)
		{
			logger.error("The resource pack is missing the pack.mcmeta file.");
			return res;
		}
		final int sourcePackFormat = packmeta.get("pack_format").asInt();
		final File resourcesRoot = new File(resourcePackFolder.getPath() + "/assets/minecraft");
		if(!resourcesRoot.exists() || !resourcesRoot.isDirectory())
		{
			logger.error("The resource pack is missing the `assets/minecraft/` folder.");
			return res;
		}
		final String packName = resourcePackFolder.getName();
		for(PackFormat packFormat : PackFormat.values())
		{
			final File outputFile = new File(outputFolder.getPath() + "/" + packName + " (" + packFormat.mcversions + ").zip");
			if(outputFile.exists() && outputFile.isFile() && !outputFile.delete())
			{
				logger.error("Failed to delete " + outputFile.getPath());
				return res;
			}
		}
		logger.info("Indexing resource pack...");
		final ArrayList<String> files = recursivelyIndex(resourcePackFolder, resourcePackFolder.getPath().length() + 1);
		final ArrayList<String> complaints = new ArrayList<>();
		final HashMap<Integer, ArrayList<String>> versions = new HashMap<>();
		for(PackFormat packFormat : PackFormat.values())
		{
			versions.put(packFormat.id, new ArrayList<String>());
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
		final String fromBlocksDir = (sourcePackFormat < 4 ? "blocks/" : "block/");
		final String fromItemsDir = (sourcePackFormat < 4 ? "items/" : "item/");
		for(PackFormat packFormat : outputFormats)
		{
			logger.info("Creating " + packFormat.mcversions + " version...");
			final ConversionTables ct = ConversionTables.get(sourcePackFormat, packFormat.id);
			final File zipFile = new File(resourcePackFolder + "/" + packName + " (" + packFormat.mcversions + ").zip");
			final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
			final String toBlocksDir = (packFormat.id < 4 ? "blocks/" : "block/");
			final String toItemsDir = (packFormat.id < 4 ? "items/" : "item/");
			for(String file : versions.get(packFormat.id))
			{
				String output_name = file;
				final boolean isVersionSpecific = file.substring(file.length() - 2, file.length() - 1).equals("@");
				if(isVersionSpecific)
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
					String dirname = output_name.substring(0, output_name.length() - filename.length());
					if(dirname.equals("assets/minecraft/textures/" + fromBlocksDir) || dirname.equals("assets/minecraft/textures/" + fromItemsDir))
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
						if(extensionless_name != null && ct.textures.containsKey(extensionless_name))
						{
							filename = ct.textures.get(extensionless_name) + extension;
							if(filename.equals(extension))
							{
								continue;
							}
						}
						if(dirname.equals("assets/minecraft/textures/" + fromBlocksDir))
						{
							dirname = "assets/minecraft/textures/" + toBlocksDir;
						}
						else
						{
							dirname = "assets/minecraft/textures/" + toItemsDir;
						}
					}
					else if(filename.endsWith(".json"))
					{
						String extensionless_name = filename.substring(0, filename.length() - 5);
						if(dirname.equals("assets/minecraft/blockstates/"))
						{
							if(ct.blockstates.containsKey(extensionless_name))
							{
								filename = ct.blockstates.get(extensionless_name) + ".json";
								if(filename.equals(".json"))
								{
									continue;
								}
							}
						}
						else if(dirname.startsWith("assets/minecraft/models/") && ct.models.containsKey(extensionless_name))
						{
							filename = ct.models.get(extensionless_name) + ".json";
							if(filename.equals(".json"))
							{
								continue;
							}
						}
					}
					if(packFormat.id == 1)
					{
						if(file.startsWith("assets/minecraft/textures/" + fromItemsDir + "compass_"))
						{
							if(file.equals("assets/minecraft/textures/" + fromItemsDir + "compass_00.png"))
							{
								final BufferedImage img = new BufferedImage(16, 512, BufferedImage.TYPE_INT_ARGB);
								final Graphics2D g = img.createGraphics();
								for(int i = 0; i < 32; i++)
								{
									String is = String.valueOf(i);
									if(is.length() == 1)
									{
										is = "0" + is;
									}
									g.drawImage(ImageIO.read(new File("assets/minecraft/textures/" + fromItemsDir + "compass_" + is + ".png")), 0, i * 16, null);
								}
								g.dispose();
								zip.putNextEntry(new ZipEntry("assets/minecraft/textures/" + toItemsDir + "compass.png"));
								ImageIO.write(img, "png", zip);
								zip.closeEntry();
								final byte[] bytes = "{\"animation\":{}}".getBytes();
								zip.putNextEntry(new ZipEntry("assets/minecraft/textures/" + toItemsDir + "compass.png.mcmeta"));
								zip.write(bytes, 0, bytes.length);
								zip.closeEntry();
							}
							continue;
						}
						else if(file.startsWith("assets/minecraft/textures/item/clock_"))
						{
							if(file.equals("assets/minecraft/textures/item/clock_00.png"))
							{
								final BufferedImage img = new BufferedImage(16, 1024, BufferedImage.TYPE_INT_ARGB);
								final Graphics2D g = img.createGraphics();
								for(int i = 0; i < 64; i++)
								{
									String is = String.valueOf(i);
									if(is.length() == 1)
									{
										is = "0" + is;
									}
									g.drawImage(ImageIO.read(new File("assets/minecraft/textures/" + fromItemsDir + "clock_" + is + ".png")), 0, i * 16, null);
								}
								g.dispose();
								zip.putNextEntry(new ZipEntry("assets/minecraft/textures/" + toItemsDir + "clock.png"));
								ImageIO.write(img, "png", zip);
								zip.closeEntry();
								final byte[] bytes = "{\"animation\":{}}".getBytes();
								zip.putNextEntry(new ZipEntry("assets/minecraft/textures/" + toItemsDir + "clock.png.mcmeta"));
								zip.write(bytes, 0, bytes.length);
								zip.closeEntry();
							}
							continue;
						}
					}
					else
					{
						if(file.equals("assets/minecraft/textures/" + fromItemsDir + "compass.png.mcmeta") || file.equals("assets/minecraft/textures/" + fromItemsDir + "clock.png.mcmeta"))
						{
							continue;
						}
						else if(file.equals("assets/minecraft/textures/" + fromItemsDir + "compass.png"))
						{
							final BufferedImage img = ImageIO.read(new File("assets/minecraft/textures/" + fromItemsDir + "compass.png"));
							for(int i = 0; i < 32; i++)
							{
								final BufferedImage img_ = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
								final Graphics2D g = img_.createGraphics();
								g.drawImage(img.getSubimage(0, i * 16, 16, 16), 0, 0, null);
								g.dispose();
								zip.putNextEntry(new ZipEntry("assets/minecraft/textures/" + toItemsDir + "compass_" + twoDigitNumberString(i) + ".png"));
								ImageIO.write(img_, "png", zip);
								zip.closeEntry();
							}
							continue;
						}
						else if(file.equals("assets/minecraft/textures/" + fromItemsDir + "clock.png"))
						{
							final BufferedImage img = ImageIO.read(new File("assets/minecraft/textures/" + fromItemsDir + "clock.png"));
							for(int i = 0; i < 64; i++)
							{
								final BufferedImage img_ = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
								final Graphics2D g = img_.createGraphics();
								g.drawImage(img.getSubimage(0, i * 16, 16, 16), 0, 0, null);
								g.dispose();
								zip.putNextEntry(new ZipEntry("assets/minecraft/textures/" + toItemsDir + "clock_" + twoDigitNumberString(i) + ".png"));
								ImageIO.write(img_, "png", zip);
								zip.closeEntry();
							}
							continue;
						}
					}
					if(sourcePackFormat >= 4 && packFormat.id < 4)
					{
						if(output_name.equals("assets/minecraft/textures/particle/particles.png"))
						{
							zip.putNextEntry(new ZipEntry(output_name));
							ImageIO.write(ImageIO.read(new File(file)).getSubimage(0, 0, 128, 128), "png", zip);
							zip.closeEntry();
							continue;
						}
						if(dirname.startsWith("assets/minecraft/optifine/"))
						{
							dirname = "assets/minecraft/mcpatcher/" + dirname.substring(26);
						}
					}
					else if(sourcePackFormat < 4 && packFormat.id >= 4)
					{
						if(!isVersionSpecific && output_name.equals("assets/minecraft/textures/particle/particles.png"))
						{
							complain(complaints, output_name + ": will not be present in 1.13+ ports. Either create a version-specific file or upgrade your resource pack to 1.13+.");
							continue;
						}
						if(dirname.startsWith("assets/minecraft/mcpatcher/"))
						{
							dirname = "assets/minecraft/optifine/" + dirname.substring(27);
						}
					}
					output_name = dirname + filename;
					if(dirname.equals("assets/minecraft/blockstates/"))
					{
						final JsonObject o = Json.parse(new FileReader(new File(file))).asObject();
						final JsonObject variants = o.get("variants").asObject();
						for(JsonObject.Member member : variants)
						{
							final JsonArray _value;
							if(member.getValue().isArray())
							{
								_value = member.getValue().asArray();
							}
							else if(member.getValue().isObject())
							{
								_value = new JsonArray().add(member.getValue().asObject());
							}
							else
							{
								complain(complaints, output_name + ": Variant \"" + member.getName() + "\" is of an invalid type.");
								continue;
							}
							final JsonArray value = new JsonArray();
							for(JsonValue _props : _value.values())
							{
								if(!_props.isObject())
								{
									continue;
								}
								final JsonObject props = _props.asObject();
								String model = null;
								if(sourcePackFormat >= 4)
								{
									if(props.get("model") != null && props.get("model").asString().startsWith(fromBlocksDir))
									{
										model = props.get("model").asString().substring(fromBlocksDir.length());
									}
								}
								else if(props.get("model") != null)
								{
									model = props.get("model").asString();
								}
								if(model != null)
								{
									if(packFormat.id >= 4)
									{
										if(ct.models.containsKey(model))
										{
											props.set("model", toBlocksDir + ct.models.get(model));
										}
										else
										{
											props.set("model", toBlocksDir + model);
										}
									}
									else
									{
										if(ct.models.containsKey(model))
										{
											props.set("model", ct.models.get(model));
										}
										else
										{
											props.set("model", model);
										}
									}
								}
								value.add(props);
							}
							variants.set(member.getName(), value);
						}
						final byte[] bytes = o.toString().getBytes();
						zip.putNextEntry(new ZipEntry(output_name));
						zip.write(bytes, 0, bytes.length);
						zip.closeEntry();
					}
					else if(dirname.startsWith("assets/minecraft/models/"))
					{
						final JsonObject o = Json.parse(new FileReader(new File(file))).asObject();
						if(packFormat.id == 1 && o.get("parent") != null && o.get("elements") != null)
						{
							o.remove("parent");
						}
						if(o.get("textures") != null)
						{
							final JsonObject textures = o.get("textures").asObject();
							for(JsonObject.Member member : textures)
							{
								String path = member.getValue().asString();
								if(path.startsWith(fromBlocksDir))
								{
									path = path.substring(fromBlocksDir.length());
									if(ct.textures.containsKey(path))
									{
										path = toBlocksDir + ct.textures.get(path);
									}
									else
									{
										path = toBlocksDir + path;
									}
									textures.set(member.getName(), path);
								}
								else if(path.startsWith(fromItemsDir))
								{
									path = path.substring(fromBlocksDir.length());
									if(ct.textures.containsKey(path))
									{
										path = toItemsDir + ct.textures.get(path);
									}
									else
									{
										path = toItemsDir + path;
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
		if(complaints.size() > 0)
		{
			logger.info("The resource pack has been ported. However, there are some complaints:");
			for(String complaint : complaints)
			{
				logger.warn(complaint);
			}
		}
		else
		{
			logger.info("The resource pack has successfully been ported.");
		}
		return res;
	}

	private static void complain(ArrayList<String> complaints, String complaint)
	{
		if(!complaints.contains(complaint))
		{
			complaints.add(complaint);
		}
	}

	private static String twoDigitNumberString(int i)
	{
		String str = String.valueOf(i);
		if(str.length() < 2)
		{
			return "0" + str;
		}
		return str;
	}

	private static ArrayList<String> recursivelyIndex(File folder, int offset)
	{
		final ArrayList<String> files = new ArrayList<>();
		for(File f : Objects.requireNonNull(folder.listFiles()))
		{
			if(f.isDirectory())
			{
				files.addAll(recursivelyIndex(f, offset));
			}
			else if(!f.getName().equals("Thumbs.db"))
			{
				files.add(f.getPath().substring(offset).replace("\\", "/"));
			}
		}
		return files;
	}
}
