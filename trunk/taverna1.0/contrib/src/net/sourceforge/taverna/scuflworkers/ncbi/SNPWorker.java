/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.Map;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * @author mfortner
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SNPWorker extends AbstractNCBIWorker {
	
	public SNPWorker(){
		this.startTag = "&lt;NSE-rs&gt;";
		this.endTag = "&lt;/NSE-rs&gt;";
		this.originalDb = "snp";
		this.queryKey = "13";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.taverna.scuflworkers.ncbi.AbstractNCBIWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

}
