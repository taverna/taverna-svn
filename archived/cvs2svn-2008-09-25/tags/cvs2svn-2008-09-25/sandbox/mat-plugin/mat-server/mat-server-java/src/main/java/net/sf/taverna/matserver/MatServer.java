package net.sf.taverna.matserver;

import net.sf.taverna.matlabactivity.matserver.api.MatEngine;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.server.http.XFireHttpServer;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;

/**
 *
 * @author petarj
 */
public class MatServer {

    public static final int DEFAULT_PORT = 8194;
    private XFireHttpServer server;
    private int port = DEFAULT_PORT;

    public void start() throws Exception {
        ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
        Service service = serviceFactory.create(MatEngine.class);
        service.setInvoker(new BeanInvoker(new MatEngineImpl()));

        XFire xfire = XFireFactory.newInstance().getXFire();
        xfire.getServiceRegistry().register(service);

        server = new XFireHttpServer();
        server.setPort(port);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
}
