/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellPortTypes.SymanticTypes;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;

/**
 * InputPort specialization for Rserv processor.
 * 
 * Add support for setting the desired javaType on input, so as to convert
 * inputs on workflow invocation.
 * 
 * The syntactic type will be set to l('text/plain') for all but REXP, which
 * will be text/plain.
 * 
 * @author Stian Soiland, Ingo Wassink
 * 
 */
public class RshellInputPort extends InputPort {

	private static final long serialVersionUID = 6454564758308556076L;

	private SymanticTypes symanticType;

	/**
	 * Constructor for input port
	 * 
	 * @param processor
	 *            the processor to which the input ports belongs
	 * @param name
	 *            the name of the input port
	 * @throws DuplicatePortNameException
	 * @throws PortCreationException
	 */
	public RshellInputPort(Processor processor, String name)
			throws DuplicatePortNameException, PortCreationException {
		super(processor, name);

		this.setSymanticType(SymanticTypes.REXP);
	}

	/**
	 * Method for setting the symantic type
	 * 
	 * @param semanticType
	 *            the new value
	 */
	public void setSymanticType(SymanticTypes symanticType) {
		this.symanticType = symanticType;
		this.setSyntacticType(symanticType.syntacticType);
	}

	/**
	 * Method for getting the symantic type
	 * 
	 * @return the symantic type
	 */
	public SymanticTypes getSymanticType() {
		return symanticType;
	}
}
