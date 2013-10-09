/**
 * 
 */
package net.sf.taverna.t2.component.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alanrw
 * 
 */
public class ExceptionHandling {
	private final uk.org.taverna.ns._2012.component.profile.ExceptionHandling proxied;

	private List<HandleException> handleExceptions = new ArrayList<HandleException>();

	public ExceptionHandling(
			uk.org.taverna.ns._2012.component.profile.ExceptionHandling proxied) {
		this.proxied = proxied;
	}

	public boolean failLists() {
		return (proxied.getFailLists() != null);
	}

	public List<HandleException> getHandleExceptions() {
		if (handleExceptions.isEmpty()
				&& !proxied.getHandleException().isEmpty()) {
			for (uk.org.taverna.ns._2012.component.profile.HandleException he : proxied
					.getHandleException()) {
				handleExceptions.add(new HandleException(he));
			}
		}
		return handleExceptions;
	}
}
