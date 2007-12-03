package net.sf.taverna.t2.workflowmodel.processor.activity;


import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;

import org.junit.Before;
import org.junit.Test;

public class ActivityHealthReportTest {

	ActivityHealthReport report;
	
	@Before
	public void setUp() throws Exception {
		report = new ActivityHealthReport("a subject","a message",Status.WARNING);
	}

	@Test
	public void testActivityHealthReportString() {
		ActivityHealthReport report = new ActivityHealthReport("the subject","string");
		assertEquals("string",report.getMessage());
		assertEquals(Status.OK,report.getStatus());
		assertEquals("the subject",report.getSubject());
	}

	@Test
	public void testActivityHealthReportStringStatus() {
		report = new ActivityHealthReport("the subject","a string",Status.SEVERE);
		assertEquals("a string",report.getMessage());
		assertEquals(Status.SEVERE,report.getStatus());
		assertEquals("the subject",report.getSubject());
	}

	@Test
	public void testGetMessage() {
		assertEquals("a message",report.getMessage());
	}

	@Test
	public void testGetStatus() {
		assertEquals(Status.WARNING,report.getStatus());
	}
	
	@Test
	public void testGetSubject() {
		assertEquals("a subject",report.getSubject());
	}

}
