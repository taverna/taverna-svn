package net.sf.taverna.t2.cloudone.bean;

import net.sf.taverna.t2.cloudone.ReferenceScheme;

/**
 * Abstract bean for serialising references, such as from
 * {@link DataDocumentBean}.
 * 
 * @see Beanable
 * @see DataDocumentBean
 * @see net.sf.taverna.t2.cloudone.impl.BlobReferenceBean
 * @see net.sf.taverna.t2.cloudone.impl.http.HttpReferenceBean
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public abstract class ReferenceBean {

	/**
	 * Get the {@link Beanable} class that "owns" this bean. An instance of this
	 * class created with the default constructor should be able to use this
	 * {@link ReferenceBean} as a parameter to
	 * {@link Beanable#setFromBean(Object)}.
	 * 
	 * @return The owning class
	 */
	public abstract Class<? extends ReferenceScheme<? extends ReferenceBean>> getOwnerClass();
}
