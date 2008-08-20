package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;

public class DummyQueryFactory implements QueryFactory {

	public List<Query<?>> getQueries() {
		List<Query<?>> l = new ArrayList<Query<?>>();
		l.add(new DummyQuery());
		return l;
	}

}
