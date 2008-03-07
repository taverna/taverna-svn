package net.sf.taverna.t2.cloudone.bean;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * Abstract bean for serialising references, such as from
 * {@link DataDocumentBean}.
 * 
 * @see Beanable
 * @see DataDocumentBean
 * @see net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceBean
 * @see net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceBean
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ReferenceBean {
	//needs a default value or else it breaks, even if you tell it to auto-generate
	@Id  //@GeneratedValue (strategy=GenerationType.AUTO)
	private String identifier = UUID.randomUUID().toString();;

//	public Long getIdentifier() {
//		return identifier;
//	}
//
//	public void setIdentitifer(Long identifier) {
//		this.identifier = identifier;
//	}

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
