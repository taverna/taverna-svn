package net.sf.taverna.t2.matlabactivity.query;

import net.sf.taverna.t2.partition.ActivityQuery;

/**
 *
 * @author petarj
 */
public class MatPluginQuery extends ActivityQuery {

    public MatPluginQuery(String property) {
        super(property);
    }

    @Override
    public void doQuery() {
        add(new MatActivityItem());
    }
}
