package net.sf.taverna.t2.provenance.connector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.jdom.output.XMLOutputter;


import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenance;
import net.sf.taverna.t2.reference.ReferenceService;

public class LocalProvenanceConnector  implements ProvenanceConnector{

	private ReferenceService rs = null;
	private Provenance provenance;

	public LocalProvenanceConnector() {
		try {
			provenance = new MySQLProvenance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		System.out.println("invoked: LocalConnector::getProvenanceCollection()");
		return null;
	}

	public void store(ReferenceService referenceService) {

		System.out.println("invoked: LocalConnector::store()");


	}


	/**
	 * main entry point into the service
	 */
	public void addProvenanceItem(ProvenanceItem provenanceItem) {

		String content = provenanceItem.getAsString();

		if (content != null) {

			System.out.println("EVENT: " + provenanceItem.getEventType());

		} else {

			XMLOutputter outputter = new XMLOutputter();
			content = outputter.outputString(provenanceItem.getAsXML(rs));
			System.out.println("EVENT: " + provenanceItem.getEventType());

		}

		try {
			provenance.acceptRawProvenanceEvent(provenanceItem.getEventType(), content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public void createDatabase() {
		// TODO Auto-generated method stub

	}

	public void deleteDatabase() {
		// TODO Auto-generated method stub

	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public ReferenceService getReferenceService() {
		return rs;
	}

	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDBLocation(String location) {
		// TODO Auto-generated method stub

	}

	public void setReferenceService(ReferenceService referenceService) {
		this.rs = referenceService;
	}

	public void setSessionId(String identifier) {
		// TODO Auto-generated method stub

	}

	public void store(ProvenanceItem provenanceItem) {
		// TODO Auto-generated method stub

	}

}
