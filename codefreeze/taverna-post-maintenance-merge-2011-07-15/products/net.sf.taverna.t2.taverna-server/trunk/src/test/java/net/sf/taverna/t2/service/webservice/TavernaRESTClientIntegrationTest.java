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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.service.webservice.resource.DataResource;
import net.sf.taverna.t2.service.webservice.resource.DataValue;
import net.sf.taverna.t2.service.webservice.resource.JobResource;
import net.sf.taverna.t2.service.webservice.resource.Resource;
import net.sf.taverna.t2.service.webservice.resource.WorkflowResource;
import net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 * @author David Withers
 */
public class TavernaRESTClientIntegrationTest {

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

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#addData(net.sf.taverna.t2.service.model.DataMap)}.
	 */
	@Test
	public void testAddData() {
		Map<String, DataValue> data = new HashMap<String, DataValue>();
		data.put("Input", new DataValue("path:map07025"));
		Long dataID = tavernaRESTClient.addData(new DataResource(data));
		assertNotNull(dataID);
		assertEquals(data, tavernaRESTClient.getData(dataID).getDataMap());
		tavernaRESTClient.deleteData(dataID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#addJob(java.lang.String, java.lang.String)}.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testAddJob() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-no-input.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			System.out.println("Waiting for job ["+jobID+"] to complete");
			Thread.sleep(1000);
		}
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		Long outputID = job.getOutputs();
		assertNotNull(outputID);
		DataResource outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		assertEquals("test output", outputData.getDataMap().get("out").getValue());
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);
				
