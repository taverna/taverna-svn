/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.util.List;
import java.util.Properties;

/**
 * Interface for processor implementations
 * 
 * @author Tom Oinn
 */
public interface IProcessor {

	/**
	 * A string representing a resource location within the enactor JVM. This is
	 * the case, for example, for the simple string operations and the local
	 * service processors.
	 */
	public static String ENACTOR = "Local to enactor";

	/**
	 * Is this processor boring? If so it shouldn't show up by default in the
	 * status display
	 */
	public boolean isBoring();

	/**
	 * Set whether this processor is boring
	 */
	public void setBoring(boolean boring);

	/**
	 * Allow subclasses to intercept requests to go offline and online, allows
	 * e.g. the workflow processor to delegate this to the child workflow
	 */
	public void setOnline();

	public void setOffline();

	/**
	 * Check for a breakpoint
	 */
	public boolean hasBreakpoint();

	/**
	 * Add a breakpoint to the processor
	 */
	public void addBreakpoint();

	/**
	 * Remove a breakpoint to the processor
	 */
	public void rmvBreakpoint();

	/**
	 * If meaningful, obtain the host that the resource is based in. Of course,
	 * not all processor implementations are service based so this may return
	 * the string Processor.ENACTOR instead to represent this, and this is the
	 * default.
	 */
	public String getResourceHost();

	/**
	 * Return the maximum number of task implementations that should be launched
	 * for this processor. This is ignored unless the processor is being
	 * iterated over in which case a number of threads up to the value specified
	 * are created to process the information. You must be particularly careful
	 * that the task implementation is thread safe before changing this from the
	 * default specified here, at the current time the web service processor is
	 * not thread safe!
	 */
	public int getMaximumWorkers();

	/**
	 * Return the default number of workers for an instance of this processor
	 * type
	 */
	public int getDefaultWorkers();

	/**
	 * Return the number of workers for this instance
	 */
	public int getWorkers();

	/**
	 * Set the number of workers
	 */
	public void setWorkers(int workers);

	/**
	 * Set the name, providing that names doesn't exist within the current
	 * workflow that this processor is bound to. If it does then do nothing
	 */
	public void setName(String newName);

	/**
	 * Get the iteration strategy for this processor, or null if the default
	 * should be used
	 */
	public IterationStrategy getIterationStrategy();

	/**
	 * Set the iteration strategy
	 */
	public void setIterationStrategy(IterationStrategy i);

	/**
	 * Return an array containing all annotation templates for this processor
	 */
	public AnnotationTemplate[] getAnnotationTemplates();

	/**
	 * Add an annotation template to this processor
	 */
	public void addAnnotationTemplate(AnnotationTemplate theTemplate);

	/**
	 * Remove an annotation template from this processor
	 */
	public void removeAnnotationTemplate(AnnotationTemplate theTemplate);

	/**
	 * Create a standard annotation template for each pair of bound input /
	 * output ports, mostly just to test the metadata store and browser
	 * functionality. Removes any existing template definitions!
	 */
	public AnnotationTemplate[] defaultAnnotationTemplates();

	/**
	 * Return the list of AlternateProcessor holders for this primary processor
	 * implementation.
	 */
	public AlternateProcessor[] getAlternatesArray();

	/**
	 * Return the alternates list object to allow addition or reordering of
	 * alternate processors
	 */
	public List getAlternatesList();

	/**
	 * Add an alternate processor to this processor definition
	 */
	public void addAlternate(AlternateProcessor ap);

	/**
	 * Delete an alternate processor from this processor definition
	 */
	public void removeAlternate(AlternateProcessor ap);

	/**
	 * Return the time in milliseconds after which an instance of this processor
	 * should be regarded as having failed with a timeout. If this value is set
	 * to zero then no timeout applies.
	 */
	public int getTimeout();

	/**
	 * Set the timeout parameter
	 */
	public void setTimeout(int timeout);

	/**
	 * Return the number of retries after the initial invocation attempt. If set
	 * to zero then retry behaviour is disabled.
	 */
	public int getRetries();

	/**
	 * Set the number of retries
	 */
	public void setRetries(int retries);

	/**
	 * Get the number of milliseconds to wait before first retrying an
	 * invocation of the task this processor represents.
	 */
	public int getRetryDelay();

	/**
	 * Set the retry delay
	 */
	public void setRetryDelay(int delay);

	/**
	 * Return the factor by which the timeout value will be multiplied for each
	 * retry after the first. This allows for exponential backoff from failing
	 * service instances.
	 */
	public double getBackoff();

	/**
	 * Set the backoff factor
	 */
	public void setBackoff(double backoff);

	/**
	 * Get whether the Processor is critical. This is true if its execution
	 * failure will cause its containing workflow to fail.
	 */
	public boolean getCritical();

	/**
	 * Set whether the Processor is critical. When a critical Processor's
	 * execution fails, its containing workflow is halted and fails. In
	 * contrast, the containing workflow will ignore failures of non-critical
	 * Processors and will continue to run.
	 */
	public void setCritical(boolean critical);

	/**
	 * Get the log level, this is the effective log level of the processor
	 * taking into account possible inheritence of the level from the ScuflModel
	 * instance.
	 */
	public int getLogLevel();

	/**
	 * Get the real log level set by this processor, this can be -1 in which
	 * case the getLogLevel method will return the log level of the ScuflModel
	 * that 'owns' this processor
	 */
	public int getRealLogLevel();

	/**
	 * Set the log level
	 */
	public void setLogLevel(int level);

	/**
	 * Return a properties object containing the processor specific properties
	 * for this processor type instance. This is used by the user interface code
	 * to display additional properties for each processor and should be
	 * implemented by the subclasses to display useful information.
	 * 
	 * @return a Properties instance containing all processor-specific
	 *         properties, or optinally null if there are none
	 */
	public Properties getProperties();

	/**
	 * Get the name for this processor. There is no corresponding set method
	 * because names are immutable once created.
	 */
	public String getName();

	/**
	 * Get a description of the processor.
	 */
	public String getDescription();

	/**
	 * Set the description for the processor.
	 */
	public void setDescription(String the_description);

	/**
	 * Get an array of the ports, input or output, defined within this
	 * processor.
	 */
	public Port[] getPorts();

	/**
	 * Get an array of the input ports that are bound by data constraints
	 * defined within this processor
	 */
	public InputPort[] getBoundInputPorts();

	/**
	 * Get an array of all the output ports that are bound by data constraints
	 * and defined within this processor
	 */
	public OutputPort[] getBoundOutputPorts();

	/**
	 * Find a particular named port
	 */
	public Port locatePort(String port_name) throws UnknownPortException;

	/**
	 * Find a particular named port, input port if boolean flag is true
	 */
	public Port locatePort(String port_name, boolean isInputPort)
			throws UnknownPortException;

	/**
	 * Get an array containing only input ports
	 */
	public InputPort[] getInputPorts();

	/**
	 * Get an array containing only output ports
	 */
	public OutputPort[] getOutputPorts();

	/**
	 * Add a new port to this processor
	 */
	public void addPort(Port the_port);

	/**
	 * Remove a port from a processor (only really applicable to the workflow
	 * source and sink ports, so be careful when you're using it)
	 */
	public void removePort(Port the_port);

	/**
	 * Get the parent model
	 */
	public ScuflModel getModel();

	/**
	 * Fire a change event back to the model
	 */
	public void fireModelEvent(ScuflModelEvent event);

	/**
	 * Are we in offline mode?
	 */
	public boolean isOffline();

}
