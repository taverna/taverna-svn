package net.sf.taverna.t2.cloudone.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;

import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.bean.SillyBean;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.junit.Test;


public class EntitySerialiserTest {
	
	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	XMLOutputter xo = new XMLOutputter();

	
	@Test
	public void serialiseSillyBean() throws JDOMException, IOException {
		SillyBean silly = new SillyBean();
		silly.setName("I'm silly");
		Element elem = EntitySerialiser.toXML(silly);
		SillyBean silly2 = (SillyBean) EntitySerialiser.fromXML(elem, SillyBean.class.getClassLoader());
		assertEquals("I'm silly", silly2.getName());
	}
	
	
	@Test
	public void serialiseDataDocument() throws EntityNotFoundException, JDOMException, IOException {
		DataDocumentIdentifier docid = dManager.registerDocument(new HashSet<ReferenceScheme>());
		System.out.println(docid);
		DataDocument doc = (DataDocument) dManager.getEntity(docid);
		Element elem = EntitySerialiser.toXML(doc);
		System.out.println(xo.outputString(elem));
	}
	
	
	@Test
	public void serialiseEmptyList() throws EntityNotFoundException, JDOMException, IOException {
		int depth = 3;
		EntityListIdentifier list = dManager.registerEmptyList(depth);
		assertEquals(depth, list.getDepth());
		EntityList entList = (EntityList) dManager.getEntity(list);
		Element elem = EntitySerialiser.toXML(entList);
		System.out.println(xo.outputString(elem));
	}
	
	@Test
	public void serialiseURLRefScheme() throws JDOMException, IOException {
		URLReferenceScheme urlRef = new URLReferenceScheme();
		urlRef.setFromBean("http://taverna.sf.net/");
		Element elem = EntitySerialiser.toXML(urlRef.getAsBean());
		System.out.println( xo.outputString(elem));
	}
	

}
