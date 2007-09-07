package net.sf.taverna.t2.cloudone;

import net.sf.taverna.t2.cloudone.bean.Beanable;

public interface BlobReferenceScheme extends ReferenceScheme, Beanable<String>{

	public abstract String getId();

	public abstract String getNamespace();

	public abstract String toString();

	public abstract int hashCode();

	public abstract boolean equals(Object obj);

}