package net.sf.taverna.t2.cloudone.entity.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

import org.apache.log4j.Logger;

/**
 * A container for {@link ReferenceScheme}s which describe how to access an
 * {@link Entity} via a {@link DataManager}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class DataDocumentImpl implements DataDocument {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataDocumentImpl.class);

	private DataDocumentIdentifier identifier;
	@SuppressWarnings("unchecked")
	private Set<ReferenceScheme> referenceSchemes;

	@SuppressWarnings("unchecked")
	public DataDocumentImpl() {
		identifier = null;
		referenceSchemes = new HashSet<ReferenceScheme>();
	}

	/**
	 * Access to the {@link DataDocument} is through its identifier, access to
	 * the {@link Entity} it describes is via the {@link ReferenceScheme}
	 * describing where and what it is
	 * 
	 * @param identifier
	 *            unique id
	 * @param references
	 *            {@link ReferenceScheme}s describing different ways to access
	 *            the same {@link Entity}
	 */
	@SuppressWarnings("unchecked")
	public DataDocumentImpl(DataDocumentIdentifier identifier,
			Set<ReferenceScheme> references) {
		this.identifier = identifier;
		this.referenceSchemes = references;
	}

	/**
	 * Get this {@link DataDocument} as a serialisable {@link DataDocumentBean}
	 */
	public DataDocumentBean getAsBean() {
		DataDocumentBean bean = new DataDocumentBean();
		bean.setIdentifier(identifier.getAsURI());
		List<ReferenceBean> references = new ArrayList<ReferenceBean>();
		for (ReferenceScheme<? extends ReferenceBean> refSchema : referenceSchemes) {
			references.add(refSchema.getAsBean());
		}
		bean.setReferences(references);
		return bean;
	}

	/**
	 * The unique id for this {@link DataDocument}
	 */
	public DataDocumentIdentifier getIdentifier() {
		return identifier;
	}

	/**
	 * The reference schemes describing how to access the {@link Entity}
	 */
	@SuppressWarnings("unchecked")
	public Set<ReferenceScheme> getReferenceSchemes() {
		return referenceSchemes;
	}

	/**
	 * Given a {@link DataDocumentBean}, set the fields in this
	 * {@link DataDocumentImpl}
	 */
	@SuppressWarnings("unchecked")
	public void setFromBean(DataDocumentBean bean) {
		if (identifier != null || !referenceSchemes.isEmpty()) {
			throw new IllegalStateException("Can't initialise twice");
		}
		identifier = EntityIdentifiers.parseDocumentIdentifier(bean
				.getIdentifier());

		for (ReferenceBean refBean : bean.getReferences()) {
			Class<? extends ReferenceScheme> ownerClass = refBean
					.getOwnerClass();
			ReferenceScheme refScheme;
			try {
				refScheme = ownerClass.newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(
						"Can't instantiate reference scheme " + ownerClass, e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access reference scheme "
						+ ownerClass, e);
			}
			refScheme.setFromBean(refBean);
			referenceSchemes.add(refScheme);
		}

	}

	/**
	 * Set the uniue indentifier for this {@link DataDocumentImpl}
	 * 
	 * @param identifier
	 *            the unique identifier
	 */
	public void setIdentifier(DataDocumentIdentifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * The schemes which describe how to access the Entity represented by this
	 * {@link DataDocument}
	 * 
	 * @param referenceSchemes
	 */
	@SuppressWarnings("unchecked")
	public void setReferenceSchemes(Set<ReferenceScheme> referenceSchemes) {
		this.referenceSchemes = referenceSchemes;
	}

}