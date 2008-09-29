package net.sf.taverna.t2.activities.rshell.query;

import net.sf.taverna.t2.partition.ActivityQuery;

public class RshellQuery extends ActivityQuery{

	public RshellQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		add(new RshellActivityItem());
	}

}
