package net.sf.taverna.t2.compatibility.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.translator.BeanshellActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ActivityTranslatorTest {

	private ActivityTranslator<BeanshellActivityConfigurationBean> translator;

	@SuppressWarnings("unchecked")
	@Before
	public void createTranslator() {
		translator = (ActivityTranslator<BeanshellActivityConfigurationBean>) new BeanshellActivityTranslator();
	}

	@SuppressWarnings("null")
	@Test
	public void testPorts() throws Exception {
		BeanshellProcessor processor = new BeanshellProcessor(null,
				"simplebeanshell", "", new String[] { "input1", "input2" },
				new String[] { "output" });
		processor.getInputPorts()[0].setSyntacticType("l('text/plain')");

		Activity<?> activity = translator.doTranslation(processor);

		assertEquals(2, activity.getInputPorts().size());
		assertEquals(1, activity.getOutputPorts().size());

		Port in1 = null;
		Port in2 = null;

		// don't rely on the order. Look for each expected port, then check
		// neither are still null.
		for (Port inPort : activity.getInputPorts()) {
			if (inPort.getName().equals("input1"))
				in1 = inPort;
			if (inPort.getName().equals("input2"))
				in2 = inPort;
		}
		assertNotNull("No input named input1 found", in1);
		assertNotNull("No input named input2 found", in2);

		Port out1 = (Port) activity.getOutputPorts().toArray()[0];

		assertEquals("input1", in1.getName());
		assertEquals(1, in1.getDepth());

		assertEquals("input2", in2.getName());
		assertEquals(0, in2.getDepth());

		assertEquals("output", out1.getName());
		assertEquals(0, out1.getDepth());

		assertNotNull(activity.getConfiguration());
	}

	@Ignore("Can't find mimetype")
	@Test
	public void testPortAnnotatedMimeTypes() throws Exception {
		BeanshellProcessor processor = new BeanshellProcessor(null,
				"simplebeanshell", "", new String[] { "input1", "input2" },
				new String[] { "output" });
		processor.getInputPorts()[0].setSyntacticType("'text/xml'");
		processor.getOutputPorts()[0]
				.setSyntacticType("l('application/octet-stream')");

		Activity<?> activity = translator.doTranslation(processor);

		Port in1 = null;
		Port in2 = null;

		// don't rely on the order. Look for each expected port, then check
		// neither are still null.
		for (Port inPort : activity.getInputPorts()) {
			if (inPort.getName().equals("input1"))
				in1 = inPort;
			if (inPort.getName().equals("input2"))
				in2 = inPort;
		}
		assertNotNull("No input named input1 found", in1);
		assertNotNull("No input named input2 found", in2);

		Port out1 = (Port) activity.getOutputPorts().toArray()[0];
		
		// TODO - this will have to wait until we have the AnnotationPerspective
		// at least naively implemented as there's now an additional layer of
		// stuff before you can get to the 'real' annotations on any given
		// object.

		/**
		 * MimeType mimetype = null; for (WorkflowAnnotation annotation :
		 * in1.getAnnotations()) { if (annotation instanceof MimeType) {
		 * assertNotNull("More than 1 mime type annotation found."); mimetype =
		 * (MimeType) annotation; } } assertNotNull("No mimetype annotation
		 * found", mimetype); assertEquals("'text/xml'",
		 * mimetype.getMIMEType());
		 * 
		 * mimetype = null; for (WorkflowAnnotation annotation :
		 * in2.getAnnotations()) { if (annotation instanceof MimeType) {
		 * assertNotNull("More than 1 mime type annotation found."); mimetype =
		 * (MimeType) annotation; } } assertNotNull("No mimetype annotation
		 * found", mimetype); assertEquals("'text/plain'",
		 * mimetype.getMIMEType());
		 * 
		 * mimetype = null; for (WorkflowAnnotation annotation :
		 * out1.getAnnotations()) { if (annotation instanceof MimeType) {
		 * assertNotNull("More than 1 mime type annotation found."); mimetype =
		 * (MimeType) annotation; } } assertNotNull("No mimetype annotation
		 * found", mimetype); assertEquals("l('application/octet-stream')",
		 * mimetype.getMIMEType());
		 */

	}
}
