package net.sf.taverna.t2.activities.dataflow;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DataflowActivityTest {

	private DataflowActivity activity;

	private DataflowActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new DataflowActivity();
		configurationBean = new DataflowActivityConfigurationBean();
	}

	@Ignore
	@Test
	public void testConfigureDataflowActivityConfigurationBean() throws Exception {
		activity.configure(configurationBean);
		assertEquals(configurationBean, activity.getConfiguration());
	}

	@Test
	public void testGetConfiguration() {
		assertNull(activity.getConfiguration());
	}

	@Ignore
	@Test
	public void testExecuteAsynch() {
		fail("Not yet implemented");
	}

}
