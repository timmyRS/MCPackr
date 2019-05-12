package de.timmyrs.mcpackr;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @deprecated Use {@link ResourcePack} instead.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class MCPackr
{
	/**
	 * Creates a version of a resource pack for each pack format.
	 *
	 * @param resourcePackFolder The base folder of the resource pack, which must contain a pack.mcmeta file.
	 * @param outputFolder       The folder where the zips should be generated in.
	 * @return The files that have been generated or empty on failure.
	 * @throws IOException When there are some unexpected errors with the file system.
	 */
	public static Map<PackFormat, File> packResourcePack(File resourcePackFolder, File outputFolder) throws IOException
	{
		return new ResourcePack(resourcePackFolder).pack(outputFolder);
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
		return new ResourcePack(resourcePackFolder).pack(outputFolder, outputFormat);
	}

	/**
	 * Creates a version of a resource pack for each given pack format.
	 *
	 * @param resourcePackFolder The base folder of the resource pack, which must contain a pack.mcmeta file.
	 * @param outputFolder       The folder where the zips should be generated in.
	 * @param outputFormats      An array of versions you'd like to the resource pack to be compatible with.
	 * @return The files that have been generated or empty on failure.
	 * @throws IOException When there are some unexpected errors with the file system.
	 */
	public static Map<PackFormat, File> packResourcePack(File resourcePackFolder, File outputFolder, List<PackFormat> outputFormats) throws IOException
	{
		return new ResourcePack(resourcePackFolder).pack(outputFolder, outputFormats);
	}
}
