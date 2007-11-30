package net.sf.taverna.t2.workflowmodel.processor.activity;


import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;

import org.junit.Before;
import org.junit.Test;

public class ActivityHealthReportTest {

	ActivityHealthReport report;
	
	@Before
	public void setUp() throws Exception {
		report = new ActivityHealthReport("a message",Status.WARNING);
	}

	@Test
	public void testActivityHealthReportString() {
		ActivityHealthReport report = new ActivityHealthReport("string");
		assertEquals("string",report.getMessage());
		assertEquals(Status.OK,report.getStatus());
	}

	@Test
	public void testActivityHealthReportStringStatus() {
		report = new ActivityHealthReport("a string",Status.SEVERE);
		assertEquals("a string",report.getMessage());
		assertEquals(Status.SEVERE,report.getStatus());
	}

	@Test
	public void testGetMessage() {
		assertEquals("a message",report.getMessage());
	}

	@Test
	public void testGetStatus() {
		assertEquals(Status.WARNING,report.getStatus());
	}

}
