package net.sf.taverna.t2.provenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class InMemoryProvenanceConnector implements ProvenanceConnector {

	private static Logger logger = Logger.getLogger(InMemoryProvenanceConnector.class);
	
	private ArrayList<ProvenanceItem> provenanceCollection;
	
	private String provenance;
	
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
		return provenanceCollection;
	}

	@SuppressWarnings("unchecked")
	public void store() {
		List<ProvenanceItem> copiedList = (List<ProvenanceItem>)provenanceCollection.clone();
		for (ProvenanceItem item:copiedList) {
			System.out.println(item.getAsXML());
			provenanceCollection.remove(item);
		}
	}

}
