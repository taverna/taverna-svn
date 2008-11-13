package net.sf.taverna.t2.provenance.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.item.WorkflowProvenanceItem;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.output.XMLOutputter;

public class InMemoryProvenanceConnector implements ProvenanceConnector {

	// private static Logger logger =
	// Logger.getLogger(InMemoryProvenanceConnector.class);

	private ArrayList<ProvenanceItem> provenanceCollection;

	private boolean runStarted = false;

	private String provenance;

	private int storedNumber = 0;

	private UUID randomUUID;

	private ReferenceService referenceService;

	private List<String> idList;

	private String identifier;

	public InMemoryProvenanceConnector() {
		idList = new ArrayList<String>();
		provenanceCollection = new ArrayList<ProvenanceItem>();
		// TODO needs to store this in the database somehow
		setSessionId(UUID.randomUUID().toString());
		System.out.println("new provenance session: " + randomUUID);
		// could use spring to get the reference service??
		// ApplicationContext appContext = new
		// RavenAwareClassPathXmlApplicationContext(
		// "inMemoryIntegrationTestsContext.xml");
		// referenceService = (ReferenceService)
		// appContext.getBean("t2reference.service.referenceService");
	}

	public String getProvenance() {
		return provenance;
	}

	public void saveProvenance(String annotation) {
		System.out.println(annotation);
		provenance = annotation;
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		System.out.println("***********ADD PROVENANCE*************");
		return provenanceCollection;
	}

	@SuppressWarnings("unchecked")
	public synchronized void store(ProvenanceItem provenanceItem) {
		// System.out.println("**********STORE************");
		//
		// int size = provenanceCollection.size();
		// System.out.println("Collection size is " + size + " , stored "
		// + storedNumber);
		// if (size > 0) {
		// Send the next event in the collection, since
		// it is asynch other events could have been stored but not
		// completed.
		// ProvenanceItem provItem = provenanceCollection.get(storedNumber);
		// String asString = provItem.getAsString();
		// String asString = provenanceItem.getAsString();
		// if (asString != null) {
		// // System.out.println("EVENT: " + provItem.getEventType());
		// System.out.println("EVENT: " + provenanceItem.getEventType());
		// // System.out.println(provItem.getEventType() + " " + asString);
		//
		// } else {
		// XMLOutputter outputter = new XMLOutputter();
		// // String outputString = outputter.outputString(provItem
		// // .getAsXML(referenceService));
		// // String outputString = outputter.outputString(provenanceItem
		// // .getAsXML(referenceService));
		// System.out.println("EVENT: " + provenanceItem.getEventType());
		// // System.out
		// // .println(provItem.getEventType() + " " + outputString);
		//
		// // }
		// }
		// remember that this one is complete
		// storedNumber++;
	}

	public void createDatabase() {
		// TODO Auto-generated method stub

	}

	public void deleteDatabase() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		return "In Memory";
	}

	public void setDBLocation(String location) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "In Memory Connector - use for testing only";
	}

	public void addProvenanceItem(ProvenanceItem provenanceItem) {
		// FIXME just store the event straight away, the required process id is
		// inside it so no need to bother about what it is
		// call store with this item and do what you need to do
		// not even sure we need this step and could just call store directly
		// if (provenanceItem instanceof WorkflowProvenanceItem && !runStarted)
		// {
		// runStarted = true;
		// provenanceCollection.add(provenanceItem);
		// provenanceItem.getIdentifier();
		// if (!idList.contains(provenanceItem.getIdentifier())) {
		// idList.add(provenanceItem.getIdentifier());
		// System.out.println(provenanceItem.getIdentifier() + " - "
		// + provenanceItem.getClass().getName() + " new id");
		// } else {
		// System.out.println(provenanceItem.getIdentifier() + " - "
		// + provenanceItem.getClass().getName());
		// }
		// store(provenanceItem);
		// } else {
		// provenanceCollection.add(provenanceItem);
		// provenanceItem.getIdentifier();
		// if (!idList.contains(provenanceItem.getIdentifier())) {
		// idList.add(provenanceItem.getIdentifier());
		// System.out.println(provenanceItem.getIdentifier() + " - "
		// + provenanceItem.getClass().getName() + " new id");
		// } else {
		System.out.println("ITEM: " + provenanceItem.getClass().getName()
				+ "\nOWNING PROCESS: " + provenanceItem.getProcessId()
				+ "\nIDENTIFIER: " + provenanceItem.getIdentifier()
				+ "\nPARENT: " + provenanceItem.getParentId());
		// }
		store(provenanceItem);
		// }
	}

	public void setSessionId(String identifier) {
		this.identifier = identifier;
	}

	public String getSessionId() {
		return identifier;
	}

	public ReferenceService getReferenceService() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
		// TODO Auto-generated method stub

	}

}
