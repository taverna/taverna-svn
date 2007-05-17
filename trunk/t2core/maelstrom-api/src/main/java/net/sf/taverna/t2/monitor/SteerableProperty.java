package net.sf.taverna.t2.monitor;

/**
 * Some monitorable properties are mutable and can be written to by steering
 * agents or other clients.
 * 
 * @author Tom Oinn
 * 
 */
public interface SteerableProperty<T> extends MonitorableProperty<T> {

	public void setProperty(T newValue) throws NoSuchPropertyException;
	
}
