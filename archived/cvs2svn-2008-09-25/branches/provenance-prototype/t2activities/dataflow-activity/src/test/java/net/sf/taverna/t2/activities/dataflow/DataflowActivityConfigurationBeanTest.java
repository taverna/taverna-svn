package net.sf.taverna.t2.activities.dataflow;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

import org.junit.Before;
import org.junit.Test;

/**
 * Dataflow Configuration Tests
 * 
 * @author David Withers
 * 
 */
public class DataflowActivityConfigurationBeanTest {

	private DataflowActivityConfigurationBean bean;

	private Dataflow dataflow;

	@Before
	public void setUp() throws Exception {
		bean = new DataflowActivityConfigurationBean();
		dataflow = EditsRegistry.getEdits().createDataflow();
	}

	@Test
	public void testGetDataflow() {
		assertNull(bean.getDataflow());
	}

	@Test
	public void testSetDataflow() {
		bean.setDataflow(dataflow);
		assertSame(dataflow, bean.getDataflow());
		bean.setDataflow(null);
		assertNull(bean.getDataflow());
	}

}
