package net.sf.taverna.t2.activities.biomart;

import org.biomart.martservice.MartQuery;

/**
 * A configuration bean specific to a Biomart activity. In particular it provides details
 * about the Biomart query.
 * 
 * @author David Withers
 */
public class BiomartActivityConfigurationBean {

	private MartQuery query;

	/**
	 * Returns the Biomart query.
	 * 
	 * @return the Biomart query
	 */
	public MartQuery getQuery() {
		return query;
	}

	/**
	 * Sets the Biomart query.
	 * 
	 * @param query the Biomart query
	 */
	public void setQuery(MartQuery query) {
		this.query = query;
	}
	
}
