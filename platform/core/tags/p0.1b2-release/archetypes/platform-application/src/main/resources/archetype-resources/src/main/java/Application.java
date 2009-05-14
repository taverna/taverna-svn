#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import net.sf.taverna.t2.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.platform.taverna.TavernaBaseProfile;

import org.springframework.context.ApplicationContext;

/**
 * Skeleton application, initializes the platform from the 'context.xml'
 */
public class Application {

	/**
	 * This is the main method, showing a very simple platform initialization. 
	 * It doesn't do anything else though, yet...
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		// Initialise the platform
		System.out.println("Initializing platform...");
		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext("context.xml");
		TavernaBaseProfile profile = new TavernaBaseProfile(context);
		System.out.println("Platform created.");

		// Do stuff with platform!
		// Your code here...
		
	}
}
