package net.sf.taverna.matserver;

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
public class MatServer
{

    private XFireHttpServer server;

    public void start() throws Exception
    {
        ObjectServiceFactory serviceFactory = new ObjectServiceFactory();
        Service service=serviceFactory.create(MatEngine.class);
        service.setInvoker(new BeanInvoker(new MatEngineImpl()));
        
        XFire xfire=XFireFactory.newInstance().getXFire();
        xfire.getServiceRegistry().register(service);
        
        server=new XFireHttpServer();
        server.setPort(8194);
        server.start();
    }
    
    public void stop() throws Exception
    {
        server.stop();
    }
}
