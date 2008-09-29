package net.sf.taverna.t2.provenance.connector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import net.sf.taverna.t2.lineageService.Provenance;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.reference.ReferenceService;

import org.jdom.output.XMLOutputter;

public class LocalProvenanceConnector implements ProvenanceConnector {

//	private static Logger logger = Logger.getLogger(InMemoryProvenanceConnector.class);
	
//	private static Provenance pService = null;
	
	private ArrayList<ProvenanceItem> provenanceCollection;
	
	private String provenance;
	
	private int storedNumber=0;
	
	public LocalProvenanceConnector() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		provenanceCollection = new ArrayList<ProvenanceItem>();
		
		// PM init the service as a local class -- assume multiple threads may call the methods
		// how is this serialised??
//		pService = new Provenance();
		
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
		System.out.println("**********STORE BLA BLA ************");

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
				
//				try {
//					pService.acceptRawProvenanceEvent(provItem.getEventType(), asString);
//				} catch (SQLException e) {
//					System.out.println("SQL Exception while invoking local provenance service" + e);					
//					e.printStackTrace();
//				} catch (IOException e) {
//					System.out.println("IOException while invoking local provenance service" + e);					
//					e.printStackTrace();
//				}
				
			} else {
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(provItem
						.getAsXML(referenceService));

//				try {
////					pService.acceptRawProvenanceEvent(provItem.getEventType(), outputString);
//				} catch (SQLException e) {
//					System.out.println("SQL Exception while invoking local provenance service" + e);					
//					e.printStackTrace();
//				} catch (IOException e) {
//					System.out.println("IOException while invoking local provenance service" + e);					
//					e.printStackTrace();
//				}

			}
		}
		// remember that this one is complete
		storedNumber++;
	}

}
