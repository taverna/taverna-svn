package net.sf.taverna.t2.component.api;

import java.net.URL;
import java.util.List;
import java.util.Set;

public interface Registry {

	License getPreferredLicense() throws RegistryException;

	Set<Version.ID> searchForComponents(String prefixString, String text)
			throws RegistryException;

	/**
	 * Returns all the ComponentFamilies in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain any ComponentFamilies an empty
	 * list is returned.
	 * 
	 * @return all the ComponentFamilies in this ComponetRegistry.
	 * @throws RegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	List<Family> getComponentFamilies() throws RegistryException;

	List<License> getLicenses() throws RegistryException;

	List<SharingPolicy> getPermissions() throws RegistryException;

	/**
	 * Adds a ComponentProfile to this ComponentRegistry.
	 * 
	 * @param componentProfile
	 *            the ComponentProfile to add. Must not be null.
	 * @param sharingPolicy
	 * @param license
	 * @return the ComponentProfile added to this ComponentRegistry.
	 * @throws RegistryException
	 *             <ul>
	 *             <li>if componentProfile is null,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	Profile addComponentProfile(Profile componentProfile, License license,
			SharingPolicy sharingPolicy) throws RegistryException;

	/**
	 * Returns all the ComponentProfiles in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain any ComponentProfiles an empty
	 * list is returned.
	 * 
	 * @return all the ComponentProfiles in this ComponetRegistry.
	 * @throws RegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	List<Profile> getComponentProfiles() throws RegistryException;

	/**
	 * Returns the ComponentProfile with the given ID in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain the ComponentProfile, a
	 * <tt>null</tt> is returned.
	 * 
	 * @return the matching ComponentProfile in this ComponetRegistry, or
	 *         <tt>null</tt> if there is no such thing.
	 * @throws RegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	Profile getComponentProfile(String id) throws RegistryException;

	String getRegistryBaseString();

	/**
	 * Returns the location of this ComponentRepository.
	 * 
	 * @return the location of this ComponentRepository
	 */
	URL getRegistryBase();

	/**
	 * Removes a the ComponentFamily with the specified name from this
	 * ComponentRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain a ComponentFamily with the
	 * specified name this method has no effect.
	 * 
	 * @param componentFamily
	 *            the ComponentFamily to remove.
	 * @throws RegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	void removeComponentFamily(Family componentFamily) throws RegistryException;

	/**
	 * Creates a new ComponentFamily and adds it to this ComponentRegistry.
	 * 
	 * @param familyName
	 *            the name of the ComponentFamily to create. Must not be null.
	 * @param componentProfile
	 *            the ComponentProfile for the new ComponentFamily. Must not be
	 *            null.
	 * @param sharingPolicy
	 *            the SharingPolicy to use for the new ComponentFamily.
	 * @return the new ComponentFamily
	 * @throws RegistryException
	 *             <ul>
	 *             <li>if familyName is null,
	 *             <li>if componentProfile is null,
	 *             <li>if a ComponentFamily with this name already exists,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	Family createComponentFamily(String familyName, Profile componentProfile,
			String description, License license, SharingPolicy sharingPolicy)
			throws RegistryException;

	/**
	 * Returns the ComponentFamily with the specified name.
	 * <p>
	 * If this ComponentRegistry does not contain a ComponentFamily with the
	 * specified name <code>null</code> is returned.
	 * 
	 * @param familyName
	 *            the name of the ComponentFamily to return. Must not be null.
	 * @return the ComponentFamily with the specified name in this
	 *         ComponentRepository or null if none exists.
	 * @throws RegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	Family getComponentFamily(String familyName) throws RegistryException;

	/**
	 * @return A description of the type of registry this is.
	 */
	String getRegistryTypeName();
}
