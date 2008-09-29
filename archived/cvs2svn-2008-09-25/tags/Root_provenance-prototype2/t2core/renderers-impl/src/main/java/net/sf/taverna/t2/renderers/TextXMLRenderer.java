package net.sf.taverna.t2.renderers;

import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

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

	public String getType() {
		return "XML tree";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		String resolve = null;
		try {
			resolve = (String) referenceService.renderIdentifier(reference,
					String.class, null);
		} catch (Exception e) {
			// TODO not a string so throw something
			return null;
		}
		try {
			return new XMLTree(resolve);
		} catch (Exception ex) {
			// throw something?
		}
		return null;
	}
}
