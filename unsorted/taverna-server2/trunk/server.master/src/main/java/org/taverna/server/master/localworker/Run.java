package org.taverna.server.master.localworker;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.taverna.server.master.SCUFL;
import org.taverna.server.master.Status;
import org.taverna.server.master.exceptions.BadPropertyValueException;
import org.taverna.server.master.exceptions.BadStateChangeException;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.Input;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.interfaces.TavernaSecurityContext;

public class Run implements TavernaRun, Listener, TavernaSecurityContext {
	private SCUFL workflow;
	private List<Listener> listeners;
	private Date expiry;
	private Principal creator;
	Status s;
	String inputBaclava;
	private String outputBaclava;
	List<RunInput> inputs;
	private Map<String, String> properties;

	Run(Principal creator, SCUFL workflow) {
		this.creator = creator;
		this.listeners = new ArrayList<Listener>();
		this.properties = new HashMap<String, String>();
		this.workflow = workflow;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 20);
		this.expiry = c.getTime();
		this.s = Status.Initialized;
		addListener(this);
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

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
		return s;
	}

	@Override
	public SCUFL getWorkflow() {
		return workflow;
	}

	@Override
	public Directory getWorkingDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExpiry(Date d) {
		if (d.after(new Date()))
			expiry = d;
	}

	@Override
	public void setStatus(Status s) throws BadStateChangeException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getConfiguration() {
		return "";
	}

	@Override
	public String getName() {
		return "default";
	}

	@Override
	public String getProperty(String propName) throws NoListenerException {
		if (!properties.containsKey(propName))
			throw new NoListenerException("no such property");
		return properties.get(propName);
	}

	@Override
	public String getType() {
		return "default";
	}

	@Override
	public String[] listProperties() {
		return properties.keySet().toArray(new String[properties.size()]);
	}

	@Override
	public void setProperty(String propName, String value)
			throws NoListenerException, BadPropertyValueException {
		// TODO change this?
		throw new BadPropertyValueException("setting not supported");
	}

	@Override
	public Principal getOwner() {
		return creator;
	}

	static void checkBadFilename(String filename)
			throws FilesystemAccessException {
		if (filename.startsWith("/"))
			throw new FilesystemAccessException("filename may not be absolute");
		if (Arrays.asList(filename.split("/")).contains(".."))
			throw new FilesystemAccessException(
					"filename may not refer to parent");
	}

	class RunInput implements Input {
		private final String name;
		String file, value;

		RunInput(String name) {
			this.name = name;
		}

		@Override
		public String getFile() {
			return file;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setFile(String file) throws FilesystemAccessException,
				BadStateChangeException {
			if (s != Status.Initialized)
				throw new BadStateChangeException();
			checkBadFilename(file);
			this.file = file;
			this.value = null;
			inputBaclava = null;
		}

		@Override
		public void setValue(String value) throws BadStateChangeException {
			if (s != Status.Initialized)
				throw new BadStateChangeException();
			this.value = value;
			this.file = null;
			inputBaclava = null;
		}

		void reset() {
			value = file = null;
		}
	}

	@Override
	public String getInputBaclavaFile() {
		return inputBaclava;
	}

	@Override
	@SuppressWarnings("unchecked")
	// OK; list is unmodifiable
	public List<Input> getInputs() {
		return (List) Collections.unmodifiableList(inputs);
	}

	@Override
	public String getOutputBaclavaFile() {
		return outputBaclava;
	}

	@Override
	public Input makeInput(String name) throws BadStateChangeException {
		if (s != Status.Initialized)
			throw new BadStateChangeException();
		RunInput r = new RunInput(name);
		inputs.add(r);
		return r;
	}

	@Override
	public void setInputBaclavaFile(String filename)
			throws FilesystemAccessException, BadStateChangeException {
		if (s != Status.Initialized)
			throw new BadStateChangeException();
		checkBadFilename(filename);
		inputBaclava = filename;
		for (RunInput i : inputs)
			i.reset();
	}

	@Override
	public void setOutputBaclavaFile(String filename)
			throws FilesystemAccessException, BadStateChangeException {
		if (s != Status.Initialized)
			throw new BadStateChangeException();
		checkBadFilename(filename);
		outputBaclava = filename;
	}
}
