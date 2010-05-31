package org.taverna.server.master.localworker;

import static org.taverna.server.master.localworker.AbstractRemoteRunFactory.log;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.taverna.server.localworker.remote.RemoteDirectory;
import org.taverna.server.localworker.remote.RemoteDirectoryEntry;
import org.taverna.server.localworker.remote.RemoteFile;
import org.taverna.server.localworker.remote.RemoteInput;
import org.taverna.server.localworker.remote.RemoteListener;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.localworker.remote.RemoteStatus;
import org.taverna.server.master.SCUFL;
import org.taverna.server.master.Status;
import org.taverna.server.master.exceptions.BadPropertyValueException;
import org.taverna.server.master.exceptions.BadStateChangeException;
import org.taverna.server.master.exceptions.FilesystemAccessException;
import org.taverna.server.master.exceptions.NoListenerException;
import org.taverna.server.master.interfaces.Directory;
import org.taverna.server.master.interfaces.DirectoryEntry;
import org.taverna.server.master.interfaces.File;
import org.taverna.server.master.interfaces.Input;
import org.taverna.server.master.interfaces.Listener;
import org.taverna.server.master.interfaces.TavernaRun;
import org.taverna.server.master.interfaces.TavernaSecurityContext;

public class RemoteRunDelegate implements TavernaRun, TavernaSecurityContext {
	private SCUFL workflow;
	private Date expiry;
	private Principal creator;
	//private List<Listener> listeners;
	//Status s;
	//String inputBaclava;
	//String outputBaclava;
	//List<RunInput> inputs;
	//private Map<String, String> properties;

