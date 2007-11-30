package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityHealthReport;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ProcessorHealthReportImplTest {

	List<ActivityHealthReport> activityReports;
	ProcessorHealthReportImpl report;
	
	@Before
	public void setUp() throws Exception {
		activityReports = new ArrayList<ActivityHealthReport>();
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.OK));
		
		report = new ProcessorHealthReportImpl(activityReports);
	}

	@Test
	public void testProcessorHealthReportImpl() {
		assertEquals("There should be 3 activity reports",3,report.getActivityHealthReports().size());
	}

	@Test
	public void testGetActivityHealthReports() {
		assertEquals("There should be 3 activity reports",3,report.getActivityHealthReports().size());
		assertSame(activityReports.get(0),report.getActivityHealthReports().get(0));
		assertSame(activityReports.get(1),report.getActivityHealthReports().get(1));
		assertSame(activityReports.get(2),report.getActivityHealthReports().get(2));
	}

	@Ignore("Not yet implented, flagged as Ignore to allow commit")
	@Test
	public void testGetMessage() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStatusAllOK() {
		List<ActivityHealthReport> activityReports = new ArrayList<ActivityHealthReport>();
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.OK));
		
		ProcessorHealthReportImpl report = new ProcessorHealthReportImpl(activityReports);
		
		assertEquals("the status should be OK",Status.OK,report.getStatus());
	}
	
	@Test
	public void testGetStatusContainsWarning() {
		List<ActivityHealthReport> activityReports = new ArrayList<ActivityHealthReport>();
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.WARNING));
		
		ProcessorHealthReportImpl report = new ProcessorHealthReportImpl(activityReports);
		
		assertEquals("the status should be WARNING",Status.WARNING,report.getStatus());
	}
	
	@Test
	public void testGetStatusContainsSevere() {
		List<ActivityHealthReport> activityReports = new ArrayList<ActivityHealthReport>();
		activityReports.add(new ActivityHealthReport("",Status.OK));
		activityReports.add(new ActivityHealthReport("",Status.SEVERE));
		activityReports.add(new ActivityHealthReport("",Status.OK));
		
		ProcessorHealthReportImpl report = new ProcessorHealthReportImpl(activityReports);
		
		assertEquals("the status should be WARNING",Status.WARNING,report.getStatus());
	}
	
	@Test
	public void testGetStatusAllSevere() {
		List<ActivityHealthReport> activityReports = new ArrayList<ActivityHealthReport>();
		activityReports.add(new ActivityHealthReport("",Status.SEVERE));
		activityReports.add(new ActivityHealthReport("",Status.SEVERE));
		activityReports.add(new ActivityHealthReport("",Status.SEVERE));
		
		ProcessorHealthReportImpl report = new ProcessorHealthReportImpl(activityReports);
		
		assertEquals("the status should be SEVERE",Status.SEVERE,report.getStatus());
	}
}
