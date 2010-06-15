package org.taverna.server.localworker.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.commons.io.FileUtils;
import org.taverna.server.localworker.remote.RemoteDirectory;
import org.taverna.server.localworker.remote.RemoteFile;

/**
 * This class acts as a remote-aware delegate for the files in a workflow run's
 * working directory and its subdirectories.
 * 
 * @author Donal Fellows
 * @see DirectoryDelegate
 */
public class FileDelegate extends UnicastRemoteObject implements RemoteFile {
	private File file;
	private DirectoryDelegate parent;

	public FileDelegate(File file, DirectoryDelegate parent)
			throws RemoteException {
		super();
		this.file = file;
		this.parent = parent;
	}

	@Override
	public byte[] getContents() throws RemoteException {
		FileInputStream fis = null;
		try {
			try {
				fis = new FileInputStream(file);
				byte[] buffer = new byte[(int) file.length()];
				fis.read(buffer);
				return buffer;
			} finally {
				if (fis != null)
					fis.close();
			}
		} catch (IOException e) {
			throw new RemoteException("problem reading contents of file", e);
		}
	}

	@Override
	public long getSize() {
		return file.length();
	}

	@Override
	public void setContents(byte[] data) throws RemoteException {
		FileOutputStream fos = null;
		try {
			try {
				fos = new FileOutputStream(file);
				fos.write(data);
				return;
			} finally {
				if (fos != null)
					fos.close();
			}
		} catch (IOException e) {
			throw new RemoteException("problem writing contents of file", e);
		}
	}

	@Override
	public void destroy() throws RemoteException {
		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			throw new RemoteException("problem deleting file", e);
		}
		parent.forgetEntry(this);
		parent = null;
	}

	@Override
	public RemoteDirectory getContainingDirectory() {
		return parent;
	}

	@Override
	public String getName() {
		return file.getName();
	}
}
