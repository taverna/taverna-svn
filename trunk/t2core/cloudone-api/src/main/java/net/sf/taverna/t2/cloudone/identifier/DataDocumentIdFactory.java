package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class DataDocumentIdFactory extends BeanableFactory<DataDocumentIdentifier, String> {
	public DataDocumentIdFactory() {
		super(DataDocumentIdentifier.class, String.class);
	}
}
