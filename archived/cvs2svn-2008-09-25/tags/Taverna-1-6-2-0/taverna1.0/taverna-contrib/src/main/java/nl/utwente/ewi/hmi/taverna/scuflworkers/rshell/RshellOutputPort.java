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
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;

/**
 * Output port for an Rshell
 * 
 * @author Ingo Wassink
 */
public class RshellOutputPort extends OutputPort {

	private static final long serialVersionUID = -3714649331013542345L;

	private SymanticTypes symanticType;

	/**
	 * Constructor
	 * 
	 * @param processor
	 *            the parent processor
	 * @param name
	 *            the name of the output port
	 * @throws DuplicatePortNameException
	 * @throws PortCreationException
	 */
	public RshellOutputPort(Processor processor, String name)
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
