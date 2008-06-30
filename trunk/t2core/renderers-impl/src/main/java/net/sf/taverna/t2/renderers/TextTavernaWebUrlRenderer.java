package net.sf.taverna.t2.renderers;

import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * View a URL as a clickable HTML URL.
 * 
 * @author Ian Dunlop
 */
public class TextTavernaWebUrlRenderer implements Renderer {
	private Pattern pattern;

	public TextTavernaWebUrlRenderer() {
		pattern = Pattern.compile(".*text/x-taverna-web-url.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "URL";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		{
			Object dataObject = null;
			try {
				dataObject = referenceService.renderIdentifier(reference,
						Object.class, null);
			} catch (Exception e) {
				throw new RendererException("Could not resolve " + reference, e);
			}
			try {
				JEditorPane jep = new JEditorPane();
				String url = dataObject.toString();
				jep.setContentType("text/html");
				jep.setText("<a href=\"" + url + "\">" + url + "</a>");
				return jep;
			} catch (Exception ex) {
				JTextArea theTextArea = null;
				try {
					theTextArea = new JTextArea();
					theTextArea.setText((String) dataObject);
					theTextArea.setFont(Font.getFont("Monospaced"));
				} catch (Exception e) {
					throw new RendererException(
							"Could not create URL renderer for " + reference, e);
				}
				return theTextArea;
			}
		}
	}
}
