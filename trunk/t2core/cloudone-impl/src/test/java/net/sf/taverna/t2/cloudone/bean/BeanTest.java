package net.sf.taverna.t2.cloudone.bean;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;

import org.jdom.output.XMLOutputter;
import org.junit.Test;


public class BeanTest {

	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	XMLOutputter xo = new XMLOutputter();
	
	
	@Test
	public void testDataDocumentIdentifier() {
		DataDocumentIdentifier id = new DataDocumentIdentifier("urn:t2data:ddoc://dataNS/data0");
		assertEquals("dataNS", id.getNamespace());
		assertEquals(0, id.getDepth());
		assertEquals(IDType.Data, id.getType());
		assertEquals("data0", id.getName());
		String bean = id.getAsBean();
		
		DataDocumentIdentifier newId = (DataDocumentIdentifier) EntityIdentifiers.parse(bean);
		assertEquals("dataNS", newId.getNamespace());
		assertEquals(0, newId.getDepth());
		assertEquals(IDType.Data, newId.getType());
		assertEquals("data0", newId.getName());
		assertEquals(bean, newId.getAsBean());
		
	}
	
	@Test
	public void testEntityListIdentifier() {
		EntityListIdentifier id = new EntityListIdentifier("urn:t2data:list://fish/list5311/2");
		assertEquals("fish", id.getNamespace());
		assertEquals(2, id.getDepth());
		assertEquals(IDType.List, id.getType());
		assertEquals("list5311", id.getName());
		String bean = id.getAsBean();
		
		EntityListIdentifier newId = (EntityListIdentifier) EntityIdentifiers.parse(bean);
		assertEquals("fish", newId.getNamespace());
		assertEquals(2, newId.getDepth());
		assertEquals(IDType.List, newId.getType());
		assertEquals("list5311", newId.getName());
		assertEquals(bean, newId.getAsBean());
	}
	
	@Test
	public void testErrorDocumentIdentifier() {
		//ErrorDocumentIdentifier id = new ErrorDocumentIdentifier("urn:t2data:error://fish/error1/3/2");
		
	}
	
}
