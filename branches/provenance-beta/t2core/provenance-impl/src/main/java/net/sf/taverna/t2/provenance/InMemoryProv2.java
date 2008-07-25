package net.sf.taverna.t2.provenance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.reference.ReferenceService;

import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;

public class InMemoryProv2 implements ProvenanceConnector {

	private int storedNumber = 0;

	private static Logger logger = Logger
			.getLogger(InMemoryProvenanceConnector.class);

	private ArrayList<ProvenanceItem> provenanceCollection;

	private String provenance;

	private File file;

	public InMemoryProv2() {
		provenanceCollection = new ArrayList<ProvenanceItem>();

		file = new File("/home/ian/scratch/output.txt");

	}

	public String getProvenance() {
		return provenance;
	}

	public void saveProvenance(String annotation) {
		System.out.println(annotation);
		provenance = annotation;
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		// System.out.println("Someone has asked for the provenance
		// collection");
		return provenanceCollection;
	}

	@SuppressWarnings("unchecked")
	public void store(ReferenceService referenceService) {

		int size = provenanceCollection.size();
		if (size > 0) {
			// Send the next event in the collection, since
			// it is asynch other events could have been stored but not
			// completed.
			ProvenanceItem provItem = provenanceCollection.get(storedNumber);
			String asString = provItem.getAsString();
			if (asString != null) {

				System.out.println(provItem.getEventType() + " " + asString);

			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(referenceService));

				System.out
						.println(provItem.getEventType() + " " + outputString);

			}
		}
		// remember that this one is complete
		storedNumber++;
	}
}
