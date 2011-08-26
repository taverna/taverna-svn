package net.sf.taverna.t2.matlabactivity.query;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

/**
 *
 * @author petarj
 */
public class MatPluginQueryFactory extends ActivityQueryFactory {

    @Override
    protected String getPropertyKey() {
        return null;
    }

    @Override
    protected ActivityQuery createQuery(String property) {
        return new MatPluginQuery(property);
    }
}
