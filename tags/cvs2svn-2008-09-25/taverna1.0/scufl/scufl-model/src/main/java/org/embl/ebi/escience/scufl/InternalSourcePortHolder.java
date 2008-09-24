package org.embl.ebi.escience.scufl;

import java.util.Properties;

/**
 * A Processor subclass to hold ports for the overal workflow inputs. These
 * ports are therefore output ports, as they are used as data sources for links
 * into the workflow
 */
public class InternalSourcePortHolder extends Processor {
	protected InternalSourcePortHolder(ScuflModel model)
		throws DuplicateProcessorNameException, ProcessorCreationException {
		super(model, "SCUFL_INTERNAL_SOURCEPORTS");
	}

	public Properties getProperties() {
		return null;
	}
}