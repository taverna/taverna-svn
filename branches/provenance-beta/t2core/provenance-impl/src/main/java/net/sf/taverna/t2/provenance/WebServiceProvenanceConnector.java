package net.sf.taverna.t2.provenance;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.reference.ReferenceService;

import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;

import provenance.ProvenanceLocator;
import provenance.ProvenancePortType;

public class WebServiceProvenanceConnector implements ProvenanceConnector,
		SharedVocabulary {

	private static Logger logger = Logger
			.getLogger(WebServiceProvenanceConnector.class);

	private ArrayList<ProvenanceItem> provenanceCollection;

	// private Provenance provenanceCollector;
	private ProvenancePortType provenanceHttpPort;

	// * Keep a note of what part of the provenance collection was last sent */
	private int storedNumber = 0;

	public WebServiceProvenanceConnector() {
		provenanceCollection = new ArrayList<ProvenanceItem>();
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		return provenanceCollection;
	}

	/**
	 * Get the xml formatted string representation of the provenance event
	 */
	public synchronized void store(ReferenceService referenceService) {
		webServiceConnector();

		int size = provenanceCollection.size();
		if (size > 0) {
			// Send the next event in the collection, since
			// it is asynch other events could have been stored but not
			// completed.
			ProvenanceItem provItem = provenanceCollection.get(storedNumber);
			String asString = provItem.getAsString();
			if (asString != null) {
				try {
					provenanceHttpPort.acceptRawProvenanceEvent(provItem
							.getEventType(), asString);
				} catch (RemoteException e) {
					logger.warn("Could not store provenance from xml" + e);
				}
			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(referenceService));
				try {
					provenanceHttpPort.acceptRawProvenanceEvent(provItem
							.getEventType(), outputString);
				} catch (RemoteException e) {
					logger.warn("Could not store provenance from xml" + e);
				}
			}
		}
		// remember that this one is complete
		storedNumber++;
	}

	private void webServiceConnector() {
		if (provenanceHttpPort == null) {
			ProvenanceLocator provenanceCollectorService = new ProvenanceLocator();

			try {
				provenanceHttpPort = provenanceCollectorService
						.getProvenanceHttpPort();
			} catch (ServiceException e) {
				logger.warn("Could not get provenance service" + e);
			}
		}
	}

}
