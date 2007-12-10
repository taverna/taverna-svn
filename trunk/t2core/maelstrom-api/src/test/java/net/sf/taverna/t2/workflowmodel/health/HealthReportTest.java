package net.sf.taverna.t2.workflowmodel.health;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

import org.junit.Before;
import org.junit.Test;

public class HealthReportTest {

	HealthReport report;
	
	@Before
	public void setUp() throws Exception {
		List<HealthReport> subreports = new ArrayList<HealthReport>();
		subreports.add(new HealthReport("sub subject","this is a subreport",Status.OK));
		report = new HealthReport("a subject","a message",Status.WARNING,subreports);
	}

	@Test
	public void testActivityHealthReportStringStatus() {
		report = new HealthReport("the subject","a string",Status.SEVERE);
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
	
	@Test 
	public void testStatusHighestIncludingSubReports() {
		report = new HealthReport("parent","set to ok",Status.OK);
		assertEquals("should be OK",Status.OK,report.getStatus());
		report.getSubReports().add(new HealthReport("child1","set to warning",Status.WARNING));
		assertEquals("should be WARNING",Status.WARNING,report.getStatus());
		report.getSubReports().add(new HealthReport("child1","set to severe",Status.SEVERE));
		assertEquals("should be SEVERE",Status.SEVERE,report.getStatus());
	}

}
