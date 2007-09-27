package net.sf.taverna.t2.cloudone;

import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public interface BlobReferenceScheme<B extends ReferenceBean> extends
		ReferenceScheme, Beanable<B> {

	public abstract boolean equals(Object obj);

	public abstract String getId();

	public abstract String getNamespace();

	public abstract int hashCode();

	public abstract String toString();

}