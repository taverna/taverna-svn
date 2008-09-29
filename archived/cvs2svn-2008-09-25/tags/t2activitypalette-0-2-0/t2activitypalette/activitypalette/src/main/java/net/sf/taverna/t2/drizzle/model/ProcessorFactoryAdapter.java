/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import net.sf.taverna.t2.drizzle.bean.ProcessorFactoryAdapterBean;
import net.sf.taverna.t2.util.beanable.Beanable;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.jdom.Element;

/**
 * @author alanrw
 * 
 */
public final class ProcessorFactoryAdapter implements
		Beanable<ProcessorFactoryAdapterBean> {

	private byte[] serializedVersion = null;

	private ProcessorFactory theFactory;

	private int hashCode;

	/**
	 * @param theFactory
	 */
	public ProcessorFactoryAdapter(ProcessorFactory theFactory) {
		this.theFactory = theFactory;
		this.hashCode = super.hashCode();
		try {
			Element elem = theFactory.getXMLFragment();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(elem);
				this.serializedVersion = baos.toByteArray();
				this.hashCode = Arrays.hashCode(this.serializedVersion);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Throwable e) {
			// TODO figure out what is thrown because this is very bad
		}
	}

	/**
	 * @return the serializedVersion
	 */
	public synchronized final byte[] getSerializedVersion() {
		return this.serializedVersion;
	}

	/**
	 * @return the theFactory
	 */
	public synchronized final ProcessorFactory getTheFactory() {
		return this.theFactory;
	}

	public ProcessorFactoryAdapterBean getAsBean() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see net.sf.taverna.t2.util.beanable.Beanable#setFromBean(java.lang.Object)
	 */
	public void setFromBean(ProcessorFactoryAdapterBean bean)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof ProcessorFactoryAdapter) {
			return (Arrays.equals(((ProcessorFactoryAdapter) o)
					.getSerializedVersion(), this.getSerializedVersion()));
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.hashCode;
	}

}
