package net.sf.taverna.t2.renderers;

import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Viewer to display XML as a tree.
 * 
 * @author Matthew Pocock
 * @auhor Ian Dunlop
 */
public class TextXMLRenderer implements Renderer {
	
	private Pattern pattern;

	public TextXMLRenderer() {
			pattern = Pattern.compile(".*text/xml.*");
	}
	
	public boolean isTerminal() {
		return true;
	}


	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
		String resolve = null;
		try {
			resolve = (String) dataFacade.resolve(entityIdentifier, String.class);
		} catch (RetrievalException e) {
			// TODO not a string so throw something
			return null;
		} catch (NotFoundException e) {
			// TODO not a string so throw something
			return null;
		}
		try {
			return new XMLTree(resolve);
		} catch (Exception ex) {
			//throw something?
		}
		//return as an XML tree??
//		DataThing dataThing) throws RendererException {
//			DataThing copy = new DataThing(dataThing);
//			copy.getMetadata().setMIMETypes(
//				Arrays.asList(strip(dataThing.getMetadata().getMIMETypes())));
//
//			try {
//				return new XMLTree((String) dataThing.getDataObject());
//			} catch (Exception ex) {
//				throw new RendererException(ex);
//			}
		return null;
	}


	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) {
		return canHandle(mimeType);
	}


	public String getType() {
		return "XML tree";
	}
}
