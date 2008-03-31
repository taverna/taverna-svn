package net.sf.taverna.t2.activities.biomart;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceXMLHandler;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

public class BiomartActivityConfigurationBeanTest {

	private BiomartActivityConfigurationBean bean;

	private MartQuery query;

	private Element queryElement;

	@Before
	public void setUp() throws Exception {
		bean = new BiomartActivityConfigurationBean();
		query = new MartQuery();
		queryElement = new Element("test");
	}

	@Test
	public void testGetQuery() {
		assertNull(bean.getQuery());
	}

	@Test
	public void testSetQuery() {
		bean.setQuery(queryElement);
		assertSame(queryElement, bean.getQuery());
		bean.setQuery(null);
		assertNull(bean.getQuery());
	}

}
