/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import uk.ac.soton.itinnovation.freefluo.main.FlowContext;

/**
 * Represents a user workflow context. This allows the workflow engine to which
 * the context is passed to act on behalf of the user, accessing protected
 * resources and informing provenance generation
 * 
 * @author Tom Oinn
 */
public interface UserContext {

	// public String getUser();
	public String getPersonLSID();

	// public String getOrganisation();
	public String getOrganizationLSID();

	public String getExperimentDesignLSID();

	// public String getProject();
	// public String getExperiment();
	public String[] getUserNameAndPassword(String resourceExpression);

	FlowContext toFlowContext();

}