	RemoteRunDelegate(Principal creator, SCUFL workflow, RemoteSingleRun rsr) {
		this.creator = creator;
		this.workflow = workflow;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 20);
		this.expiry = c.getTime();
		this.run = rsr;
	}

	RemoteSingleRun run;

	@Override
	public void addListener(Listener listener) {
		if (listener instanceof ListenerDelegate)
			try {
				run.addListener(((ListenerDelegate) listener).getRemote());
			} catch (RemoteException e) {
				log.warn("problem adding listener", e);
			}
		else
			log.fatal("bad listener " + listener.getClass()
					+ "; not applicable remotely!");
	}

	@Override
	public void destroy() {
		try {
			run.destroy();
		} catch (RemoteException e) {
			log.warn("failed to destroy run", e);
		}
	}

	static class ListenerDelegate implements Listener {
		private RemoteListener r;
		String conf;

		ListenerDelegate(RemoteListener l) {
			r = l;
		}

		RemoteListener getRemote() {
			return r;
		}

		@Override
		public String getConfiguration() {
			try {
				if (conf == null)
					conf = r.getConfiguration();
			} catch (RemoteException e) {
				log.warn("failed to get configuration", e);
			}
			return conf;
		}

		@Override
		public String getName() {
			try {
				return r.getName();
			} catch (RemoteException e) {
				log.warn("failed to get name", e);
				return "UNKNOWN NAME";
			}
		}

		@Override
		public String getProperty(String propName) throws NoListenerException {
			try {
				return r.getProperty(propName);
			} catch (RemoteException e) {
				NoListenerException nl = new NoListenerException(propName);
				nl.initCause(e);
				throw nl;
			}
		}

		@Override
		public String getType() {
			try {
				return r.getType();
			} catch (RemoteException e) {
				log.warn("failed to get type", e);
				return "UNKNOWN TYPE";
			}
		}

		@Override
		public String[] listProperties() {
			try {
				return r.listProperties();
			} catch (RemoteException e) {
				log.warn("failed to list properties", e);
				return new String[0];
			}
		}

		@Override
		public void setProperty(String propName, String value)
				throws NoListenerException, BadPropertyValueException {
			try {
				r.setProperty(propName, value);
			} catch (RemoteException e) {
				NoListenerException nl = new NoListenerException();
				nl.initCause(e);
				log.warn("failed to set property", e);
				throw nl;
			}
		}
	}

	@Override
	public Date getExpiry() {
		return expiry;
	}

	@Override
	public List<Listener> getListeners() {
		ArrayList<Listener> listeners = new ArrayList<Listener>();
		try {
			for (RemoteListener rl: run.getListeners()) {
				listeners.add(new ListenerDelegate(rl));
			}
		} catch (RemoteException e) {
			log.warn("failed to get listeners", e);
		}
		return listeners;
	}

	@Override
	public TavernaSecurityContext getSecurityContext() {
		return this;
	}

	@Override
	public Status getStatus() {
		try {
			switch (run.getStatus()) {
			case Initialized:
				return Status.Initialized;
			case Operating:
				return Status.Operating;
			case Stopped:
				return Status.Stopped;
			case Finished:
				return Status.Finished;
			}
		} catch (RemoteException e) {
			log.warn("problem getting remote status", e);
		}
		return Status.Finished;
	}

	@Override
	public SCUFL getWorkflow() {
		return workflow;
	}

	@Override
	public Directory getWorkingDirectory() {
		try {
			return new DirectoryDelegate(run.getWorkingDirectory());
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	abstract static class DEDelegate implements DirectoryEntry {
		private RemoteDirectoryEntry entry;
		private String name;

		DEDelegate(RemoteDirectoryEntry entry) {
			this.entry = entry;
		}

		@Override
		public void destroy() throws FilesystemAccessException {
			try {
				entry.destroy();
			} catch (RemoteException e) {
				log.error("failed to delete directory entry", e);
			}
		}

		@Override
		public String getFullName() {
			String n = getName();
			RemoteDirectoryEntry re = entry;
			try {
				while (true) {
					RemoteDirectory parent = re.getContainingDirectory();
					if (parent == null)
						break;
					n = parent.getName() + "/" + n;
					re = parent;
				}
			} catch (RemoteException e) {
				log.warn("failed to generate full name", e);
			}
			return n;
		}

		@Override
		public String getName() {
			if (name == null)
				try {
					name = entry.getName();
				} catch (RemoteException e) {
					log.error("failed to get name", e);
				}
			return name;
		}
	}

	static class DirectoryDelegate extends DEDelegate implements Directory {
		private RemoteDirectory rd;

		DirectoryDelegate(RemoteDirectory dir) {
			super(dir);
			rd = dir;
		}

		@Override
		public Collection<DirectoryEntry> getContents() {
			ArrayList<DirectoryEntry> result = new ArrayList<DirectoryEntry>();
			try {
				for (RemoteDirectoryEntry rde : rd.getContents()) {
					if (rde instanceof RemoteDirectory)
						result
								.add(new DirectoryDelegate(
										(RemoteDirectory) rde));
					else
						result.add(new FileDelegate((RemoteFile) rde));
				}
			} catch (RemoteException e) {
				log.warn("failed to get directory contents", e);
			}
			return result;
		}

		@Override
		public File makeEmptyFile(Principal actor, String name)
				throws FilesystemAccessException {
			try {
				return new FileDelegate(rd.makeEmptyFile(name));
			} catch (RemoteException e) {
				FilesystemAccessException fa = new FilesystemAccessException(e
						.getMessage());
				fa.initCause(e);
				throw fa;
			}
		}

		@Override
		public Directory makeSubdirectory(Principal actor, String name)
				throws FilesystemAccessException {
			try {
				return new DirectoryDelegate(rd.makeSubdirectory(name));
			} catch (RemoteException e) {
				FilesystemAccessException fa = new FilesystemAccessException(e
						.getMessage());
				fa.initCause(e);
				throw fa;
			}
		}
	}

	static class FileDelegate extends DEDelegate implements File {
		RemoteFile rf;

		FileDelegate(RemoteFile f) {
			super(f);
			this.rf = f;
		}

		@Override
		public byte[] getContents() throws FilesystemAccessException {
			try {
				return rf.getContents();
			} catch (RemoteException e) {
				FilesystemAccessException fa = new FilesystemAccessException(e
						.getMessage());
				fa.initCause(e);
				throw fa;
			}
		}

		@Override
		public long getSize() throws FilesystemAccessException {
			try {
				return rf.getSize();
			} catch (RemoteException e) {
				FilesystemAccessException fa = new FilesystemAccessException(e
						.getMessage());
				fa.initCause(e);
				throw fa;
			}
		}

		@Override
		public void setContents(byte[] data) throws FilesystemAccessException {
			try {
				rf.setContents(data);
			} catch (RemoteException e) {
				FilesystemAccessException fa = new FilesystemAccessException(e
						.getMessage());
				fa.initCause(e);
				throw fa;
			}
		}
	}

	@Override
	public void setExpiry(Date d) {
		if (d.after(new Date()))
			expiry = d;
	}

	@Override
	public void setStatus(Status s) throws BadStateChangeException {
		try {
			switch (s) {
			case Initialized:
				run.setStatus(RemoteStatus.Initialized);
				break;
			case Operating:
				run.setStatus(RemoteStatus.Operating);
				break;
			case Stopped:
				run.setStatus(RemoteStatus.Stopped);
				break;
			case Finished:
				run.setStatus(RemoteStatus.Finished);
				break;
			}
		} catch (Exception e) {
			BadStateChangeException bsc = new BadStateChangeException();
			bsc.initCause(e);
			throw bsc;
		}
	}

	/*
	 * @Override public String getConfiguration() {// FIXME return ""; }
	 * 
	 * @Override public String getName() {// FIXME return "default"; }
	 * 
	 * @Override public String getProperty(String propName) throws
	 * NoListenerException {// FIXME if (!properties.containsKey(propName))
	 * throw new NoListenerException("no such property"); return
	 * properties.get(propName); }
	 * 
	 * @Override public String getType() {// FIXME return "default"; }
	 * 
	 * @Override public String[] listProperties() {// FIXME return
	 * properties.keySet().toArray(new String[properties.size()]); }
	 * 
	 * @Override public void setProperty(String propName, String value) throws
	 * NoListenerException, BadPropertyValueException { // TODO change this?
	 * throw new BadPropertyValueException("setting not supported"); }
	 */

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
		private final RemoteInput i;

		RunInput(RemoteInput remote) {
			this.i = remote;
		}

		@Override
		public String getFile() {
			try {
				return i.getFile();
			} catch (RemoteException e) {
				return null;
			}
		}

		@Override
		public String getName() {
			try {
				return i.getName();
			} catch (RemoteException e) {
				return null;
			}
		}

		@Override
		public String getValue() {
			try {
				return i.getValue();
			} catch (RemoteException e) {
				return null;
			}
		}

		@Override
		public void setFile(String file) throws FilesystemAccessException,
				BadStateChangeException {
			//if (s != Status.Initialized)
			//	throw new BadStateChangeException();
			checkBadFilename(file);
			try {
				i.setFile(file);
			} catch (RemoteException e) {
				FilesystemAccessException f = new FilesystemAccessException(e
						.getMessage());
				f.initCause(e);
				throw f;
			}
			// this.file = file;
			// this.value = null;
			// inputBaclava = null;
		}

		@Override
		public void setValue(String value) throws BadStateChangeException {
			//if (s != Status.Initialized)
			//	throw new BadStateChangeException();
			try {
				i.setValue(value);
			} catch (RemoteException e) {
				BadStateChangeException bsc = new BadStateChangeException();
				bsc.initCause(e);
				throw bsc;
			}
			// this.value = value;
			// this.file = null;
			// inputBaclava = null;
		}

		// void reset() {
		// file = value = null;
		// }
	}

	@Override
	public String getInputBaclavaFile() {
		try {
			return run.getInputBaclavaFile();
		} catch (RemoteException e) {
			log.warn("problem when fetching input baclava file", e);
			return null;
		}
	}

	@Override
	public List<Input> getInputs() {
		ArrayList<Input> inputs = new ArrayList<Input>();
		try {
			for (RemoteInput ri : run.getInputs()) {
				inputs.add(new RunInput(ri));
			}
		} catch (RemoteException e) {
			log.warn("problem when fetching list of workflow inputs", e);
		}
		return inputs;
	}

	@Override
	public String getOutputBaclavaFile() {
		try {
			return run.getOutputBaclavaFile();
		} catch (RemoteException e) {
			log.warn("problem when fetching output baclava file", e);
			return null;
		}
	}

	@Override
	public Input makeInput(String name) throws BadStateChangeException {
		//if (s != Status.Initialized)
		//	throw new BadStateChangeException();
		try {
			return new RunInput(run.makeInput(name));
		} catch (RemoteException e) {
			BadStateChangeException bsc = new BadStateChangeException();
			bsc.initCause(e);
			throw bsc;
		}
		// inputs.add(r);
		// return r;
	}

	@Override
	public void setInputBaclavaFile(String filename)
			throws FilesystemAccessException, BadStateChangeException {
		//if (s != Status.Initialized)
		//	throw new BadStateChangeException();
		checkBadFilename(filename);
		try {
			run.setInputBaclavaFile(filename);
		} catch (RemoteException e) {
			FilesystemAccessException f = new FilesystemAccessException(e
					.getMessage());
			f.initCause(e);
			throw f;
		}
		// inputBaclava = filename;
		// for (RunInput i : inputs)
		// i.reset();
	}

	@Override
	public void setOutputBaclavaFile(String filename)
			throws FilesystemAccessException, BadStateChangeException {
		//if (s != Status.Initialized)
		//	throw new BadStateChangeException();
		checkBadFilename(filename);
		try {
			run.setOutputBaclavaFile(filename);
		} catch (RemoteException e) {
			FilesystemAccessException f = new FilesystemAccessException(e
					.getMessage());
			f.initCause(e);
			throw f;
		}
		// outputBaclava = filename;
	}
}
