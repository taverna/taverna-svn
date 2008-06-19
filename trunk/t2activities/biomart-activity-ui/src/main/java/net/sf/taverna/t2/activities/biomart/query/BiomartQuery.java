package net.sf.taverna.t2.activities.biomart.query;

import java.util.Arrays;

import net.sf.taverna.t2.partition.ActivityQuery;

import org.apache.log4j.Logger;
import org.biomart.martservice.MartDataset;
import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartRegistry;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartURLLocation;

public class BiomartQuery extends ActivityQuery {
	
	private static Logger logger = Logger.getLogger(BiomartQuery.class);

	public BiomartQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		try {
			MartService martService = MartService
					.getMartService(getBiomartServiceLocation(getProperty()));
			martService.setRequestId("taverna");
			MartRegistry registry = martService.getRegistry();
			MartURLLocation[] martURLLocations = registry.getMartURLLocations();
			for (MartURLLocation martURLLocation : martURLLocations) {
				if (martURLLocation.isVisible()) {
					
					MartDataset[] datasets = martService
							.getDatasets(martURLLocation);
					Arrays.sort(datasets, MartDataset.getDisplayComparator());
					for (MartDataset dataset : datasets) {
						if (dataset.isVisible()) {
							BiomartActivityItem item = new BiomartActivityItem();
							item.setLocation(martURLLocation.getDisplayName());
							item.setDataset(dataset.getDisplayName());
							MartQuery biomartQuery = new MartQuery(martService,
									dataset, "taverna");
							item.setMartQuery(biomartQuery);
							add(item);
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("There was an error querying Biomart at:"+getProperty(),ex);
		}
	}
	
	/**
	 * Attempts to construct a valid MartService URL from the location given.
	 * 
	 * @param biomartLocation
	 * @return a (hopefully) valid MartService URL
	 */
	private String getBiomartServiceLocation(String biomartLocation) {
		StringBuffer sb = new StringBuffer();
		if (biomartLocation.endsWith("martservice")) {
			sb.append(biomartLocation);
		} else if (biomartLocation.endsWith("martview")) {
			sb.append(biomartLocation.substring(0, biomartLocation
					.lastIndexOf("martview")));
			sb.append("martservice");
		} else if (biomartLocation.endsWith("/")) {
			sb.append(biomartLocation);
			sb.append("martservice");
		} else {
			sb.append(biomartLocation);
			sb.append("/martservice");
		}
		return sb.toString();
	}

}
