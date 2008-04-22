package net.sf.taverna.t2.cloudone.refscheme.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * Represents a File on a local file system
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileReferenceScheme implements ReferenceScheme<FileReferenceBean> {

	private File file;
	private String charset;

	public FileReferenceScheme(File file) {
		this(file, null);
	}

	/**
	 * Default constructor for immediate initialisation using
	 * {@link #setFromBean(FileReferenceBean)}.
	 * 
	 */
	public FileReferenceScheme() {
		file = null;
		charset = null;
	}

	public FileReferenceScheme(File file, String charset) {
		if (file == null) {
			throw new NullPointerException("File can't be null");
		}
		this.file = file.getAbsoluteFile();
		this.charset = charset;
	}

	/**
	 * find this {@link File} using a {@link DataManager} and return a stream
	 * representing it
	 */
	public InputStream dereference(DataManager manager)
			throws DereferenceException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new DereferenceException("Could not find file " + file, e);
		}
	}

	/**
	 * How has the file been encoded
	 */
	public String getCharset() {
		return charset;
	}

	public Date getExpiry() {
		return null;
	}

	public boolean isImmediate() {
		return false;
	}

	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Used for serialising
	 */
	public FileReferenceBean getAsBean() {
		FileReferenceBean bean = new FileReferenceBean();
		bean.setFile(getFile().getPath());
		bean.setCharset(getCharset());
		return bean;
	}

	/**
	 * Used for deserialising
	 */
	public synchronized void setFromBean(FileReferenceBean bean)
			throws IllegalArgumentException {
		if (file != null) {
			throw new IllegalStateException("Can't initialise twice");
		}
		file = new File(bean.getFile()).getAbsoluteFile();
		charset = bean.getCharset();
	}

	public File getFile() {
		return file;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + file;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FileReferenceScheme other = (FileReferenceScheme) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}

}
