package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.*;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Before;
import org.junit.Test;

public class ConfigureActivityEditTest {

	DummyActivity activity;
	
	@Before
	public void setup() throws Exception {
		activity=new DummyActivity();
	}
	
	@Test
	public void testDoEdit() throws Exception {
		String bean = "bob";
		Edit<?> edit = new ConfigureActivityEdit(activity,bean);
		edit.doEdit();
		assertTrue(activity.isConfigured);
		assertEquals("bob",activity.getConfiguration());
	}
	
	@Test
	public void testUndo() throws Exception {
		String bean = "bob";
		activity.configure("fred");
		Edit<?> edit = new ConfigureActivityEdit(activity,bean);
		edit.doEdit();
		
		assertEquals("bob",activity.getConfiguration());
		edit.undo();
		
		assertEquals("fred",activity.getConfiguration());
		assertFalse(edit.isApplied());
	}
	
	@Test
	public void testIsApplied()  throws Exception {
		String bean = "bob";
		Edit<?> edit = new ConfigureActivityEdit(activity,bean);
		assertFalse(edit.isApplied());
		edit.doEdit();
		assertTrue(edit.isApplied());
	}
	
	@Test
	public void testSubject() {
		String bean = "bob";
		Edit<?> edit = new ConfigureActivityEdit(activity,bean);
		assertEquals(activity,edit.getSubject());
	}
	
	class DummyActivity extends AbstractActivity<Object> {
		public boolean isConfigured=false;
		public Object configBean=null;
		@Override
		public void configure(Object conf)
				throws ActivityConfigurationException {
			isConfigured=true;
			configBean=conf;
		}

		@Override
		public Object getConfiguration() {
			return configBean;
		}
		
	}
}
