/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-11 15:08:48 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * The scavanger for abstract processors
 * 
 * @author Ingo Wassink
 */
public class APScavenger extends Scavenger {
	private static final long serialVersionUID = -6186599168134511411L;

	/**
	 * Constructor for creating the RshellScavenger
	 * 
	 * @throws ScavengerCreationException
	 */
	public APScavenger() throws ScavengerCreationException {
		super(new APProcessorFactory());
	}

}
