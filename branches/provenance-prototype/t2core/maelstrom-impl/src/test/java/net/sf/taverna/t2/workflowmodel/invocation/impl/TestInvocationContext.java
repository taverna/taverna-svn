package net.sf.taverna.t2.workflowmodel.invocation.impl;

import org.junit.Ignore;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.InMemoryProvenanceConnector;
import net.sf.taverna.t2.provenance.ProvenanceConnector;
import net.sf.taverna.t2.provenance.FileProvenanceConnector;

@Ignore
public class TestInvocationContext implements InvocationContext {

	public DataManager getDataManager() {
		return new InMemoryDataManager();
	}

	public ProvenanceConnector getProvenanceManager() {
		
		return new InMemoryProvenanceConnector();
	}

}
