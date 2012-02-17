/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.jetty;

import net.sf.taverna.t2.activities.interaction.preference.InteractionPreference;
import net.sf.taverna.t2.workbench.StartupSPI;

import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.abdera.protocol.server.ServiceManager;
import org.apache.abdera.protocol.server.provider.basic.BasicProvider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author alanrw
 *
 */
public class JettyStartupHook implements StartupSPI {

	private static Server server;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#positionHint()
	 */
	@Override
	public int positionHint() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.StartupSPI#startup()
	 */
	@Override
	public boolean startup() {
		server = new Server();
		
		try {
			SelectChannelConnector connector = new SelectChannelConnector();
	        connector.setPort(Integer.parseInt(InteractionPreference.getInstance().getPort()));
	        server.addConnector(connector);
	        
	        ContextHandler interactionContext = new ContextHandler();
	        interactionContext.setContextPath("/interaction");
	        interactionContext.setResourceBase(".");
	        interactionContext.setClassLoader(Thread.currentThread().getContextClassLoader());
	        
		ResourceHandler presentationHandler = new ResourceHandler();
		presentationHandler.setResourceBase(InteractionPreference.getInstance().getPresentationDirectory());
		interactionContext.setHandler(presentationHandler);
		
		HandlerList handlers = new HandlerList();
		Context abderaContext = new Context(handlers, "/", Context.SESSIONS);
 
        AbderaServlet abderaServlet = new AbderaServlet();
		ServletHolder servletHolder = new ServletHolder(abderaServlet);
		servletHolder.setInitParameter(ServiceManager.PROVIDER, BasicProvider.class.getName());
        
		abderaContext.addServlet(servletHolder,"/*");
		
       handlers.setHandlers(new Handler[] { interactionContext, abderaContext, new DefaultHandler() });
        server.setHandler(handlers);
		
			server.start();
			
/*			Entry entry = abderaServlet.getAbdera().newEntry();

            String id = FOMHelper.generateUuid();
			entry.setId(id);
            entry.setUpdated(new java.util.Date());

            entry.addAuthor("Taverna");
            entry.setTitle("Interaction notification");
            entry.addLink("http://www.taverna.org.uk");
            entry.setContentAsXhtml("<p><a href=\"http://www.taverna.org.uk\">Link</a></p>");


            AbderaClient client = new AbderaClient(abderaServlet.getAbdera());
            RequestOptions rOptions = client.getDefaultRequestOptions();
            rOptions.setSlug(id);

            ClientResponse resp = client.post(
            "http://localhost:8080/feed", entry, rOptions);
                    client.teardown();*/

		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return true;
	}
	
	public static Server getServer() {
		return server;
	}

}
