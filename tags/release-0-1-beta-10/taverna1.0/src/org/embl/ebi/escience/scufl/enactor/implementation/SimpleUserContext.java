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
    
    private String user, project, organisation, experiment;

    public SimpleUserContext(String userID,
			     String project,
			     String organisation,
			     String experiment) {
	this.user = userID;
	this.project = project;
	this.organisation = organisation;
	this.experiment = experiment;
    }
    
    public String getUser() {
	return this.user;
    }

    public String getProject() {
	return this.project;
    }

    public String getOrganisation() {
	return this.organisation;
    }

    public String getExperiment() {
	return this.experiment;
    }

    public String[] getUserNameAndPassword(String resourceExpression) {
	return new String[]{"",""};
    }
    
}
