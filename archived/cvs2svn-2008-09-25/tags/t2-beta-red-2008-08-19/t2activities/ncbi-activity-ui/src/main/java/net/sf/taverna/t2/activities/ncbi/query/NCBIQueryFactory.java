package net.sf.taverna.t2.activities.ncbi.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class NCBIQueryFactory extends ActivityQueryFactory{

	@Override
	protected ActivityQuery createQuery(String property) {
		return new NCBIQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
