package net.sf.taverna.t2.activities.beanshell.query;

import net.sf.taverna.t2.partition.ActivityQuery;

public class BeanshellQuery extends ActivityQuery {

	public BeanshellQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		add(new BeanshellActivityItem());		
	}


}
