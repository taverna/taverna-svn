package net.sf.taverna.t2.activities.biomoby.query;

import static org.junit.Assert.assertTrue;
import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.t2.partition.Query;

import org.junit.Before;
import org.junit.Test;

public class BiomobyQueryTest {
	
	@Before
	public void setup() {
		System.setProperty(ApplicationConfig.APP_NAME, "Biomoby_test");
	}
	
	private  final String BIOMOBY_URL="http://moby.ucalgary.ca/moby/MOBY-Central.pl";
	
	@Test
	public void testDoQuery() {
		Query<?> q = new BiomobyQuery(BIOMOBY_URL);
		q.doQuery();
		assertTrue("There should be at least more than one item found",q.size()>1);
	}
}
