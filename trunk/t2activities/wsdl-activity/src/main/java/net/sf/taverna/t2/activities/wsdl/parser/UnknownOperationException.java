package net.sf.taverna.t2.activities.wsdl.parser;

/**
 * Exception thrown when a given service operation name cannot be found for that
 * WSDL
 * 
 * @author Stuart Owen
 * 
 */

public class UnknownOperationException extends Exception {
	private static final long serialVersionUID = -9119188266154359132L;

	UnknownOperationException(String val) {
		super(val);
	}
}
