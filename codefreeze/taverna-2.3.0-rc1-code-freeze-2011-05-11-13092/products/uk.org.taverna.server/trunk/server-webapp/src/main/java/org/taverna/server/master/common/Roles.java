package org.taverna.server.master.common;

/**
 * The roles defined in this webapp.
 * 
 * @author Donal Fellows
 */
public interface Roles {
	/** The role of a normal user. */
	static final String USER = "ROLE_tavernauser";
	/**
	 * The role of an administrator. Administrators <i>should</i> have the
	 * normal user role as well.
	 */
	static final String ADMIN = "ROLE_tavernasuperuser";
}
