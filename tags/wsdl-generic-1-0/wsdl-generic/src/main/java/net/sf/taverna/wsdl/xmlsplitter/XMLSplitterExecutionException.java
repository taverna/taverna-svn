package net.sf.taverna.wsdl.xmlsplitter;

public class XMLSplitterExecutionException extends Exception {

	private static final long serialVersionUID = 5623707293500493612L;

	public XMLSplitterExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public XMLSplitterExecutionException(String msg) {
		super(msg);
	}
}
