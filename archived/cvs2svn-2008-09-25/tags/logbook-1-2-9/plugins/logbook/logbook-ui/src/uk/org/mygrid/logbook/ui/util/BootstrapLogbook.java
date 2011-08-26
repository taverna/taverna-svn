package uk.org.mygrid.logbook.ui.util;

import net.sf.taverna.tools.Bootstrap;

/**
 * Used for debugging within Eclipse.
 * @author dturi
 * @version $Id: BootstrapLogbook.java,v 1.1 2007-12-14 12:48:40 stain Exp $
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
