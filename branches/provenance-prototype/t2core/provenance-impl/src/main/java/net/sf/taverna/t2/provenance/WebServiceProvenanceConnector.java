package net.sf.taverna.t2.provenance;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.service.provenance.ProvenanceCollector;
import net.sf.taverna.t2.service.provenance.ProvenanceCollectorService;
import net.sf.taverna.t2.service.provenance.ProvenanceCollectorServiceLocator;

import org.jdom.output.XMLOutputter;

public class WebServiceProvenanceConnector implements ProvenanceConnector {

	private ArrayList<ProvenanceItem> provenanceCollection;
	private ProvenanceCollector provenanceCollector;

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
		for (ProvenanceItem provItem : provenanceCollection) {
			String asString = provItem.getAsString();
			if (asString != null) {
				try {
					System.out.println("************saving provenance************");
					provenanceCollector.acceptProvenanceRawEvent(asString,
							"dataflow");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					System.out.println("Could not store provenance from xml" + e);
				}
			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(dataFacade));
				try {
					System.out.println("************saving provenance************");
					provenanceCollector.acceptProvenanceRawEvent(outputString,
							"dataflow");
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					System.out.println("Could not store provenance from xml" + e);
				}
			}

		}
	}

	private void webServiceConnector() {
		if (provenanceCollector == null) {
			ProvenanceCollectorService provenanceCollectorService = new ProvenanceCollectorServiceLocator();

			try {
				provenanceCollector = provenanceCollectorService
						.getProvenance();
			} catch (ServiceException e) {
				System.out.println("Could not get provenance service " + e);
			}
		}
	}

}
