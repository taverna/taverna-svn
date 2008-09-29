package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.vocabularly.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;
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



	public Element getAsXML(ReferenceService referenceService) {
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
		return null;
	}



	public String getEventType() {
		return SharedVocabulary.ERROR_EVENT_TYPE;
	}



	public Throwable getCause() {
		return cause;
	}



	public void setCause(Throwable cause) {
		this.cause = cause;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public DispatchErrorType getErrorType() {
		return errorType;
	}



	public void setErrorType(DispatchErrorType errorType) {
		this.errorType = errorType;
	}

}
