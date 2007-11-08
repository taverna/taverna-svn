package net.sf.taverna.t2.cloudone.refscheme.file;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

public class FileReferenceScheme implements ReferenceScheme<ReferenceBean> {

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

	public ReferenceBean getAsBean() {
		// TODO Auto-generated method stub
		return null;
	}

	public Class<ReferenceBean> getBeanClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFromBean(ReferenceBean bean) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

}
