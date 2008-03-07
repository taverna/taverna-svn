package net.sf.taverna.t2.cloudone.entity;

import java.io.PrintWriter;
import java.io.StringWriter;

import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;

/**
 * Represents a single error document within the data manager. The error
 * document contains a {@link Throwable} and a message, either of which may be
 * <code>null</code>, but not both.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class ErrorDocument implements
		Entity<ErrorDocumentIdentifier, ErrorDocumentBean> {
	private ErrorDocumentIdentifier identifier;

	private String message;

	private Throwable cause;

	private String stackTrace;

	/**
	 * Construct an ErrorDocument that is to be configured from
	 * {@link #setFromBean(ErrorDocumentBean)}.
	 * 
	 */
	public ErrorDocument() {
		identifier = null;
		message = null;
		cause = null;
	}

	/**
	 * Construct an ErrorDocument from an identifier, message and/or cause.
	 * Either of <code>message</code> and <code>cause</code> can be
	 * <code>null</code>, but not both.
	 * 
	 * @param identifier
	 *            The identifier as created by the
	 *            {@link net.sf.taverna.t2.cloudone.datamanager.DataManager}
	 * @param message
	 *            The optional error message
	 * @param cause
	 *            The optional cause as a {@link Throwable}
	 */
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	/**
	 * Check equality with another object. Two {@link ErrorDocument}s are
	 * considered equal if they have the same {@link #getIdentifier()}.
	 * 
	 * @param obj
	 *            The object to check equality against
	 * @return true if and only if <code>obj</code> is an
	 *         {@link ErrorDocument} and it's {@link #getIdentifier()} equals
	 *         the identifier of this ErrorDocument.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ErrorDocument other = (ErrorDocument) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	/**
	 * Get as a serialisable {@link ErrorDocumentBean}. Note that
	 * {@link #getCause()} will not be serialised, although it's
	 * {@link #getStackTrace()} will.
	 * 
	 * @return A serialisable {@link ErrorDocumentBean} configured from this
	 *         ErrorDocument.
	 */
	public ErrorDocumentBean getAsBean() {
		ErrorDocumentBean bean = new ErrorDocumentBean();
		bean.setIdentifier(identifier.getAsURI());
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

	/**
	 * Initialise from an {@link ErrorDocumentBean}. This method can only be
	 * called once and only if the empty constructor {@link #ErrorDocument()}
	 * was used.
	 * 
	 * @see #getAsBean()
	 * @see net.sf.taverna.t2.util.beanable.Beanable#getAsBean()
	 * @param bean
	 *            The {@link ErrorDocumentBean} from where to initialise
	 */
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
