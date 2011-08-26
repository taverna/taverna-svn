package net.sf.taverna.t2.renderers;

import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Display the content of a URL.
 * 
 * @author Ian Dunlop
 */
public class TextTavernaWebUrlFetcherRenderer implements Renderer {
	private Pattern pattern;

	public TextTavernaWebUrlFetcherRenderer() {
		pattern = Pattern.compile(".*text/x-taverna-web-url.*");
	}

	public boolean isTerminal() {
		return false;
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		// FIXME needs to do something here
		// not sure what should happen here
		// {
		// Object dataObject = dataFacade.resolve(entityIdentifier);
		// try {
		// URL url = new URL((String) dataObject);
		// DataThing urlThing = DataThingFactory.fetchFromURL(url);
		// return renderers.getRenderer(urlThing).getComponent(
		// renderers, urlThing);
		// } catch (Exception ex) {
		// JTextArea theTextArea = new JTextArea();
		// theTextArea.setText((String) dataObject);
		// theTextArea.setFont(Font.getFont("Monospaced"));
		// return theTextArea;
		// }
		return null;
	}
}
