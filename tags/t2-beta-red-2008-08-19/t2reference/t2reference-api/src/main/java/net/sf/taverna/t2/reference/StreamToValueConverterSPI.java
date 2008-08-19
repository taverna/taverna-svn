package net.sf.taverna.t2.reference;

import java.io.InputStream;

/**
 * SPI for objects that can render a POJO from an InputStream
 * 
 * @author Tom Oinn
 * 
 */
public interface StreamToValueConverterSPI<T> {

	/**
	 * The class of objects which this builder can construct from a stream
	 */
	public Class<T> getPojoClass();

	/**
	 * Render the stream to the target object type
	 * 
	 * @param stream
	 *            input stream of data to render to the object
	 * @return the newly created object
	 */
	public T renderFrom(InputStream stream);

}
