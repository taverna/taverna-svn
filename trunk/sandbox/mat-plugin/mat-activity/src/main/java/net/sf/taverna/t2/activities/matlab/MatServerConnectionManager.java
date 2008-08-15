package net.sf.taverna.t2.activities.matlab;

import java.net.MalformedURLException;
import net.sf.taverna.matlabactivity.matserver.api.MatEngine;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;

/**
 *
 * @author petarj
 */
public class MatServerConnectionManager {

    private MatActivityConnectionSettings settings;

    public MatServerConnectionManager() {
    }

    public MatServerConnectionManager(MatActivityConnectionSettings settings) {
        this.settings = settings;
    }

    public void configure(MatActivityConnectionSettings settings) {
        this.settings = settings;
    }

    public MatEngine getEngine() throws MalformedURLException {
        MatEngine engine;
        ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
        Service serviceModel = serviceFactory.create(MatEngine.class);
        XFireProxyFactory proxyFactory = new XFireProxyFactory();
System.err.println("AAAAAAAAAAAAAAAAAAAAAAABBBA");

        String serviceURL = "http://" + settings.getHost() + ":" + settings.
                getPort() + "/MatEngine";
System.err.println("****"+serviceURL);
        engine = (MatEngine) proxyFactory.create(serviceModel, serviceURL);
        return engine;
    }
}
