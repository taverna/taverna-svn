package org.taverna.launcher.environment;

import java.util.List;

/**
 * Interface to the command line argument processor.
 * 
 * @author Donal Fellows
 */
public interface CommandLineArgumentProvider {
	/**
	 * Informs the runtime that this application has started sufficiently that
	 * there is no need to automatically print the help message and shut down.
	 * Should be called from the bundle that implements the core of the
	 * application.
	 */
	void markAsStarted();

	/**
	 * Consume an option argument plus parameters from the command line
	 * arguments. The option argument may exist at most once in the command line
	 * arguments.
	 * 
	 * @param name
	 *            The name of the option argument to consume. Must start with a
	 *            " <tt>-</tt>" character.
	 * @param parameters
	 *            How many additional parameters follow after the option
	 *            argument.
	 * @param help
	 *            Help string describing the use of the option argument. If
	 *            <tt>null</tt>, this option will not be described in the help
	 *            message.
	 * @return The list of parameters associated with the option argument, or
	 *         <tt>null</tt> if the option argument is absent. When the
	 *         <tt>parameters</tt> argument is zero, a present option is marked
	 *         by an empty list.
	 */
	List<String> consumeArgumentOnce(String name, int parameters, String help);

	/**
	 * Consume an option argument plus parameters from the command line
	 * arguments. The option argument may exist many times in the command line
	 * arguments.
	 * 
	 * @param name
	 *            The name of the option argument to consume. Must start with a
	 *            " <tt>-</tt>" character.
	 * @param parameters
	 *            How many additional parameters follow after the option
	 *            argument.
	 * @param help
	 *            Help string describing the use of the option argument. If
	 *            <tt>null</tt>, this option will not be described in the help
	 *            message.
	 * @return A list of lists of parameters associated with each argument, in
	 *         the order in which they exist on the command line. An empty outer
	 *         list indicates that the option argument is not present at all.
	 *         The inner lists should all be of the length given by the
	 *         <tt>parameters</tt> argument.
	 */
	List<List<String>> consumeArgumentMultiple(String name, int parameters,
			String help);

	/**
	 * Get all the remaining arguments.
	 * 
	 * @param template
	 *            Simple string for output at the start of the help text.
	 * @return The list of remaining arguments.
	 */
	List<String> getRemainingArguments(String template);

	/**
	 * Print the help message to {@link java.lang.System#out System.out}.
	 */
	void printHelp();
}
