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
public class TextRtfRenderer implements Renderer {

	private Pattern pattern;

	public TextRtfRenderer() {
		pattern = Pattern.compile(".*text/rtf.*");
	}

	public boolean isTerminal() {
		return true;
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "Text/Rtf";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		try {
			JEditorPane editorPane = null;
			String resolve = (String) referenceService.renderIdentifier(
					reference, String.class, null);
			try {
				editorPane = new JEditorPane("text/html", resolve);
			} catch (Exception e) {
				throw new RendererException(
						"Unable to create text/rtf renderer", e);
			}
			return editorPane;
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
	}
}
