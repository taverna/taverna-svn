package net.sf.taverna.t2.renderers;

import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * 
 * 
 * @author Ian Dunlop
 */
public class TextHtmlRenderer implements Renderer {
	private Pattern pattern;

	public TextHtmlRenderer() {
		pattern = Pattern.compile(".*text/html.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public boolean isTerminal() {
		return true;
	}

	public String getType() {
		return "Text/Html";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		try {
			String resolve = (String) referenceService.renderIdentifier(
					reference, String.class, null);
			JEditorPane editorPane = null;
			try {
				editorPane = new JEditorPane("text/html", "<pre>" + resolve
						+ "</pre>");
			} catch (Exception e) {
				throw new RendererException(
						"Unable to generate text/html renderer", e);
			}
			return editorPane;
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
	}
}
