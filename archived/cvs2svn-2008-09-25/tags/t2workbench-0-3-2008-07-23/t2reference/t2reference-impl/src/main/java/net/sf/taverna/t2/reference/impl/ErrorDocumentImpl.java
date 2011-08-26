package net.sf.taverna.t2.reference.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.StackTraceElementBean;
import net.sf.taverna.t2.reference.h3.HibernateMappedEntity;

/**
 * Simple bean implementation of ErrorDocument
 * 
 * @author Tom Oinn
 * 
 */
public class ErrorDocumentImpl extends AbstractEntityImpl implements
		ErrorDocument, HibernateMappedEntity {

	private String exceptionMessage = "";
	private String message = "";
	List<StackTraceElementBean> stackTrace;
	
	public ErrorDocumentImpl() {
		this.stackTrace = new ArrayList<StackTraceElementBean>();
	}

	public String getExceptionMessage() {
		return this.exceptionMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * From interface, not used by hibernate internally
	 */
	public List<StackTraceElementBean> getStackTraceStrings() {
		return this.stackTrace;
	}

	/**
	 * Used by Hibernate to bodge around problems with interface types in the
	 * API
	 */
	@SuppressWarnings("unchecked")
	public void setStackTraceList(List newList) {
		this.stackTrace = newList;
	}

	/**
	 * Used by Hibernate to bodge around problems with interface types in the
	 * API
	 */
	@SuppressWarnings("unchecked")
	public List getStackTraceList() {
		return this.stackTrace;
	}

}
