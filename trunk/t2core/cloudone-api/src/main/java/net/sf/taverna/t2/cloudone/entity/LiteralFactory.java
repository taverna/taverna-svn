package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class LiteralFactory extends BeanableFactory<Literal, String> {

	public LiteralFactory() {
		super(Literal.class, String.class);
	}

}
