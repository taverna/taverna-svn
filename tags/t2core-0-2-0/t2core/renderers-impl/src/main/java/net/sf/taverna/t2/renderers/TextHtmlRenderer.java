package net.sf.taverna.t2.renderers;

import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

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

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) throws RendererException {

		try {
			String resolve = (String) dataFacade.resolve(entityIdentifier,
					String.class);
			JEditorPane editorPane = null;
			try {
				editorPane = new JEditorPane("text/html", "<pre>" + resolve
						+ "</pre>");
			} catch (Exception e) {
				throw new RendererException(
						"Unable to generate text/html renderer", e);
			}
			return editorPane;
		} catch (RetrievalException e) {
			throw new RendererException(
					"Could not resolve " + entityIdentifier, e);
		} catch (NotFoundException e) {
			throw new RendererException("Data Manager Could not find "
					+ entityIdentifier, e);
		}
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType)
			throws RendererException {
		return canHandle(mimeType);
	}

	public String getType() {
		return "Text/Html";
	}
}
