package net.sf.taverna.t2.activities.localworker.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

/**
 * Factory method used to get a {@link LocalworkerQuery}
 * 
 * @author Ian Dunlop
 * 
 */
public class LocalworkerQueryFactory extends ActivityQueryFactory {

	@Override
	protected ActivityQuery createQuery(String property) {
		return new LocalworkerQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		// TODO Auto-generated method stub
		return null;
	}

}
