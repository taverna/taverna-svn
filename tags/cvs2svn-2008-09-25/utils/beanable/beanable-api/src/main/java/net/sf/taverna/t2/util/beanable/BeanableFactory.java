/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.util.beanable;

/**
 * A factory for reconstructing a {@link Beanable} given a bean previously
 * serialised from {@link Beanable#getAsBean()}.
 * <p>
 * Each {@link Beanable} subclass should be matched with a subclass of
 * {@link BeanableFactory} for producing the beanable from the bean.
 * <p>
 * This abstract class is an SPI, and it's concrete instances can be found by
 * accessing {@link BeanableFactoryRegistry}. Implementations must be listed in
 * META-INF/services/net.sf.taverna.t2.cloudone.bean.BeanableFactory to be found
 * by the registry.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <BeanableType>
 *            The {@link Beanable} type
 * @param <BeanType>
 *            The bean type of the {@link Beanable}.
 */
public abstract class BeanableFactory<BeanableType extends Beanable<BeanType>, BeanType> {

	private final Class<BeanableType> beanableType;
	private final Class<BeanType> beanType;

	/**
	 * Construct a BeanableFactory for a given {@link Beanable}. This
	 * constructor is protected so that it can only be called from specialising
	 * subclasses.
	 * 
	 * @param beanableType
	 *            The {@link Beanable} sub{@link Class} this factory is to
	 *            produce
	 * @param beanType
	 *            The {@link Class} of beans this factory consumes
	 */
	protected BeanableFactory(Class<BeanableType> beanableType,
			Class<BeanType> beanType) {
		this.beanableType = beanableType;
		this.beanType = beanType;

	}

	/**
	 * Construct a Beanable (an instance of the class returned from
	 * {@link #getBeanableType()}) loaded from the given bean.
	 * 
	 * @param bean
	 *            Bean containing the data to reconstruct the Beanable. The bean
	 *            must be an instance of the class of {@link #getBeanType()}.
	 * @return A new instance initialised from the bean
	 */
	public BeanableType createFromBean(BeanType bean) {
		BeanableType beanable;
		try {
			beanable = getBeanableType().newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException("Could not instantiate "
					+ getBeanableType(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Could not access "
					+ getBeanableType(), e);
		}
		beanable.setFromBean(bean);
		return getBeanableType().cast(beanable);
	}

	/**
	 * Get this factory's {@link Beanable} type. Instances of this class can be
	 * created using {@link #createFromBean(Object)}.
	 * 
	 * @return The {@link Beanable} {@link Class} this factory produces
	 */
	public Class<BeanableType> getBeanableType() {
		return beanableType;
	}

	/**
	 * Get this factory's bean type. Instances of this class can be used as
	 * argument to {@link #createFromBean(Object)}.
	 * 
	 * @return The {@link Class} of the bean consumed by
	 *         {@link #createFromBean(Object)}
	 */
	public Class<BeanType> getBeanType() {
		return beanType;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "<"
				+ getBeanableType().getSimpleName() + ", "
				+ getBeanType().getSimpleName() + ">";
	}

}
