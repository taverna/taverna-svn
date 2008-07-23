package net.sf.taverna.t2.renderers;

import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * 
 * 
 * @author Ian Dunlop
 */
public class TextRenderer implements Renderer {
	private Pattern pattern;

	public TextRenderer() {
		pattern = Pattern.compile(".*text/.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "Text";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		JTextArea theTextArea = new JTextArea();
		String resolve = null;
		try {
			resolve = (String) referenceService.renderIdentifier(reference,
					String.class, null);
		} catch (Exception e1) {
			// TODO not a string so break - should handle this better
			return null;
		}
		try {
			theTextArea.setText(resolve);
			theTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		} catch (Exception e) {
			throw new RendererException("Unable to create text renderer", e);
		}

		return theTextArea;
	}
}
