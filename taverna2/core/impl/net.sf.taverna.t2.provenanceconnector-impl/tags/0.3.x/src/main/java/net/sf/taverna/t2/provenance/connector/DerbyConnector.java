package net.sf.taverna.t2.provenance.connector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenance;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.output.XMLOutputter;

public class DerbyConnector implements ProvenanceConnector {

//	private static Logger logger = Logger.getLogger(InMemoryProvenanceConnector.class);
	
	private ArrayList<ProvenanceItem> provenanceCollection;
	
	private String provenance;
	
	private int storedNumber=0;

	private String name;

	private Provenance localProvenance;

	private ReferenceService referenceService;
	
	public DerbyConnector() {
		
		provenanceCollection = new ArrayList<ProvenanceItem>();
		name = "Local Derby DB";
		
		try {
			
			localProvenance= new DerbyProvenance();
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
		createDatabase();
		
		
		
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
		System.out.println("**********STORE************");

		int size = provenanceCollection.size();
		System.out.println("Collection size is " + size + " , stored " + storedNumber);
		if (size > 0) {
			// Send the next event in the collection, since
			// it is asynch other events could have been stored but not
			// completed.
//			ProvenanceItem provItem = provenanceCollection.get(storedNumber);
			String asString = provenanceItem.getAsString();
			System.out.println("Collection item " + storedNumber);
			if (asString != null) {
				try {
					localProvenance.acceptRawProvenanceEvent(provenanceItem.getEventType(), asString);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("EVENT: " + provenanceItem.getEventType());
//				System.out.println(provItem.getEventType() + " " + asString);

			} else {
				XMLOutputter outputter = new XMLOutputter();
				System.out.println("derby connector calling serialiser");
				String outputString = outputter.outputString(provenanceItem
						.getAsXML(getReferenceService()));
				System.out
				.println("EVENT: " + provenanceItem.getEventType());
				try {
					localProvenance.acceptRawProvenanceEvent(provenanceItem.getEventType(), outputString);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out
//						.println(provItem.getEventType() + " " + outputString);

			}
		}
		// remember that this one is complete
		storedNumber++;
	}

	public void createDatabase() {
		try {
			localProvenance.createDB();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteDatabase() {
		try {
			localProvenance.clearDB();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public String getName() {
		return this.name;
	}

	public void setDBLocation(String location) {
		localProvenance.setLocation(location);
	}

	@Override
	public String toString() {
		return "Derby DB Connector";
	}

	public void addProvenanceItem(ProvenanceItem provenanceItem) {
		// TODO Auto-generated method stub
		provenanceCollection.add(provenanceItem);
		store(provenanceItem);
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public ReferenceService getReferenceService() {
		if (referenceService == null) {
			System.out.println("ref service null in the Derby connector");
		} else {
			System.out.println("ref service not null in the Dery connector");
		}
		return referenceService;
	}

	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSessionId(String identifier) {
		// TODO Auto-generated method stub
		
	}
}
