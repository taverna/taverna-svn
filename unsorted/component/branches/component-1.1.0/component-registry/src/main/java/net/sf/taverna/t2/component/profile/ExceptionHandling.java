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
	private final boolean failLists;
	private final List<HandleException> remapped = new ArrayList<HandleException>();

	public ExceptionHandling(
			uk.org.taverna.ns._2012.component.profile.ExceptionHandling proxied) {
		for (uk.org.taverna.ns._2012.component.profile.HandleException he : proxied
				.getHandleException())
			remapped.add(new HandleException(he));
		this.failLists = proxied.getFailLists() != null;
	}

	public boolean failLists() {
		return failLists;
	}

	public List<HandleException> getHandleExceptions() {
		return remapped;
	}
}
