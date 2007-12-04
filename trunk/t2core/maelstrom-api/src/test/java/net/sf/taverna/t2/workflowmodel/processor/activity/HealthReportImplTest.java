package net.sf.taverna.t2.workflowmodel.processor.activity;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.HealthReportImpl;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;

import org.junit.Before;
import org.junit.Test;

public class HealthReportImplTest {

	HealthReport report;
	
	@Before
	public void setUp() throws Exception {
		List<HealthReport> subreports = new ArrayList<HealthReport>();
		subreports.add(new HealthReportImpl("sub subject","this is a subreport",Status.OK));
		report = new HealthReportImpl("a subject","a message",Status.WARNING,subreports);
	}

	@Test
	public void testActivityHealthReportStringStatus() {
		report = new HealthReportImpl("the subject","a string",Status.SEVERE);
		assertEquals("a string",report.getMessage());
		assertEquals(Status.SEVERE,report.getStatus());
		assertEquals("the subject",report.getSubject());
		assertEquals("the subreports should be an empty list",0,report.getSubReports().size());
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
	
	@Test
	public void testGetSubreports() {
		List<HealthReport> subreports = report.getSubReports();
		assertEquals("There should be 1 report",1,subreports.size());
		assertEquals("Wrong subject","sub subject",subreports.get(0).getSubject());
	}

}
