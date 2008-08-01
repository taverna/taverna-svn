package net.sf.taverna.t2.provenance.connector;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabularly.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.output.XMLOutputter;

import provenance.ProvenanceLocator;
import provenance.ProvenancePortType;

public class WebServiceProvenanceConnector implements ProvenanceConnector,
		SharedVocabulary {

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
	 * Get the string representation of the provenance event, otherwise get the
	 * XML version and convert to a string
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
					// TODO Auto-generated c			// get type of provItem and send this info as wellatch block
					System.out.println("Could not store provenance from xml"
							+ e);
				}			// get type of p			// get type of provItem and send this info as wellrovItem and send this info as well
			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(referenceService));
				try {
					provenanceHttpPort.acceptRawProvenanceEvent(provItem
							.getEventType(), outputString);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					System.out.println("Could not store provenance from xml"
							+ e);
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
				System.out.println("Could not get provenance service " + e);
			}
		}
	}

}
