package net.sf.taverna.t2.activities.soaplab.query;

import java.util.List;

import net.sf.taverna.t2.partition.ActivityQuery;

import org.apache.log4j.Logger;

public class SoaplabQuery extends ActivityQuery {

	private static Logger logger = Logger.getLogger(SoaplabQuery.class);
	
	public SoaplabQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		try {
			List<SoaplabCategory> categories = SoaplabScavengerAgent.load(getProperty());
			for (SoaplabCategory cat : categories) {
				for (String service : cat.getServices()) {
					SoaplabActivityItem item = new SoaplabActivityItem();
					item.setCategory(cat.getCategory());
					item.setOperation(service);
					item.setUrl(getProperty());
					add(item);
				}
			}
		} catch (MissingSoaplabException e) {
			logger.error("An error occurred querying Soaplab at:"+getProperty(),e);
		}
	}

}
