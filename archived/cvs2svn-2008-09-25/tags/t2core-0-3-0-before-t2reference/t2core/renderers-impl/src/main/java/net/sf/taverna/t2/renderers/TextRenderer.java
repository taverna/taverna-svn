package net.sf.taverna.t2.renderers;

import java.awt.Font;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JTextArea;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

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

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) throws RendererException {
		JTextArea theTextArea = new JTextArea();
		String resolve = null;
		try {
			resolve = (String) dataFacade.resolve(entityIdentifier,
					String.class);
		} catch (RetrievalException e1) {
			// TODO not a string so break - should handle this better
			return null;
		} catch (NotFoundException e1) {
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

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType)
			throws RendererException {
		return canHandle(mimeType);
	}

	public String getType() {
		return "Text";
	}
}
