/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.interfaces;

import java.security.Principal;

import org.taverna.server.master.common.Credential;
import org.taverna.server.master.common.Trust;
import org.taverna.server.master.exceptions.InvalidCredentialException;

/**
 * Security context for a workflow run.
 * 
 * @author Donal Fellows
 */
public interface TavernaSecurityContext {
	/**
	 * @return Who owns the security context.
	 */
	Principal getOwner();

	/**
	 * @return The credentials owned by the user. Never <tt>null</tt>.
	 */
	Credential[] getCredentials();

	/**
	 * Add a credential to the owned set or replaces the old version with the
	 * new one.
	 * 
	 * @param toAdd
	 *            The credential to add.
	 */
	void addCredential(Credential toAdd);

	/**
	 * Remove a credential from the owned set. It's not a failure to remove
	 * something that isn't in the set.
	 * 
	 * @param toDelete
	 *            The credential to remove.
	 */
	void deleteCredential(Credential toDelete);

	/**
	 * Tests if the credential is valid. This includes testing whether the
	 * underlying credential file exists and can be unlocked by the password in
	 * the {@link Credential} object.
	 * 
	 * @param c
	 *            The credential object to validate.
	 * @throws InvalidCredentialException
	 *             If it is invalid.
	 */
	void validateCredential(Credential c) throws InvalidCredentialException;

	/**
	 * @return The identities trusted by the user. Never <tt>null</tt>.
	 */
	Trust[] getTrusted();

	/**
	 * Add an identity to the trusted set.
	 * 
	 * @param toAdd
	 *            The identity to add.
	 */
	void addTrusted(Trust toAdd);

	/**
	 * Remove an identity from the trusted set. It's not a failure to remove
	 * something that isn't in the set.
	 * 
	 * @param toDelete
	 *            The identity to remove.
	 */
	void deleteTrusted(Trust toDelete);

	/**
	 * Tests if the trusted identity descriptor is valid. This includes checking
	 * whether the underlying trusted identity file exists.
	 * 
	 * @param t
	 *            The trusted identity descriptor to check.
	 * @throws InvalidCredentialException
	 *             If it is invalid.
	 */
	void validateTrusted(Trust t) throws InvalidCredentialException;
}
