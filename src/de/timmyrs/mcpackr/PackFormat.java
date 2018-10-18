package de.timmyrs.mcpackr;

public enum PackFormat
{
	V1(1, "1.6.1 - 1.8.9"),
	V2(2, "1.9 - 1.10.2"),
	V3(3, "1.11 - 1.12.2"),
	V4(4, "1.13 - 1.13.1");

	final public static PackFormat latest = PackFormat.V4;
	final public int id;
	final public String mcversions;

	PackFormat(int id, String mcversions)
	{
		this.id = id;
		this.mcversions = mcversions;
	}
}
