package net.sf.taverna.t2.partition;

import java.util.List;

public interface QueryFactory {
	List<Query<?>> getQueries();
}
