/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.registry;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.myexperiment.MyExperimentComponentFamily;

/**
 * A ComponentRegistry contains ComponentFamilies and ComponentProfiles.
 *
 * @author David Withers
 */
public abstract class ComponentRegistry {
	
	protected Map<String, ComponentFamily> familyCache = new HashMap<String, ComponentFamily> ();
	protected List<ComponentProfile> profileCache = new ArrayList<ComponentProfile>();
	protected List<SharingPolicy> permissionCache = new ArrayList<SharingPolicy>();
	protected List<License> licenseCache = new ArrayList<License>();
	
	private URL registryBase;
	
	protected ComponentRegistry (URL registryBase) throws ComponentRegistryException {
		this.registryBase = registryBase;
	}
	
	protected ComponentRegistry (File fileDir) throws ComponentRegistryException {
			try {
				this.registryBase = fileDir.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new ComponentRegistryException (e);
			}
	}

	/**
	 * Returns all the ComponentFamilies in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain any ComponentFamilies an empty
	 * list is returned.
	 *
	 * @return all the ComponentFamilies in this ComponetRegistry.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public final List<ComponentFamily> getComponentFamilies() throws ComponentRegistryException {
		checkFamilyCache();
		return new ArrayList(familyCache.values());
	}
	
	private void checkFamilyCache() throws ComponentRegistryException {
		synchronized(familyCache) {
			if (familyCache.isEmpty()) {
				populateFamilyCache();
			}
		}
	}
	
	protected abstract void populateFamilyCache() throws ComponentRegistryException;

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
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public final ComponentFamily getComponentFamily(String familyName) throws ComponentRegistryException {
		checkFamilyCache();
		return familyCache.get(familyName);
	}

	/**
	 * Creates a new ComponentFamily and adds it to this ComponentRegistry.
	 *
	 * @param familyName
	 *            the name of the ComponentFamily to create. Must not be null.
	 * @param componentProfile
	 *            the ComponentProfile for the new ComponentFamily. Must not be
	 *            null.
	 * @param sharingPolicy 
	 * 			  the SharingPolicy to use for the new ComponentFamily.
	 * @return the new ComponentFamily
	 * @throws ComponentRegistryException
	 *             <ul>
	 *             <li>if familyName is null,
	 *             <li>if componentProfile is null,
	 *             <li>if a ComponentFamily with this name already exists,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	public final ComponentFamily createComponentFamily(String familyName,
			ComponentProfile componentProfile, String description,
			License license, SharingPolicy sharingPolicy) throws ComponentRegistryException {
		if (familyName == null) {
			throw new ComponentRegistryException(("Component family name must not be null"));
		}
		if (componentProfile == null) {
			throw new ComponentRegistryException(("Component profile must not be null"));
		}
		if (getComponentFamily(familyName) != null) {
			throw new ComponentRegistryException(("Component family already exists"));
		}
		ComponentFamily result = internalCreateComponentFamily(familyName, componentProfile, description, license, sharingPolicy);
		checkFamilyCache();
		synchronized(familyCache) {
			familyCache.put(familyName, result);
		}
		return result;
	}
	
	protected abstract ComponentFamily internalCreateComponentFamily(String familyName,
			ComponentProfile componentProfile, String description,
			License license, SharingPolicy sharingPolicy) throws ComponentRegistryException;

	/**
	 * Removes a the ComponentFamily with the specified name from this
	 * ComponentRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain a ComponentFamily with the
	 * specified name this method has no effect.
	 *
	 * @param componentFamily
	 *            the ComponentFamily to remove.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public final void removeComponentFamily(ComponentFamily componentFamily)
			throws ComponentRegistryException {
		if (componentFamily != null) {
			checkFamilyCache();
			synchronized(familyCache) {
				familyCache.remove(componentFamily.getName());
			}
		}
		internalRemoveComponentFamily(componentFamily);
	}

	protected abstract void internalRemoveComponentFamily(ComponentFamily componentFamily)
	throws ComponentRegistryException;

	/**
	 * Returns the location of this ComponentRepository.
	 *
	 * @return the location of this ComponentRepository
	 */
	public final URL getRegistryBase() {
		return registryBase;
	}
	
	public final String getRegistryBaseString() {
            String urlString = getRegistryBase().toString();
            if (urlString.endsWith("/")) {
                    urlString = urlString.substring(0, urlString.length() - 1);
            }
            return urlString;
	}

	private void checkProfileCache() throws ComponentRegistryException {
		synchronized(profileCache) {
			if (profileCache.isEmpty()) {
				populateProfileCache();
			}
		}
	}
	
	protected abstract void populateProfileCache() throws ComponentRegistryException;
	
	/**
	 * Returns all the ComponentProfiles in this ComponetRegistry.
	 * <p>
	 * If this ComponentRegistry does not contain any ComponentProfiles an empty
	 * list is returned.
	 *
	 * @return all the ComponentProfiles in this ComponetRegistry.
	 * @throws ComponentRegistryException
	 *             if there is a problem accessing the ComponentRegistry.
	 */
	public final List<ComponentProfile> getComponentProfiles() throws ComponentRegistryException {
		checkProfileCache();
		return profileCache;
	}

	/**
	 * Adds a ComponentProfile to this ComponentRegistry.
	 *
	 * @param componentProfile
	 *            the ComponentProfile to add. Must not be null.
	 * @param sharingPolicy 
	 * @param license 
	 * @return the ComponentProfile added to this ComponentRegistry.
	 * @throws ComponentRegistryException
	 *             <ul>
	 *             <li>if componentProfile is null,
	 *             <li>if there is a problem accessing the ComponentRegistry.
	 *             </ul>
	 */
	public final ComponentProfile addComponentProfile(ComponentProfile componentProfile, License license, SharingPolicy sharingPolicy)
			throws ComponentRegistryException {
		if (componentProfile == null) {
			throw new ComponentRegistryException("componentProfile is null");
		}
		ComponentProfile result = null;
		checkProfileCache();
		for (ComponentProfile p : this.getComponentProfiles()) {
			if (p.getId().equals(componentProfile.getId())) {
				result = p;
				break;
			}
		}
		if (result == null) {
			result = internalAddComponentProfile(componentProfile, license, sharingPolicy);
			synchronized(profileCache) {
				profileCache.add(result);
			}
		}
		return result;
	}

	protected abstract ComponentProfile internalAddComponentProfile(ComponentProfile componentProfile, License license, SharingPolicy sharingPolicy) throws ComponentRegistryException;
	
	private void checkPermissionCache() {
		synchronized(permissionCache) {
			if (permissionCache.isEmpty()) {
				populatePermissionCache();
			}
		}
	}
	
	protected abstract void populatePermissionCache();
	
	public final List<SharingPolicy> getPermissions() throws ComponentRegistryException {
		checkPermissionCache();
		return permissionCache;
	}

	private void checkLicenseCache() {
		synchronized(licenseCache) {
			if (licenseCache.isEmpty()) {
				populateLicenseCache();
			}
		}
	}
	
	protected abstract void populateLicenseCache();
	
	public final List<License> getLicenses() throws ComponentRegistryException {
		checkLicenseCache();
		return licenseCache;
	}

	protected License getLicenseByAbbreviation(String licenseString)
			throws ComponentRegistryException {
		checkLicenseCache();
		for (License l : getLicenses()) {
			if (l.getAbbreviation().equals(licenseString)) {
				return l;
			}
		}
		return null;
	}
	
	public abstract License getPreferredLicense() throws ComponentRegistryException;

}
