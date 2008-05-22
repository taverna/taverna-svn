package net.sf.taverna.platformtest.impl;

import net.sf.taverna.platformtest.api.AbstractDataBean;

public class FooDataBean extends AbstractDataBean {

	String fooProperty = null;
	
	public void setFoo(String newFoo) {
		this.fooProperty = newFoo;
	}
	
	public String getFoo() {
		return fooProperty;
	}
	
}
