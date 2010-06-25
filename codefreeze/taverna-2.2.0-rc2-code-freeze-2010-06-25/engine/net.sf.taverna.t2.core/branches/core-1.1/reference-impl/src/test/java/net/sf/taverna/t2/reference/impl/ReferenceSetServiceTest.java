package net.sf.taverna.t2.reference.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetDao;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;

import org.junit.Before;
import org.junit.Test;

public class ReferenceSetServiceTest {
	
	private List<ReferenceSetServiceImpl> serviceList = new ArrayList<ReferenceSetServiceImpl>();

	@Before
	public void setup() throws Exception {

		AppContextSetup.setup();

		ReferenceSetServiceImpl service = null;

		service = new ReferenceSetServiceImpl();
		service.setReferenceSetDao((ReferenceSetDao)AppContextSetup.contextList.get(0).getBean("testDao")); // hibernate
		service.setT2ReferenceGenerator(new SimpleT2ReferenceGenerator());	
		serviceList.add(service);
		
		service = new ReferenceSetServiceImpl();
		service.setReferenceSetDao((ReferenceSetDao)AppContextSetup.contextList.get(1).getBean("testDao")); // in memory
		service.setT2ReferenceGenerator(new SimpleT2ReferenceGenerator());	
		serviceList.add(service);
		
		service = new ReferenceSetServiceImpl();
		service.setReferenceSetDao((ReferenceSetDao)AppContextSetup.contextList.get(2).getBean("testDao")); // transactional hibernate
		service.setT2ReferenceGenerator(new SimpleT2ReferenceGenerator());	
		serviceList.add(service);

	}
	
	@Test
	public void testDelete() throws Exception {
		ReferenceContextImpl invocationContext = new ReferenceContextImpl();
		invocationContext.addEntity(new WorkflowRunIdEntity("wfRunRefSetTest0"));
		for (ReferenceSetServiceImpl service : serviceList){
			ReferenceSet set = service.registerReferenceSet(new HashSet(), invocationContext);
			assertNotNull(service.getReferenceSet(set.getId()));
			assertTrue(service.delete(set.getId()));
			assertNull(service.getReferenceSet(set.getId()));
			assertFalse(service.delete(set.getId()));
		}
	}
	
	@Test
	public void testDeleteReferenceSetsForWFRun() throws Exception {

		for (ReferenceSetServiceImpl service : serviceList){
			
			String wfRunId1 = "wfRunRefSetTest1";
			ReferenceContextImpl invocationContext1 = new ReferenceContextImpl();
			invocationContext1.addEntity(new WorkflowRunIdEntity(wfRunId1));
			
			String wfRunId2 = "wfRunRefSetTest2";
			ReferenceContextImpl invocationContext2 = new ReferenceContextImpl();
			invocationContext2.addEntity(new WorkflowRunIdEntity(wfRunId2));
			
			ReferenceSet set1 = service.registerReferenceSet(new HashSet(), invocationContext1);
			ReferenceSet set2 = service.registerReferenceSet(new HashSet(), invocationContext1);
			ReferenceSet set3 = service.registerReferenceSet(new HashSet(), invocationContext2);

			assertNotNull(service.getReferenceSet(set1.getId()));
			assertNotNull(service.getReferenceSet(set2.getId()));
			assertNotNull(service.getReferenceSet(set3.getId()));

			service.deleteReferenceSetsForWorkflowRun(wfRunId1);
			
			assertNull(service.getReferenceSet(set1.getId()));
			assertNull(service.getReferenceSet(set2.getId()));
			assertNotNull(service.getReferenceSet(set3.getId()));

		}
	}

}
