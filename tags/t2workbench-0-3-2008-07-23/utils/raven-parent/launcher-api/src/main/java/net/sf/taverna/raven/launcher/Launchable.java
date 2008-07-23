package net.sf.taverna.raven.launcher;


/**
 * A class that can be launched through a launch(String[]) method.
 * <p>
 * (Similar to a static main(String[])).
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public interface Launchable {

	/**
	 * Execute program, similar to in a main(String[])
	 * 
	 * @param args 0 or more string argument
	 * @return System exit code (caller will run {@link System#exit(int)}
	 * @throws Exception If anything went wrong
	 */
	public int launch(String[] args) throws Exception;
}
