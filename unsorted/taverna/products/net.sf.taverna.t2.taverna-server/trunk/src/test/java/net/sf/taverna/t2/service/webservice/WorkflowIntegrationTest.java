/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.service.webservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.service.webservice.resource.DataResource;
import net.sf.taverna.t2.service.webservice.resource.DataValue;
import net.sf.taverna.t2.service.webservice.resource.JobResource;
import net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Runs integration test workflows on the server.
 * 
 * As the workflows use real services a test failure may be caused
 * by a service being down.
 *
 * @author David Withers
 */
public class WorkflowIntegrationTest {

	private TavernaRESTClient tavernaRESTClient;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		tavernaRESTClient = new TavernaRESTClient();
		tavernaRESTClient.setBaseURL("http://localhost:8080/t2server/rest");
		tavernaRESTClient.setCredentials(new UsernamePasswordCredentials("default", "frgsi;y"));
	}

	@Test
	public void testRunWorkflow() throws IOException, InterruptedException {
		List<DataValue> innerList = new ArrayList<DataValue>();
		List<DataValue> outerList = new ArrayList<DataValue>();
//		innerList.add(new DataValue("80332"));
		innerList.add(new DataValue("3565"));
		outerList.add(new DataValue(innerList));
		Map<String, DataValue> inputData = new HashMap<String, DataValue>();
		inputData.put("id", new DataValue(outerList));
		Long inputID = tavernaRESTClient.addData(new DataResource(inputData));
		assertNotNull(inputID);
		
		String workflowXML = IOUtils.toString(getClass().getResource("EntrezGeneIdWorkflow.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, inputID);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(5000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(inputID);				
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}
	
	@Test
	public void testRunBiomobyWorkflow() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("biomoby-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(5000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}

	@Test
	public void testRunSoaplabWorkflow() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("soaplab-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(5000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);
		DataValue output = outputData.getDataMap().get("outfile");
		assertEquals(0, output.depth());
		assertEquals(false, output.getContainsError());
		assertTrue(((String) output.getValue()).trim().matches("[0-9]\\.[0-9]\\.[0-9]"));
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}

	@Test
	public void testRunLocalworkerWorkflow() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("localworker-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(1000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		assertEquals("ab", outputData.getDataMap().get("out").getValue());
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}

	@Test
	public void testRunNestedWorkflow() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("nested-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(1000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		assertEquals("constant", outputData.getDataMap().get("constant").getValue());
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}

	@Test
	public void testRunFailingWorkflow() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("failing-workflow.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(1000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}

	@Test
	public void testPauseNestedWorkflow() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("pause-nested-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		long startTime = System.currentTimeMillis();
		long duration = 0;
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		Thread.sleep(2000);
		assertEquals("RUNNING", tavernaRESTClient.getJobStatus(jobID));
		assertTrue(tavernaRESTClient.pauseJob(jobID));
		System.out.println("workflow paused");
		assertEquals("PAUSED", tavernaRESTClient.getJobStatus(jobID));
		Thread.sleep(5000);
		assertTrue(tavernaRESTClient.resumeJob(jobID));
		System.out.println("workflow resumed");
		assertEquals("RUNNING", tavernaRESTClient.getJobStatus(jobID));
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			duration = System.currentTimeMillis() - startTime;
			System.out.println("Waiting for job ["+jobID+"] to complete (" + (duration / 1000f / 60f) + ")");
			Thread.sleep(1000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		Long outputID = job.getOutputs();
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);				
		System.out.println("Job took " + (duration / 1000f / 60f)+ " minutes");
	}

}
