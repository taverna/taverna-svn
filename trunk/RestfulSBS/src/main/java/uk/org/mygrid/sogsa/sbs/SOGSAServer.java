package uk.org.mygrid.sogsa.sbs;

import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Starts the {@link SemanticBindingService} RESTFUL server in stand alone mode
 * 
 * @author Ian Dunlop
 * 
 */
public class SOGSAServer {

	public static void main(String[] args) {

		
		Component component = new Component();
        // Add a new HTTP server listening on port 8182.
        component.getServers().add(Protocol.HTTP, 25000);

        // Attach the sample application.
        component.getDefaultHost().attach(
                new SemanticBindingService(component.getContext()));
        

        // Start the component.
        try {
			component.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
