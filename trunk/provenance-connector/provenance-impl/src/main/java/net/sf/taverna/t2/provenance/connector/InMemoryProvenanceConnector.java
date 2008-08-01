package net.sf.taverna.t2.provenance.connector;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.output.XMLOutputter;

public class InMemoryProvenanceConnector implements ProvenanceConnector {

//	private static Logger logger = Logger.getLogger(InMemoryProvenanceConnector.class);
	
	private ArrayList<ProvenanceItem> provenanceCollection;
	
	private String provenance;
	
	private int storedNumber=0;
	
	public InMemoryProvenanceConnector() {
		provenanceCollection = new ArrayList<ProvenanceItem>();
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
	public synchronized void store(ReferenceService referenceService) {
		System.out.println("**********STORE************");

		int size = provenanceCollection.size();
		System.out.println("Collection size is " + size + " , stored " + storedNumber);
		if (size > 0) {
			// Send the next event in the collection, since
			// it is asynch other events could have been stored but not
			// completed.
			ProvenanceItem provItem = provenanceCollection.get(storedNumber);
			String asString = provItem.getAsString();
			System.out.println("Collection item " + storedNumber);
			if (asString != null) {
				System.out.println("EVENT: " + provItem.getEventType());
//				System.out.println(provItem.getEventType() + " " + asString);

			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(referenceService));
				System.out
				.println("EVENT: " + provItem.getEventType());
//				System.out
//						.println(provItem.getEventType() + " " + outputString);

			}
		}
		// remember that this one is complete
		storedNumber++;
	}

}
