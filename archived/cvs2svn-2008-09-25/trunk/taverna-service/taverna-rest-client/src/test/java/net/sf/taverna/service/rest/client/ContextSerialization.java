package net.sf.taverna.service.rest.client;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.junit.Test;
import static org.junit.Assert.*;

public class ContextSerialization extends ContextTest {
	private static Logger logger = Logger.getLogger(ContextSerialization.class);

	private static final String NAME = "Fish and stuff and <not xml>";
	
	@Test
	public void toXML() {
		Element e = context.toXML();
		assertEquals("restContext", e.getName());
		assertEquals("", e.getNamespaceURI());
		assertNotNull(e.getChild("uri"));
		assertNotNull(e.getChild("username"));
		assertNotNull(e.getChild("password"));
		assertNull(e.getChild("name"));
	}
	
	@Test
	public void toXMLName() {
		context.setName(NAME);
		Element e = context.toXML();
		assertEquals(NAME, e.getChildText("name"));	
	}

	@Test
	public void fromXML() throws NotSuccessException {
		Element e = context.toXML();
		RESTContext loadedContext = RESTContext.fromXML(e);
		assertEquals(context.getBaseURI(), loadedContext.getBaseURI());
		// TODO: Check uri/username/password attributes
		// requires users;current to work, thus requires correct user/password
		assertEquals(context.getUser(), loadedContext.getUser());
		assertNull(loadedContext.getName());
	}

	@Test
	public void fromXMLName() throws NotSuccessException {
		context.setName(NAME);
		Element e = context.toXML();
		RESTContext loadedContext = RESTContext.fromXML(e);
		assertEquals(context.getBaseURI(), loadedContext.getBaseURI());
		assertEquals(context.getUser(), loadedContext.getUser());
		assertEquals(NAME, loadedContext.getName());
	}

	@Test
	public void string() {
		assertEquals(context.getBaseURI().toString(), context.toString());
		context.setName(NAME);
		assertEquals(NAME, context.toString());
	}
	
}
