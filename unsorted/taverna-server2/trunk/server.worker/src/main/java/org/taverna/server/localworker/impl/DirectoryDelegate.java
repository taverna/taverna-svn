package org.taverna.server.localworker.impl;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.io.FileUtils;
import org.taverna.server.localworker.remote.RemoteDirectory;
import org.taverna.server.localworker.remote.RemoteDirectoryEntry;
import org.taverna.server.localworker.remote.RemoteFile;

public class DirectoryDelegate extends UnicastRemoteObject implements
		RemoteDirectory {
	private File dir;
	private DirectoryDelegate parent;
	private WeakHashMap<String, RemoteDirectoryEntry> localCache;

	public DirectoryDelegate(File dir, DirectoryDelegate parent)
			throws RemoteException {
		super();
		this.localCache = new WeakHashMap<String, RemoteDirectoryEntry>();
		this.dir = dir;
		this.parent = parent;
	}

	@Override
	public Collection<RemoteDirectoryEntry> getContents()
			throws RemoteException {
		ArrayList<RemoteDirectoryEntry> result = new ArrayList<RemoteDirectoryEntry>();
		for (String s : dir.list()) {
			if (s.equals(".") || s.equals(".."))
				continue;
			File f = new File(dir, s);
			RemoteDirectoryEntry entry = localCache.get(s);
			if (f.isDirectory()) {
				if (entry == null || !(entry instanceof DirectoryDelegate)) {
					entry = new DirectoryDelegate(f, this);
					localCache.put(s, entry);
				}
			} else if (f.isFile()) {
				if (entry == null || !(entry instanceof FileDelegate)) {
					entry = new FileDelegate(f, this);
					localCache.put(s, entry);
				}
			} else {
				// not file or dir; skip...
				continue;
			}
			result.add(entry);
		}
		return result;
	}

	@Override
	public RemoteFile makeEmptyFile(String name) throws RemoteException {
		if ("..".equals(name) || name.contains("/"))
			throw new RemoteException("illegal filename");
		File f = new File(dir, name);
		if (f.exists())
			throw new RemoteException("already exists");
		try {
			FileUtils.touch(f);
		} catch (IOException e) {
			RemoteException re = new RemoteException(e.getMessage());
			re.initCause(e);
			throw re;
		}
		FileDelegate delegate = new FileDelegate(f, this);
		localCache.put(name, delegate);
		return delegate;
	}

	@Override
	public RemoteDirectory makeSubdirectory(String name) throws RemoteException {
		if ("..".equals(name) || name.contains("/"))
			throw new RemoteException("illegal filename");
		File f = new File(dir, name);
		if (f.exists())
			throw new RemoteException("already exists");
		try {
			FileUtils.forceMkdir(f);
		} catch (IOException e) {
			RemoteException re = new RemoteException(e.getMessage());
			re.initCause(e);
			throw re;
		}
		DirectoryDelegate delegate = new DirectoryDelegate(f, this);
		localCache.put(name, delegate);
		return delegate;
	}

	@Override
	public void destroy() throws RemoteException {
		if (parent == null)
			throw new RemoteException(
					"tried to destroy main job working directory");
		for (RemoteDirectoryEntry entry : localCache.values()) {
			if (entry == null)
				continue;
			try {
				entry.destroy();
			} catch (RemoteException e) {
			}
		}
		try {
			FileUtils.forceDelete(dir);
		} catch (IOException e) {
			RemoteException re = new RemoteException(e.getMessage());
			re.initCause(e);
			throw re;
		}
		parent.forgetEntry(this);
	}

	@Override
	public RemoteDirectory getContainingDirectory() {
		return parent;
	}

	void forgetEntry(RemoteDirectoryEntry entry) {
		for (Map.Entry<String, RemoteDirectoryEntry> e : localCache.entrySet()) {
			if (e.getValue() == entry) {
				localCache.remove(e.getKey());
				break;
			}
		}
	}

	@Override
	public String getName() {
		return dir.getName();
	}
}
