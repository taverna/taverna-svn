package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.List;



public class DummyActivityQueryFactory extends ActivityQueryFactory {

	@Override
	public String getPropertyKey() {
		return "fred";
	}

	@Override
	public List<Query<?>> getQueries() {
		List<Query<?>> l = new ArrayList<Query<?>>();
		l.add(new DummyActivityQuery("fred"));
		return l;
	}

	@Override
	protected ActivityQuery createQuery(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
}
