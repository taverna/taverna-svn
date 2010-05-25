package org.taverna.server.master.example;

import static java.util.Calendar.MINUTE;
import static org.taverna.server.master.Status.Initialized;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.taverna.server.master.SCUFL;
import org.taverna.server.master.Status;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.factories.RunFactory;
import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.interfaces.TavernaSecurityContext;

public class ExampleRun implements TavernaRun, TavernaSecurityContext {
	List<Listener> listeners;
	SCUFL workflow;
	Status status;
	Date expiry;
	Principal owner;
	java.io.File realRoot;

	public ExampleRun(Principal creator, SCUFL workflow, Date expiry) {
		this.listeners = new ArrayList<Listener>();
		this.status = Initialized;
		this.owner = creator;
		this.workflow = workflow;
		this.expiry = expiry;
		listeners.add(new DefaultListener());
	}

	@Override
	public void addListener(Listener l) {
		listeners.add(l);
	}

	@Override
	public void destroy() {
		// This does nothing...
	}

	@Override
	public Date getExpiry() {
		return expiry;
	}

	@Override
	public List<Listener> getListeners() {
		return listeners;
	}

	@Override
	public TavernaSecurityContext getSecurityContext() {
		return this;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public SCUFL getWorkflow() {
		return workflow;
	}

	@Override
	public Directory getWorkingDirectory() {
		// TODO: Implement this!
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void setExpiry(Date d) {
		if (d.after(new Date()))
			this.expiry = d;
	}

	@Override
	public void setStatus(Status s) {
		this.status = s;
	}

	@Override
	public Principal getOwner() {
		return owner;
	}

	public static class Builder implements RunFactory {
		private int lifetime;
		public Builder(int initialLifetimeMinutes) {
			this.lifetime = initialLifetimeMinutes;
		}

		@Override
		public TavernaRun create(Principal creator, SCUFL workflow) {
			Calendar c = GregorianCalendar.getInstance();
			c.add(MINUTE, lifetime);
			return new ExampleRun(creator, workflow, c.getTime());
		}
	}

	static final String[] emptyArray = new String[0];
	class DefaultListener implements Listener {
		@Override
		public String getConfiguration() {
			return "";
		}

		@Override
		public String getName() {
			return "default";
		}

		@Override
		public String getType() {
			return "default";
		}

		@Override
		public String[] listProperties() {
			return emptyArray;
		}

		@Override
		public String getProperty(String propName) throws NoListenerException {
			throw new NoListenerException("no such property");
		}

		@Override
		public void setProperty(String propName, String value)
				throws NoListenerException {
			throw new NoListenerException("no such property");
		}
	}
}
