package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.HealthReportImpl;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProcessorHealthReportTest {

	List<HealthReport> activityReports;
	ProcessorHealthReport report;
	
	@Before
	public void setUp() throws Exception {
		activityReports = new ArrayList<HealthReport>();
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.OK));
		
		report = new ProcessorHealthReport("processor subject",activityReports);
	}

	@Test
	public void testProcessorHealthReportImpl() {
		assertEquals("There should be 3 activity reports",3,report.getSubReports().size());
	}

	@Test
	public void testGetActivityHealthReports() {
		assertEquals("There should be 3 activity reports",3,report.getSubReports().size());
		assertSame(activityReports.get(0),report.getSubReports().get(0));
		assertSame(activityReports.get(1),report.getSubReports().get(1));
		assertSame(activityReports.get(2),report.getSubReports().get(2));
	}

	@Ignore("Not yet implented, flagged as Ignore to allow commit")
	@Test
	public void testGetMessage() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatusAllOK() {
		List<HealthReport> activityReports = new ArrayList<HealthReport>();
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.OK));
		
		ProcessorHealthReport report = new ProcessorHealthReport("processor subject",activityReports);
		
		assertEquals("the status should be OK",Status.OK,report.getStatus());
	}
	
	@Test
	public void testGetStatusContainsWarning() {
		List<HealthReport> activityReports = new ArrayList<HealthReport>();
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.WARNING));
		
		ProcessorHealthReport report = new ProcessorHealthReport("processor subject",activityReports);
		
		assertEquals("the status should be WARNING",Status.WARNING,report.getStatus());
	}
	
	@Test
	public void testGetStatusContainsSevere() {
		List<HealthReport> activityReports = new ArrayList<HealthReport>();
		activityReports.add(new HealthReportImpl("","",Status.OK));
		activityReports.add(new HealthReportImpl("","",Status.SEVERE));
		activityReports.add(new HealthReportImpl("","",Status.OK));
		
		ProcessorHealthReport report = new ProcessorHealthReport("",activityReports);
		
		assertEquals("the status should be WARNING",Status.WARNING,report.getStatus());
	}
	
	@Test
	public void testGetStatusAllSevere() {
		List<HealthReport> activityReports = new ArrayList<HealthReport>();
		activityReports.add(new HealthReportImpl("","",Status.SEVERE));
		activityReports.add(new HealthReportImpl("","",Status.SEVERE));
		activityReports.add(new HealthReportImpl("","",Status.SEVERE));
		
		ProcessorHealthReport report = new ProcessorHealthReport("",activityReports);
		
		assertEquals("the status should be SEVERE",Status.SEVERE,report.getStatus());
	}
	
	@Test
	public void testGetSubject() {
		assertEquals("processor subject",report.getSubject());
	}
}
