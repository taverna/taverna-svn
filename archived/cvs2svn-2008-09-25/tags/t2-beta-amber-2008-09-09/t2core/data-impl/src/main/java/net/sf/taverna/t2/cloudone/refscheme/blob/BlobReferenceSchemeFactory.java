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
package net.sf.taverna.t2.cloudone.refscheme.blob;

import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

/**
 * Factory for associating {@link BlobReferenceSchemeImpl} with
 * {@link BlobReferenceBean} for the purpose of serialising/deserialising
 * 
 * @see BeanSerialiser
 * @see BeanableFactory
 * @see BeanableFactoryRegistry
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class BlobReferenceSchemeFactory extends
		BeanableFactory<BlobReferenceSchemeImpl, BlobReferenceBean> {

	public BlobReferenceSchemeFactory() {
		super(BlobReferenceSchemeImpl.class, BlobReferenceBean.class);
	}

}
