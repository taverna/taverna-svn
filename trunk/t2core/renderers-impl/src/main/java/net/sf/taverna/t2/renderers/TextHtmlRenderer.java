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
public class TextHtmlRenderer implements Renderer
{
	private Pattern pattern;

	public TextHtmlRenderer() {
		pattern = Pattern.compile(".*text/html.*");
	}

	public boolean canHandle(
			String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public boolean isTerminal() {
		return true;
	}


	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {

		try {
			return new JEditorPane("text/html", "<pre>"
					+ (String) dataFacade.resolve(entityIdentifier, String.class) + "</pre>");
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
