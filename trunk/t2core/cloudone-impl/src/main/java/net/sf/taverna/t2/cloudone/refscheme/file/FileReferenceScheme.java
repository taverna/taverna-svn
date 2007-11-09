package net.sf.taverna.t2.cloudone.refscheme.file;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

public class FileReferenceScheme implements ReferenceScheme<FileReferenceBean> {

	private File file;

	public FileReferenceScheme(File file) {
		this.file = file.getAbsoluteFile();
	}

	public FileReferenceScheme() {
		file = null;
	}

	public InputStream dereference(DataManager manager)
			throws DereferenceException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCharset() throws DereferenceException {
		// TODO Auto-generated method stub
		return null;
	}

	public Date getExpiry() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isImmediate() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean validInContext(Set<LocationalContext> contextSet,
			DataPeer currentLocation) {
		// TODO Auto-generated method stub
		return false;
	}

	public FileReferenceBean getAsBean() {
		FileReferenceBean bean = new FileReferenceBean();
		bean.setFile(getFile().getPath());
		return bean;
	}

	public Class<FileReferenceBean> getBeanClass() {
		return FileReferenceBean.class;
	}

	public synchronized void setFromBean(FileReferenceBean bean)
			throws IllegalArgumentException {
		if (file != null) {
			throw new IllegalStateException("Can't initialise twice");
		}
		file = new File(bean.getFile()).getAbsoluteFile();
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
