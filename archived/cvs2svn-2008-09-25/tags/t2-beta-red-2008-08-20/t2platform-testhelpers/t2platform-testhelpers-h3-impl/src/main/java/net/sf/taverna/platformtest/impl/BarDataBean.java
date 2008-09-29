package net.sf.taverna.platformtest.impl;

import net.sf.taverna.platformtest.api.AbstractDataBean;

public class BarDataBean extends AbstractDataBean {

	int barProperty;
	
	public void setBar(int newBar) {
		this.barProperty = newBar;
	}
	
	public int getBar() {
		return barProperty;
	}
	
	@Override
	public String toString() {
		return "Bar data bean, name='"+name+"' barProperty='"+barProperty+"'";
	}
	
}
