package net.sf.taverna.t2.monitor;

/**
 * Some monitorable properties are mutable and can be written to by steering
 * agents or other clients.
 * 
 * @author Tom Oinn
 * 
 */
public interface SteerableProperty<T> extends MonitorableProperty<T> {

	/**
	 * Set the property value
	 * 
	 * @param newValue
	 * @throws NoSuchPropertyException
	 */
	public void setProperty(T newValue) throws NoSuchPropertyException;

}
