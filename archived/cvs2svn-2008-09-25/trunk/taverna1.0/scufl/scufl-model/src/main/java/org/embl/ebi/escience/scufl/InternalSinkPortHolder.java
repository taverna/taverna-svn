package org.embl.ebi.escience.scufl;

import java.util.Properties;

/**
 * A Processor subclass to hold ports for the overall workflow outputs, these
 * ports are therefore held as input ports, acting as they do as data sinks.
 */
public class InternalSinkPortHolder extends Processor {

	protected InternalSinkPortHolder(ScuflModel model)
		throws DuplicateProcessorNameException, ProcessorCreationException {
		super(model, "SCUFL_INTERNAL_SINKPORTS");
	}

	public Properties getProperties() {
		return null;
	}
}
