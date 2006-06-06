/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import java.util.Properties;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;

/**
 * A processor that uses the BSF scripting engine to allow arbitrary scripts to
 * be invoked on workflow data. In this implementation all inputs and outputs
 * are strings, if this becomes a serious issue I guess we can change it but for
 * now this will do.
 * 
 * @author mfortner
 */
public class BSFProcessor extends Processor implements java.io.Serializable {

    private String theScript = "";
	private String language = "";

    /**
     * Construct a new processor with the given model and name, delegates to the
     * superclass and sets the script up correctly along with the input and
     * output port name arrays.
     */
    public BSFProcessor(ScuflModel model, String name, String script,
            String[] inputs, String[] outputs)
            throws ProcessorCreationException, DuplicateProcessorNameException {
        super(model, name);
        // Create appropriate inputs and outputs from the arrays
        try {
            this.theScript = script;
            // Iterate over inputs...
            for (int i = 0; i < inputs.length; i++) {
                Port p = new InputPort(this, inputs[i]);
                p.setSyntacticType("'text/plain'");
                addPort(p);
            }
            // Iterate over outputs
            for (int i = 0; i < outputs.length; i++) {
                Port p = new OutputPort(this, outputs[i]);
                p.setSyntacticType("'text/plain'");
                addPort(p);
            }
        } catch (DuplicatePortNameException dpne) {
            throw new ProcessorCreationException(
                    "The supplied specification for the BSF script processor '"
                            + name + "' contained a duplicate port '"
                            + dpne.getMessage() + "'");
        } catch (PortCreationException pce) {
            throw new ProcessorCreationException(
                    "An error occured whilst generating ports for the BSF processor "
                            + pce.getMessage());
        }
    }

    /**
     * Set the script
     */
    public void setScript(String theScript) {
        if (theScript != null) {
            this.theScript = theScript;
        } else {
            this.theScript = "";
        }
        fireModelEvent(new ScuflModelEvent(this, "Script modified"));
    }

    /**
     * Get the script
     */
    public String getScript() {
        return this.theScript;
    }
	
	

    /**
	 * @return Returns the language.
	 */
	public String getLanguage() {
		return language;
	}
	

	/**
	 * @param language The language to set.
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	

	/**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
        Properties props = new Properties();
        props.put("Script", "See configurator for more information");
        return props;
    }

}