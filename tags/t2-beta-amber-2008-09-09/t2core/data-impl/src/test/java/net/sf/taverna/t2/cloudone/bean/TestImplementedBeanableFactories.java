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
package net.sf.taverna.t2.cloudone.bean;

import static org.junit.Assert.assertSame;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.util.beanable.BeanableFactory;
import net.sf.taverna.t2.util.beanable.BeanableFactoryRegistry;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class TestImplementedBeanableFactories {

	private BeanableFactoryRegistry registry = BeanableFactoryRegistry.getInstance();

	Class<? extends Beanable>[] beanableClasses = new Class[]{
			Literal.class,
			HttpReferenceScheme.class,
			BlobReferenceSchemeImpl.class,
			EntityList.class,
			ErrorDocument.class,
			DataDocumentImpl.class,
	};
	

	@Test
	public void getBeanables() {
		for (Class<? extends Beanable> c : beanableClasses) {
			BeanableFactory factory = registry.getFactoryForBeanableType(c.getCanonicalName());
			assertSame("SPI registry didn't return same class", c, factory.getBeanableType());
		}
	}

}
