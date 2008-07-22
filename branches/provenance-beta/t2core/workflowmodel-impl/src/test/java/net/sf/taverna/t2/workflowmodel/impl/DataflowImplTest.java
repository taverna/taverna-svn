package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DataflowImplTest {
	DataflowImpl df = new DataflowImpl();
	
	@Test
	public void testInternalIdentifer() {
		assertNotNull("the identifier should be created at construction time",df.getInternalIdentier());
	}
	
	@Test
	public void testRefreshInternalIndentifier() {
		String oldId=df.getInternalIdentier();
		df.refreshInternalIdentifier();
		assertNotNull("the new identifier should not be null",df.getInternalIdentier());
		assertFalse("the identifier should have changed",oldId.equals(df.getInternalIdentier()));
	}
}
