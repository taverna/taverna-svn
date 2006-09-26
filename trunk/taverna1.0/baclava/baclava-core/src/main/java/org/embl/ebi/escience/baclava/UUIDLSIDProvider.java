package org.embl.ebi.escience.baclava;

import java.util.UUID;

/**
 * Generate random LSIDs using UUIDs
 * <p>
 * UUIDs are unique in time and space simply because it includes 
 * so many random bits (assuming a good enough random generator)
 * that two duplicate LSIDs will never appear in our lifetime.
 * </p>
 * 
 * @author Stian Soiland
 *
 */
public class UUIDLSIDProvider implements LSIDProvider {
	public String getID(NamespaceEnumeration namespace) {
		UUID uuid = UUID.randomUUID();
		// FIXME: Should we use props.getProperty("taverna.lsid.providerauthority") 
		// instead? What is the authority used for?
		String authority = "net.sf.taverna";
		return "urn:lsid:" + authority + ":" + namespace + ":" + uuid;				
	}	
}
