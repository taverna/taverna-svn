package org.taverna.server.master.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;

@XmlRootElement
public class RunDescription extends DescriptionElement {
	public Expiry expiry;
	public Uri creationWorkflow, status, workingDirectory, securityContext;
	public ListenerList listeners;

	public static class Expiry extends Uri {
		@XmlValue
		public Date timeOfDeath;
		public Expiry(){}
		Expiry(TavernaRun r, UriBuilder ub) {
			super(ub);
			this.timeOfDeath = r.getExpiry();
		}
	}
	public static class ListenerList extends Uri {
		List<Uri> listener;
		public ListenerList(){}
		ListenerList(TavernaRun r, UriBuilder ub) {
			super(ub);
			listener = new ArrayList<Uri>(r.getListeners().size());
			for (Listener l: r.getListeners()) {
				listener.add(new Uri(ub.path("{name}"), l.getName()));
			}
		}
	}

	public RunDescription(){}
	public RunDescription(TavernaRun r, UriInfo ui) {
		UriBuilder ub = ui.getAbsolutePathBuilder();
		creationWorkflow = new Uri(ui, "workflow");
		expiry = new Expiry(r, ub.path("expiry"));
		status = new Uri(ui, "status");
		workingDirectory = new Uri(ui, "wd");
		listeners = new ListenerList(r, ub.path("listeners"));
		securityContext = new Uri(ui, "owner");
	}
}
