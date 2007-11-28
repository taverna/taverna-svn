package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;

import org.apache.log4j.Logger;

public class ErrorDocumentIdFactory extends BeanableFactory<ErrorDocumentIdentifier, String> {
	public ErrorDocumentIdFactory() {
		super(ErrorDocumentIdentifier.class, String.class);
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(ErrorDocumentIdFactory.class);
}
