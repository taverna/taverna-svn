package net.sf.taverna.t2.service;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import net.sf.taverna.t2.provenance.SharedVocabulary;
import net.sf.taverna.t2.service.types.ProvenanceEventType;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class Provenance implements SharedVocabulary
{

	static boolean saveEvent = true;
	static boolean clearDB  = true;  // facilitates testing

	ProvenanceWriter pw = null;
	EventProcessor   ep = null;
	
	public Provenance() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
	
		pw = new ProvenanceWriter();  // singleton

		ep = new EventProcessor(pw); // singleton	
		
		// clear the DB prior to testing
		if (clearDB) pw.clearDB();
		
	}

	public String getString(){
		return "Hello World!";
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
	 */
	public void acceptRawProvenanceEvent(String eventType, String content) {

		System.out.println("raw event of type " + eventType);

//		parse the event into DOM
		SAXBuilder  b = new SAXBuilder();
		Document d;

		try {
			d = b.build (new StringReader(content));


//			saveEvent for debugging / testing
			if (saveEvent) {
				ep.saveEvent(d, eventType);
			}

			processEvent(d, eventType);

			
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



	/**
	 * parse d and generate SQL insert calls into the provenance DB
	 * @param d DOM for the event
	 * @param eventType see {@link SharedVocabulary}
	 */
	@SuppressWarnings("unchecked")
	private void processEvent(Document d, String eventType) {

		if (eventType.equals(SharedVocabulary.WORKFLOW_EVENT_TYPE)) {
			// process the workflow structure
			
			System.out.println("Provenanace: ************  processing event of type "+SharedVocabulary.WORKFLOW_EVENT_TYPE);
			ep.processWorkflowStructure(d);
			
		} else if (eventType.equals(SharedVocabulary.PROCESS_EVENT_TYPE)) {
			// process a "process execution" event
			System.out.println("Provenanace: ************  processing event of type "+SharedVocabulary.PROCESS_EVENT_TYPE);
			ep.processProcessEvent(d);
			
		} else {
			System.out.println("unknown event type");

		}

	}


	public void clearDB() throws SQLException {
		pw.clearDB();
	}



}  // end class
