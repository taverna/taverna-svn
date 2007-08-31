package net.sf.taverna.t2.cloudone.entity;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

/**
 * Represents a single error document within the data manager. The error
 * document contains a Throwable and a message, either of which may be null.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * 
 */
public class ErrorDocument implements Entity<ErrorDocumentIdentifier, ErrorDocumentBean> {
	private ErrorDocumentIdentifier id;

	private String message;

	private Throwable cause;

	private String stackTrace;
	
	public ErrorDocument() {
		this.id = null;
		this.message = null;
		this.cause = null;
	}
	
	public ErrorDocument(final ErrorDocumentIdentifier id,
			final String message, final Throwable cause) {
		if (message == null && cause == null) {
			throw new NullPointerException("Both message or cause can't be null");
		}
		this.id = id;
		this.message = message;
		this.cause = cause;
	}

	public ErrorDocumentIdentifier getIdentifier() {
		return id;
	}

	public Throwable getCause() {
		return this.cause;
	}

	public String getMessage() {
		return this.message;
	}

	public ErrorDocumentBean getAsBean() {
		ErrorDocumentBean bean = new ErrorDocumentBean();
		bean.setId(this.id.getAsBean());
		bean.setMessage(this.message);
		bean.setStackTrace(getStackTrace());
		return bean;
	}

	public void setFromBean(ErrorDocumentBean bean) {
		if (id != null) { 
			throw new IllegalStateException("Already initialised");
		}
		id = (ErrorDocumentIdentifier) EntityIdentifiers.parse(bean.getId());
		message = bean.getMessage();
		stackTrace = bean.getStackTrace();
	}

	/**
	 * Get the stacktrace from the cause of this error. The stacktrace might be
	 * available even if {@link #getCause()} is null, as the cause is not
	 * serialised by {@link #getAsBean()}.
	 * 
	 * @return The stacktrace of the cause, or null if no cause was set.
	 */
	public String getStackTrace() {
		if (stackTrace == null && cause != null) {
			StringWriter sw = new StringWriter();
			cause.printStackTrace(new PrintWriter(sw));
			stackTrace = sw.toString();
		}
		return stackTrace;
	}

}
