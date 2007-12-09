/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import net.sf.taverna.t2.drizzle.bean.ProcessorFactoryAdapterBean;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.util.beanable.Beanable;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.jdom.Element;

/**
 * @author alanrw
 *
 */
public final class ProcessorFactoryAdapter<FactoryType extends ProcessorFactory> implements Beanable<ProcessorFactoryAdapterBean>{
	
	private byte[] serializedVersion = null;

	private FactoryType theFactory;
	
	public ProcessorFactoryAdapter(FactoryType theFactory) {
		this.theFactory = theFactory;
		Element elem = theFactory.getXMLFragment();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(elem);
			serializedVersion = baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * @return the serializedVersion
	 */
	public synchronized final byte[] getSerializedVersion() {
		return serializedVersion;
	}

	/**
	 * @return the theFactory
	 */
	public synchronized final FactoryType getTheFactory() {
		return theFactory;
	}

	public ProcessorFactoryAdapterBean getAsBean() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFromBean(ProcessorFactoryAdapterBean bean)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ProcessorFactoryAdapter) {
			return (Arrays.equals(((ProcessorFactoryAdapter)o).getSerializedVersion(),this.getSerializedVersion()));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(getSerializedVersion());
	}
	

}
