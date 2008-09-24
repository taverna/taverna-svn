/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that knows how to create Rserv processors
 * 
 * @author Stian Soiland, Ingo Wassink
 */
public class RshellScavenger extends Scavenger {

	private static final long serialVersionUID = 1258935955566995997L;

	/**
	 * Constructor for creating the RshellScavenger
	 * 
	 * @throws ScavengerCreationException
	 */
	public RshellScavenger() throws ScavengerCreationException {
		super(new RshellProcessorFactory());
	}
}
