package uk.org.mygrid.logbook.ui.util;

import net.sf.taverna.tools.Bootstrap;

/**
 * Used for debugging within Eclipse.
 * @author dturi
 * @version $Id: BootstrapLogbook.java,v 1.1 2008-04-02 16:24:38 stain Exp $
 */
public class BootstrapLogbook {

    public static void main(String[] args) {
        try {
            Bootstrap.main(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
