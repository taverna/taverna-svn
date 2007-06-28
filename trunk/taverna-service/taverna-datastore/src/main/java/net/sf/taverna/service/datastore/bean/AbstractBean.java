package net.sf.taverna.service.datastore.bean;

import java.io.Serializable;

public abstract class AbstractBean<PrimaryKey extends Serializable> {
	public abstract PrimaryKey getId();
	
	
}
