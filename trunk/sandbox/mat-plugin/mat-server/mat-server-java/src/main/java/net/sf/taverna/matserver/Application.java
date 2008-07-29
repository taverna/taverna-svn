package net.sf.taverna.matserver;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author petarj
 */
public class Application {

    static MatServer server;

    public static void main(String[] args) {
        server = new MatServer();
        try {
            server.start();
        } catch (Exception ex) {
            Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
