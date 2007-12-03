package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.LiteralBean;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class LiteralFactory extends BeanableFactory<Literal, LiteralBean> {

	public LiteralFactory() {
		super(Literal.class, LiteralBean.class);
	}

}
