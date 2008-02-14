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
public class TextTavernaWebUrlRenderer
        implements Renderer
{
    private Pattern pattern;

	public TextTavernaWebUrlRenderer()
    {
        pattern = Pattern.compile(".*text/x-taverna-web-url.*");
    }

//    public boolean isTerminal()
//    {
//        return true;
//    }


	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
		// TODO Auto-generated method stub
		{
			Object dataObject = null;
			try {
				dataObject = dataFacade.resolve(entityIdentifier);
			} catch (RetrievalException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				JEditorPane jep = new JEditorPane();
				String url = dataObject.toString();
				jep.setContentType("text/html");
				jep.setText("<a href=\"" + url + "\">" + url + "</a>");
				return jep;
			} catch (Exception ex) {
				JTextArea theTextArea = new JTextArea();
				theTextArea.setText((String) dataObject);
				theTextArea.setFont(Font.getFont("Monospaced"));
				return theTextArea;
			}
	}
 }
}
