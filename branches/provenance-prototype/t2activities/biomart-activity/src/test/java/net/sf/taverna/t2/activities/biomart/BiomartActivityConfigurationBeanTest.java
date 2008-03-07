package net.sf.taverna.t2.activities.biomart;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.biomart.martservice.MartQuery;
import org.junit.Before;
import org.junit.Test;

public class BiomartActivityConfigurationBeanTest {

	private BiomartActivityConfigurationBean bean;

	private MartQuery query;

	@Before
	public void setUp() throws Exception {
		bean = new BiomartActivityConfigurationBean();
		query = new MartQuery();
	}

	@Test
	public void testGetQuery() {
		assertNull(bean.getQuery());
	}

	@Test
	public void testSetQuery() {
		bean.setQuery(query);
		assertSame(query, bean.getQuery());
		bean.setQuery(null);
		assertNull(bean.getQuery());
	}

}
