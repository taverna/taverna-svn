package uk.org.mygrid.sogsa.sbs;

/**
 * Starts the {@link SemanticBindingService} RESTFUL server in stand alone mode
 * 
 * @author Ian Dunlop
 * 
 */
public class SOGSAServer {

	public static void main(String[] args) {

		SemanticBindingService sbs = new SemanticBindingService();

		// get all the binding keys from the underlying database and fill the
		// List in SemanticBindings, then start the server
		try {
			sbs.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
