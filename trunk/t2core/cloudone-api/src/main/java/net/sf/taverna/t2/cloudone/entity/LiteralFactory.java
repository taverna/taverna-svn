package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;

import org.apache.log4j.Logger;

public class LiteralFactory extends BeanableFactory<Literal, String> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LiteralFactory.class);

	public LiteralFactory() {
		super(Literal.class, String.class);
	}

}
