package org.embl.ebi.escience.baclava;

import java.util.UUID;

/**
 * Generate random LSIDs using UUIDs
 * 
 * @author Stian Soiland
 *
 */
public class UUIDLSIDProvider implements LSIDProvider {
	public String getID(NamespaceEnumeration namespace) {
		UUID uuid = UUID.randomUUID();
		// FIXME: Should we use props.getProperty("taverna.lsid.providerauthority") 
		// instead? What is the authority used for?
		return "urn:lsid:net.sf.taverna:" + namespace + ":" + uuid;				
	}	
}
