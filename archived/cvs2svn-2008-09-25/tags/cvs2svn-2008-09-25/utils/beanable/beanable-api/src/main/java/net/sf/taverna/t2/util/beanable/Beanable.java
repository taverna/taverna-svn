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
 * Anything which you want to serialise with
 * {@link net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser} should implement this
 * interface. Expose required information as a bean from {@link #getAsBean()},
 * which can later be set using {@link #setFromBean(Object)}.
 * 
 * @see net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser
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
	 * bean or keep it internally. Note that {@link #setFromBean(Object)} can
	 * only be called once, and only after constructing the bean with the empty
	 * constructor.
	 * 
	 * @param bean
	 *            Previously serialised bean
	 */
	public void setFromBean(Bean bean) throws IllegalArgumentException;
	
}
