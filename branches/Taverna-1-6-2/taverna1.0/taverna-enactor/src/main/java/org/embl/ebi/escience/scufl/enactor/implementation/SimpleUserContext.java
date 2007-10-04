/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor.implementation;

import org.embl.ebi.escience.scufl.enactor.UserContext;

import uk.ac.soton.itinnovation.freefluo.main.FlowContext;

/**
 * A simple implementation of the UserContext interface that is configured by
 * parameters in its constructor and is subsequently immutable. No security
 * properties are contained by this context implementation.
 * 
 * @author Tom Oinn
 */
public class SimpleUserContext implements UserContext {

	private String personLSID, organizationLSID, experimentDesignLSID;

	private String username = "";

	private String password = "";

	private static final String PERSON_KEY = "personLSID";

	private static final String ORG_KEY = "organizationLSID";

	private static final String EXP_KEY = "experimentLSID";

	private static final String USERNAME_KEY = "username";

	private static final String PASSWORD_KEY = "password";

	public SimpleUserContext(String personLSID, String organisationLSID,
			String experimentDesignLSID, String username, String password) {
		this(personLSID, organisationLSID, experimentDesignLSID);
		this.username = username;
		this.password = password;
	}

	public SimpleUserContext(String personLSID, String organizationLSID,
			String experimentDesignLSID) {
		this.personLSID = personLSID;
		this.organizationLSID = organizationLSID;
		this.experimentDesignLSID = experimentDesignLSID;
	}

	public SimpleUserContext(FlowContext flowContext) {
		this.personLSID = flowContext.get(PERSON_KEY);
		this.organizationLSID = flowContext.get(ORG_KEY);
		this.experimentDesignLSID = flowContext.get(EXP_KEY);
		this.username = flowContext.get(USERNAME_KEY);
		this.password = flowContext.get(PASSWORD_KEY);
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
		return new String[] { username, password };
	}

	public FlowContext toFlowContext() {
		FlowContext flowContext = new FlowContext();
		flowContext.put(PERSON_KEY, personLSID);
		flowContext.put(ORG_KEY, organizationLSID);
		flowContext.put(EXP_KEY, experimentDesignLSID);
		flowContext.put(USERNAME_KEY, username);
		flowContext.put(PASSWORD_KEY, password);
		return flowContext;
	}
}
