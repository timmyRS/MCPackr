package de.timmyrs.mcpackr;

import java.io.File;
import java.io.IOException;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		final File workingDirectory = new File(System.getProperty("user.dir"));
		new ResourcePack(workingDirectory).pack(workingDirectory);
	}
}
