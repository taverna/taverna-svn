package org.embl.ebi.escience.scuflui.renderers;

import java.io.StringReader;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.shared.XMLTree;
import org.jdom.input.SAXBuilder;

/**
 * Viewer to display XML as a tree.
 * 
 * @author Matthew Pocock
 */
public class TextXml extends AbstractRenderer.ByPattern {
	public TextXml() {
		super("XML", new ImageIcon(TextXml.class.getClassLoader().getResource(
			"org/embl/ebi/escience/baclava/icons/text.png")),
			Pattern.compile(".*text/xml.*"));
	}
	
	private SAXBuilder saxBuilder = new SAXBuilder(false);

	protected boolean canHandle(RendererRegistry renderers, Object userObject,
		String mimeType) {
		if (! (super.canHandle(renderers, userObject, mimeType) 
			&& userObject instanceof String)) {
			return false;
		}
		// Return true only if it is parseable
		try {
			synchronized (saxBuilder) {
				saxBuilder.build(new StringReader((String) userObject));
			}
			return true;		
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isTerminal() {
		return true;
	}

	public JComponent getComponent(RendererRegistry renderers,
		DataThing dataThing) throws RendererException {
		DataThing copy = new DataThing(dataThing);
		copy.getMetadata().setMIMETypes(
			Arrays.asList(strip(dataThing.getMetadata().getMIMETypes())));

		try {
			return new XMLTree((String) dataThing.getDataObject());
		} catch (Exception ex) {
			throw new RendererException(ex);
		}
	}
}
