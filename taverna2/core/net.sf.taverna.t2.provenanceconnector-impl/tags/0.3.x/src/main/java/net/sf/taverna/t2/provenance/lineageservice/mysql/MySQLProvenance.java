package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.Map;

import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.types.ProvenanceEventType;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * implements the WS interface -- only exposed method is {@link #acceptRawProvenanceEvent(String, String)}
 * @author paolo
 *
 */
public class MySQLProvenance implements Provenance, SharedVocabulary
{

	static boolean saveEvent = true;
	static boolean clearDB  = true;  // facilitates testing

	static Logger logger = Logger.getLogger(MySQLProvenance.class);

	ProvenanceWriter     pw = null;
	ProvenanceQuery      pq = null;
	EventProcessor       ep = null;
	

	public MySQLProvenance() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		pw = new MySQLProvenanceWriter();  // singleton
		pq = new MySQLProvenanceQuery();   // singleton
		
		ep = new EventProcessor(pw,pq); // singleton	
		
		
		// clear the DB prior to testing  CHECK
		if (clearDB) {
			System.out.println("clearing DB");
			pw.clearDB();
		}
		
	}

	public String getString(){
		return "Paolo's provenance service";
	}

	/**
	 * not used -- @see #acceptRawProvenanceEvent(String, String)
	 * @param eventType
	 * @param content
	 */
	public void acceptProvenanceEvent(ProvenanceEventType eventType, String content)
	{
		; // TODO 
	}

	
	/**
	 * maps each incoming event to an insert query into the provenance store
	 * @param eventType
	 * @param content
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public void acceptRawProvenanceEvent(String eventType, String content) throws SQLException, IOException {

		System.out.println("raw event of type " + eventType);
		//System.out.println("content \n"+content);

			processEvent(content, eventType);
	}

	/**
	 * parse d and generate SQL insert calls into the provenance DB
	 * @param d DOM for the event
	 * @param eventType see {@link SharedVocabulary}
	 * @throws SQLException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	private void processEvent(String content, String eventType) throws SQLException, IOException {

//		saveEvent for debugging / testing
		if (saveEvent) {
			ep.saveEvent(content, eventType);
			
			System.out.println("event saved");
		}

		
		if (eventType.equals(SharedVocabulary.WORKFLOW_EVENT_TYPE)) {
			// process the workflow structure
						
			System.out.println("Provenance: ************  processing event of type "+SharedVocabulary.WORKFLOW_EVENT_TYPE);
			String wfinstanceRef = ep.processWorkflowStructure(content);
			
			// add propagation of anl code here
			ep.propagateANL(wfinstanceRef);   // operates on the DB
			
		} else if (eventType.equals(SharedVocabulary.PROCESS_EVENT_TYPE)) {
			
//			parse the event into DOM
			SAXBuilder  b = new SAXBuilder();
			Document d;

			try {
				d = b.build (new StringReader(content));
			
			System.out.println("Provenance: ************  processing event of type "+SharedVocabulary.PROCESS_EVENT_TYPE);
			ep.processProcessEvent(d);
			
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if (eventType.equals("EOW")) {  //SharedVocabulary.END_WORKFLOW_EVENT_TYPE)) {
			// use this event to do housekeeping on the input/output varbindings 
			System.out.println("Provenance: ************  end of workflow processing");
			ep.fillInputVarBindings();  // indep. of current event
			ep.fillOutputVarBindings();
			
			// load up any annotations associated with this workflow  TODO
	
		} else {
			System.out.println("unknown event type:"+eventType);

		}

	}


	
	public void clearDB() throws SQLException {
		pw.clearDB();
	}

	public void createDB() throws SQLException {
		// TODO Auto-generated method stub
		
	}


	public void setLocation(String location) {
		// TODO Auto-generated method stub
		
	}




}  // end class
