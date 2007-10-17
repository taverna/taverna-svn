package net.sf.taverna.t2.cloudone.bean;

/**
 * Anything which you want to serialise with
 * {@link net.sf.taverna.t2.cloudone.util.BeanSerialiser} should implement
 * this interface. Expose required information as a bean from
 * {@link #getAsBean()}, which can later be set using
 * {@link #setFromBean(Object)}.
 *
 * @see net.sf.taverna.t2.cloudone.util.BeanSerialiser
 * @author Ian Dunlop
 * @author Stian Soiland
 * @param <Bean>
 *            A simple JavaBean class which contains the required information
 */
public interface Beanable<Bean> {

	/**
	 * Expose as a serialisable bean. This is not necessarily the same bean as
	 * set with {@link #setFromBean(Object)}.
	 *
	 * @return The bean to be serialised
	 */
	public Bean getAsBean();

	/**
	 * Set values from bean. Implementations can either copy the values from the
	 * bean or keep it internally.
	 *
	 * @param bean
	 *            Previously serialised bean
	 */
	public void setFromBean(Bean bean) throws IllegalArgumentException;
}
