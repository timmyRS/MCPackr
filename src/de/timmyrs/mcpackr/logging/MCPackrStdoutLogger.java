package de.timmyrs.mcpackr.logging;

/**
 * A logger logging System.out.print.
 */
public class MCPackrStdoutLogger extends MCPackrLogger
{
	@Override
	public void log(String message)
	{
		System.out.print(message);
	}
}
