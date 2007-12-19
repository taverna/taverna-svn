package net.sourceforge.taverna.scuflui.renderers;


import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.AbstractRenderer.ByPattern;
import org.embl.ebi.escience.scuflui.spi.RendererSPI;
import org.xml.sax.SAXException;

/**
 * This class renders SVG Documents.
 * 
 * Last edited by $Author: stain $
 * 
 * @author Mark
 * @version $Revision: 1.5 $
 */
public class SVGRenderer extends ByPattern implements RendererSPI {

	private static Logger logger = Logger.getLogger(SVGRenderer.class);

	public SVGRenderer() {
		 super("SVG Image",
	              new ImageIcon(SVGRenderer.class.getClassLoader().getResource(
	                "org/embl/ebi/escience/baclava/icons/image.png")),
	              Pattern.compile(".*image/svg[+]xml.*"));
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.spi.RendererSPI#isTerminal()
	 */
	public boolean isTerminal() {
		return true;
	}

	/**
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @see org.embl.ebi.escience.scuflui.spi.RendererSPI#getComponent(RendererRegistry,
	 *      org.embl.ebi.escience.baclava.DataThing)
	 */
	public JComponent getComponent(RendererRegistry renderers, DataThing dataThing) throws RendererException {
		JSVGCanvas svgCanvas = new JSVGCanvas();
		// svgCanvas.setURI(f.toURL().toString());

		Object data = dataThing.getDataObject();
		String mimetype = dataThing.getSyntacticType();
		if (data != null && data instanceof String && !data.equals("")) {
				String svgContent = (String) data;
				File tmpFile;
				try {
					tmpFile = File.createTempFile("taverna", "svg");
					tmpFile.deleteOnExit();
					FileUtils.writeStringToFile(tmpFile, svgContent, "utf8");
				} catch (IOException e) {
					throw new RendererException(e);
				}
				svgCanvas.setURI(tmpFile.toURI().toASCIIString());
			return svgCanvas;
		} else {
			return null;
		}

	}

}
