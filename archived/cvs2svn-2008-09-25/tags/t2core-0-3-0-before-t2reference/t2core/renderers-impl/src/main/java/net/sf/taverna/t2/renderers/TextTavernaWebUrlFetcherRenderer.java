package net.sf.taverna.t2.renderers;


import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Display the content of a URL.
 *
 * @author Ian Dunlop
 */
public class TextTavernaWebUrlFetcherRenderer
        implements Renderer
{
    private Pattern pattern;

	public TextTavernaWebUrlFetcherRenderer()
    {
    	pattern = Pattern.compile(".*text/x-taverna-web-url.*");
    }

    public boolean isTerminal()
    {
        return false;
    }



	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
		//not sure what should happen here
//		{
//	        Object dataObject = dataFacade.resolve(entityIdentifier);
//	        try {
//	            URL url = new URL((String) dataObject);
//	            DataThing urlThing = DataThingFactory.fetchFromURL(url);
//	            return renderers.getRenderer(urlThing).getComponent(
//	                    renderers, urlThing);
//	        } catch (Exception ex) {
//	            JTextArea theTextArea = new JTextArea();
//	            theTextArea.setText((String) dataObject);
//	            theTextArea.setFont(Font.getFont("Monospaced"));
//	            return theTextArea;
//	        }
		return null;
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) {
		return canHandle(mimeType);
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}
 }
