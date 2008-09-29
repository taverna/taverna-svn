/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import org.embl.ebi.escience.scufl.enactor.*;

/**
 * A simple implementation of the UserContext interface
 * that is configured by parameters in its constructor
 * and is subsequently immutable. No security properties
 * are contained by this context implementation.
 * @author Tom Oinn
 */
public class SimpleUserContext implements UserContext {
    
    private String personLSID, organizationLSID, experimentDesignLSID;

    public SimpleUserContext(String personLSID,
			     String organizationLSID,
			     String experimentDesignLSID) {
	this.personLSID = personLSID;
	this.organizationLSID = organizationLSID;
	this.experimentDesignLSID = experimentDesignLSID;
    }
    
    public String getPersonLSID() {
	return this.personLSID;
    }

    public String getOrganizationLSID() {
	return this.organizationLSID;
    }

    public String getExperimentDesignLSID() {
	return this.experimentDesignLSID;
    }

    public String[] getUserNameAndPassword(String resourceExpression) {
	return new String[]{"",""};
    }
    
}
