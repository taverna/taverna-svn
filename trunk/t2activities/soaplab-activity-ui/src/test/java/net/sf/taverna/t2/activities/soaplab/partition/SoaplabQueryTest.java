package net.sf.taverna.t2.activities.soaplab.partition;

import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.soaplab.query.SoaplabQuery;

import org.junit.Ignore;
import org.junit.Test;

public class SoaplabQueryTest {

	@Test
	@Ignore("Needs moving to integration tests")
	public void doQuery() {
		SoaplabQuery q = new SoaplabQuery("http://www.ebi.ac.uk/soaplab/services/");
		q.doQuery();
		assertTrue("There must be more that 1 result",q.size()>0);
	}
}
