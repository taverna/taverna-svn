package net.sourceforge.taverna.publish;

/**
 * This class represents an Exception thrown during the publication process.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class PublicationException extends Exception {

	public PublicationException(String msg) {
		super(msg);
	}

	public PublicationException(Throwable th) {
		super(th);
	}

}