		workflowXML = IOUtils.toString(getClass().getResource("list-output-test.t2flow").openStream());
		workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			System.out.println("Waiting for job ["+jobID+"]to complete");
			Thread.sleep(1000);
		}
		job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		outputID = job.getOutputs();
		assertNotNull(outputID);
		outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		System.out.println(outputData);		
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(outputID);

		Map<String, DataValue> inputData = new HashMap<String, DataValue>();
		inputData.put("in", new DataValue("path:map07025"));
		Long inputID = tavernaRESTClient.addData(new DataResource(inputData));
		
		workflowXML = IOUtils.toString(getClass().getResource("beanshell-test.t2flow").openStream());
		workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		jobID = tavernaRESTClient.addJob(workflowID, inputID);
		assertNotNull(jobID);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			System.out.println("Waiting for job ["+jobID+"]to complete");
			Thread.sleep(1000);
		}
		job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		System.out.println(job);
		outputID = job.getOutputs();
		assertNotNull(outputID);
		outputData = tavernaRESTClient.getData(outputID);
		assertNotNull(outputData);
		assertEquals("path:map07025", outputData.getDataMap().get("out").getValue());
		System.out.println(outputData);		
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteData(inputID);
		tavernaRESTClient.deleteData(outputID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#addWorkflow(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testAddWorkflow() throws IOException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		tavernaRESTClient.deleteWorkflow(workflowID);
		workflowXML = IOUtils.toString(getClass().getResource("wsdl-test.t2flow").openStream());
		workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(workflowID);
		tavernaRESTClient.deleteWorkflow(workflowID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#deleteData(java.lang.String)}.
	 */
	@Test
	public void testDeleteData() {
		Map<String, DataValue> data = new HashMap<String, DataValue>();
		data.put("Input", new DataValue("path:map07025"));
		Long dataID = tavernaRESTClient.addData(new DataResource(data));
		assertNotNull(tavernaRESTClient.getData(dataID));
		tavernaRESTClient.deleteData(dataID);
		assertNull(tavernaRESTClient.getData(dataID));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#deleteJob(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testDeleteJob() throws IOException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-no-input.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		assertNotNull(tavernaRESTClient.getJob(jobID));
		assertTrue(containsElement(tavernaRESTClient.getJobs().getJob(), jobID));
		tavernaRESTClient.deleteJob(jobID);
		assertNull(tavernaRESTClient.getJob(jobID));
		assertFalse(containsElement(tavernaRESTClient.getJobs().getJob(), jobID));
		tavernaRESTClient.deleteWorkflow(workflowID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#deleteWorkflow(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testDeleteWorkflow() throws IOException {		
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		assertNotNull(tavernaRESTClient.getWorkflow(workflowID));
		assertTrue(containsElement(tavernaRESTClient.getWorkflows().getWorkflow(), workflowID));
		tavernaRESTClient.deleteWorkflow(workflowID);
		assertNull(tavernaRESTClient.getWorkflow(workflowID));
		assertFalse(containsElement(tavernaRESTClient.getWorkflows().getWorkflow(), workflowID));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#getData(java.lang.String)}.
	 */
	@Test
	public void testGetData() {
		Map<String, DataValue> data = new HashMap<String, DataValue>();
		data.put("Input", new DataValue("path:map07025"));
		Long dataID = tavernaRESTClient.addData(new DataResource(data));
		assertEquals(data, tavernaRESTClient.getData(dataID).getDataMap());
		tavernaRESTClient.deleteData(dataID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#getJob(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testGetJob() throws IOException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-no-input.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		JobResource job = tavernaRESTClient.getJob(jobID);
		assertNotNull(job);
		assertEquals(jobID, job.getId());
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteWorkflow(workflowID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#getJobs()}.
	 * @throws IOException 
	 */
	@Test
	public void testGetJobs() throws IOException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-no-input.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		int jobCount = tavernaRESTClient.getJobs().getJob().size();
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		Collection<JobResource> jobs = tavernaRESTClient.getJobs().getJob();
		assertEquals(jobCount + 1, jobs.size());
		assertTrue(containsElement(jobs, jobID));
		tavernaRESTClient.deleteWorkflow(workflowID);
		tavernaRESTClient.deleteJob(jobID);
	}
	
	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#getJobStatus(java.lang.String)}.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testGetJobStatus() throws IOException, InterruptedException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-no-input.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		Long jobID = tavernaRESTClient.addJob(workflowID, null);
		String status = tavernaRESTClient.getJobStatus(jobID);
		System.out.println(status);
		assertNotNull(status);
		while(!tavernaRESTClient.getJobStatus(jobID).equals("COMPLETE")) {
			System.out.println("Waiting for job ["+jobID+"]to complete");
			Thread.sleep(1000);
		}
		tavernaRESTClient.deleteJob(jobID);
		tavernaRESTClient.deleteWorkflow(workflowID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#getWorkflow(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testGetWorkflow() throws IOException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-test.t2flow").openStream());
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		WorkflowResource workflow = tavernaRESTClient.getWorkflow(workflowID);
		assertNotNull(workflow);
		assertEquals(workflowID, workflow.getId());
		assertEquals(workflowXML, workflow.getXml());
		tavernaRESTClient.deleteWorkflow(workflowID);
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient#getWorkflows()}.
	 * @throws IOException 
	 */
	@Test
	public void testGetWorkflows() throws IOException {
		String workflowXML = IOUtils.toString(getClass().getResource("beanshell-test.t2flow").openStream());
		int workflowCount = tavernaRESTClient.getWorkflows().getWorkflow().size();
		Long workflowID = tavernaRESTClient.addWorkflow(workflowXML);
		Collection<WorkflowResource> workflows = tavernaRESTClient.getWorkflows().getWorkflow();
		assertEquals(workflowCount + 1, workflows.size());
		assertTrue(containsElement(workflows, workflowID));
		tavernaRESTClient.deleteWorkflow(workflowID);
	}
	
	private <T extends Resource> boolean containsElement(Collection<T> collection, Long id) {
		boolean containsElement = false;
		for (T element : collection) {
			if(id.equals(element.getId())) {
				containsElement = true;
				break;
			}
		}
		return containsElement;
	}

}
