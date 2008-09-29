package net.sf.taverna.t2.renderers;

import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

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

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) throws RendererException {
		{
			Object dataObject = null;
			try {
				dataObject = dataFacade.resolve(entityIdentifier);
			} catch (RetrievalException e) {
				throw new RendererException("Could not resolve "
						+ entityIdentifier, e);
			} catch (NotFoundException e) {
				throw new RendererException("Data Manager Could not find "
						+ entityIdentifier, e);
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
							"Could not create URL renderer for "
									+ entityIdentifier, e);
				}
				return theTextArea;
			}
		}
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType)
			throws RendererException {
		return canHandle(mimeType);
	}

	public String getType() {
		return "URL";
	}
}
