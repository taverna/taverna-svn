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
public class ErrorDocument implements
		Entity<ErrorDocumentIdentifier, ErrorDocumentBean> {
	private ErrorDocumentIdentifier identifier;

	private String message;

	private Throwable cause;

	private String stackTrace;

	public ErrorDocument() {
		identifier = null;
		message = null;
		cause = null;
	}

	public ErrorDocument(final ErrorDocumentIdentifier identifier,
			final String message, final Throwable cause) {
		if (message == null && cause == null) {
			throw new NullPointerException(
					"Both message or cause can't be null");
		}
		this.identifier = identifier;
		this.message = message;
		this.cause = cause;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ErrorDocument other = (ErrorDocument) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		return true;
	}

	public ErrorDocumentBean getAsBean() {
		ErrorDocumentBean bean = new ErrorDocumentBean();
		bean.setIdentifier(identifier.getAsBean());
		bean.setMessage(message);
		bean.setStackTrace(getStackTrace());
		return bean;
	}

	public Throwable getCause() {
		return cause;
	}

	public ErrorDocumentIdentifier getIdentifier() {
		return identifier;
	}

	public String getMessage() {
		return message;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	public void setFromBean(ErrorDocumentBean bean) {
		if (identifier != null) {
			throw new IllegalStateException("Already initialised");
		}
		identifier = (ErrorDocumentIdentifier) EntityIdentifiers.parse(bean
				.getIdentifier());
		message = bean.getMessage();
		stackTrace = bean.getStackTrace();
	}

}
