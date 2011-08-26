package net.sf.taverna.t2.activities.soaplab.query;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SoaplabQueryTest {

	@Test
	public void doQuery() {
		SoaplabQuery q = new SoaplabQuery("http://www.ebi.ac.uk/soaplab/services/");
		q.doQuery();
		assertTrue("There must be more that 1 result",q.size()>0);
	}
}
