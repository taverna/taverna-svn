package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;

import org.apache.log4j.Logger;

public class DataDocumentIdFactory extends BeanableFactory<DataDocumentIdentifier, String> {
	public DataDocumentIdFactory() {
		super(DataDocumentIdentifier.class, String.class);
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(DataDocumentIdFactory.class);
}
