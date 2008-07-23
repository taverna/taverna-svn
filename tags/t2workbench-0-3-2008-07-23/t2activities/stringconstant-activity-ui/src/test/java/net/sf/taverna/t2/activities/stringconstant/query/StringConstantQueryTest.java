package net.sf.taverna.t2.activities.stringconstant.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.partition.Query;

import org.junit.Test;

public class StringConstantQueryTest {

	@Test
	public void doQuery() {
		Query<?> q = new StringConstantQuery(null);
		q.doQuery();
		assertEquals("There should be 1 item found",1,q.size());
		assertTrue("The item should be a StringConstantActivityItem",q.toArray()[0] instanceof StringConstantActivityItem);
	}
}
