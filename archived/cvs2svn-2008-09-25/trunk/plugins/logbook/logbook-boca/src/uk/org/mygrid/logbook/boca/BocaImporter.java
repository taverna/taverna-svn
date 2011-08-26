package uk.org.mygrid.logbook.boca;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;

public class BocaImporter {
	
	private Logger logger = Logger.getLogger(BocaImporter.class);
	
	private Properties legacyConfiguration;
	
	private Properties bocaConfiguration;

	private MetadataService legacyService;

	private MetadataService bocaService;

	public BocaImporter(Properties legacyConfiguration, Properties bocaConfiguration) throws MetadataServiceCreationException {
		this.legacyConfiguration = legacyConfiguration;
		this.bocaConfiguration = bocaConfiguration;
		legacyService = MetadataServiceFactory.getInstance(legacyConfiguration);
		bocaService = MetadataServiceFactory.getInstance(bocaConfiguration);
	}
	
	public void transfer() throws MetadataServiceException {
		Set<String> allWorkflowRuns = new HashSet<String>(legacyService.getAllWorkflowRuns());
		for (String workflowRunId : allWorkflowRuns) {
			String workflowRun = legacyService.retrieveGraph(workflowRunId);
			logger.debug("Transferring " + workflowRunId + " = " + workflowRun);
			bocaService.storeRDFGraph(workflowRun, workflowRunId);
			List<String> processesRunsIds = legacyService.getNonNestedProcessRuns(workflowRunId);
			for (String processRunId : processesRunsIds) {
				String processRun = legacyService.retrieveGraph(processRunId);
				bocaService.storeRDFGraph(processRun, processRunId);
			}
		}
	}

	public Properties getBocaConfiguration() {
		return bocaConfiguration;
	}

	public MetadataService getBocaService() {
		return bocaService;
	}

	public Properties getLegacyConfiguration() {
		return legacyConfiguration;
	}

	public MetadataService getLegacyService() {
		return legacyService;
	}

}
