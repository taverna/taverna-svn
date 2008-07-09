package net.sf.taverna.t2.provenance;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.events.DispatchErrorType;

import org.jdom.Element;

public class ErrorProvenanceItem implements ProvenanceItem {

	private Throwable cause;
	private String message;
	private DispatchErrorType errorType;
	
	
	
	public ErrorProvenanceItem(Throwable cause, String message,
			DispatchErrorType errorType) {
		super();
		this.cause = cause;
		this.message = message;
		this.errorType = errorType;
	}



	public Element getAsXML(DataFacade dataFacade) {
		Element result = new Element("error");
		result.setAttribute("message",message);
		result.setAttribute("type",errorType.toString());
		Element causeElement = new Element("cause");
		String st="";
		for (StackTraceElement trace : cause.getStackTrace()) {
			st+=trace.toString();
			st+="\n";
		}
		causeElement.setText(st);
		result.addContent(causeElement);
		return result;
	}



	public String getAsString() {
		// TODO Auto-generated method stub
		return null;
	}



	public String getEventType() {
		return SharedVocabulary.ERROR_EVENT_TYPE;
	}

}
