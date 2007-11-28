package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;


public class ErrorDocumentIdFactory extends BeanableFactory<ErrorDocumentIdentifier, String> {
	public ErrorDocumentIdFactory() {
		super(ErrorDocumentIdentifier.class, String.class);
	}

}
