package org.embl.ebi.escience.baclava;

import java.util.UUID;
import org.embl.ebi.escience.baclava.LSIDProvider.NamespaceEnumeration;
import org.embl.ebi.escience.baclava.lsid.UUIDLSIDProvider;

import junit.framework.TestCase;

/**
 * UUIDLSIDProviderTest
 * 
 * @author Stian Soiland
 * 
 */
public class UUIDLSIDProviderTest extends TestCase {
	public void testUUID() {
		LSIDProvider provider = new UUIDLSIDProvider();
		NamespaceEnumeration namespace = new NamespaceEnumeration("fish");
		String id = provider.getID(namespace);
		String prefix = "urn:lsid:net.sf.taverna:fish:";
		assertTrue(id.startsWith(prefix));
		// LSIDs are urn:lsid:PROVIDER:NAMESPACE:VALUE:VERSION - with :VERSION
		// optional. We'll check the VALUE (#4)
		UUID uuid = UUID.fromString(id.split(":")[4]);
		assertEquals(2, uuid.variant());
		assertEquals(4, uuid.version());
		// And should be unique
		String otherUUID = provider.getID(namespace).split(":")[4];
		assertFalse(id.equals(otherUUID));
		// Even with a new instance should be unique
		String yetOtherUUID = new UUIDLSIDProvider().getID(namespace)
				.split(":")[4];
		assertFalse(yetOtherUUID.equals(uuid));
		assertFalse(yetOtherUUID.equals(otherUUID));
	}
}
