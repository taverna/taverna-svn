package net.sf.taverna.t2.provenance;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;

import org.jdom.output.XMLOutputter;

import provenance.ProvenanceLocator;
import provenance.ProvenancePortType;

public class WebServiceProvenanceConnector implements ProvenanceConnector {

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
	public void store(DataFacade dataFacade) {
		webServiceConnector();

		int size = provenanceCollection.size();
		if (size > 0) {
			// WRONG --retrieve last event stored and send to the service for storage-- WRONG
			// You should actually send the next event in the collection, since
			// it is asynch other events could have been stored but not
			// completed. Confusing, you bet it is
			ProvenanceItem provItem = provenanceCollection.get(storedNumber);
			String asString = provItem.getAsString();
			// get type of provItem and send this info as well
			if (asString != null) {
				try {
					System.out
							.println("************saving provenance************");
					provenanceHttpPort.acceptRawProvenanceEvent(asString,
							"dataflow");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					System.out.println("Could not store provenance from xml"
							+ e);
				}
			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(dataFacade));
				try {
					System.out
							.println("************saving provenance************");
					provenanceHttpPort.acceptRawProvenanceEvent(outputString,
							"dataflow");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					System.out.println("Could not store provenance from xml"
							+ e);
				}
			}
		}
		//remember that this one is complete
		storedNumber++;
		// for (ProvenanceItem provItem : provenanceCollection) {
		// String asString = provItem.getAsString();
		// //get type of provItem and send this info as well
		// if (asString != null) {
		// try {
		// System.out.println("************saving provenance************");
		// provenanceHttpPort.acceptRawProvenanceEvent(asString,
		// "dataflow");
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// System.out.println("Could not store provenance from xml" + e);
		// }
		// } else {
		// XMLOutputter outputter = new XMLOutputter();
		// String outputString = outputter.outputString(provItem
		// .getAsXML(dataFacade));
		// try {
		// System.out.println("************saving provenance************");
		// provenanceHttpPort.acceptRawProvenanceEvent(outputString,
		// "dataflow");
		// } catch (RemoteException e) {
		// // TODO Auto-generated catch block
		// System.out.println("Could not store provenance from xml" + e);
		// }
		// }
		//
		// }
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
