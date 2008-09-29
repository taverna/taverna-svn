package net.sf.taverna.t2.activities.matlab;

/**
 *
 * @author petarj
 */
public class MatActivityConnection {

    private boolean keepSessionAlive;

    public MatActivityConnection(MatActivityConnectionSettings connectionSettings) {
    }

    public boolean isKeepSessionAlive() {
        return keepSessionAlive;
    }
}
