package net.sf.taverna.t2.activities.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Dataflow Activity Tests
 * 
 * @author David Withers
 * 
 */
public class DataflowActivityTest {

	private Dataflow dataflow;

	private DataflowActivity activity;

	private DataflowActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new DataflowActivity();
		configurationBean = new DataflowActivityConfigurationBean();
		Edits edits = EditsRegistry.getEdits();
		dataflow = edits.createDataflow();
		edits.getCreateDataflowInputPortEdit(dataflow, "input", 0, 0).doEdit();
		edits.getCreateDataflowOutputPortEdit(dataflow, "output").doEdit();
		configurationBean.setDataflow(dataflow);
	}

	@Test
	public void testConfigureDataflowActivityConfigurationBean()
			throws Exception {
		activity.configure(configurationBean);
		assertEquals(configurationBean, activity.getConfiguration());
		assertEquals(1, activity.getInputPorts().size());
		assertEquals("input", activity.getInputPorts().iterator().next()
				.getName());
		assertEquals(1, activity.getOutputPorts().size());
		assertEquals("output", activity.getOutputPorts().iterator().next()
				.getName());
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
