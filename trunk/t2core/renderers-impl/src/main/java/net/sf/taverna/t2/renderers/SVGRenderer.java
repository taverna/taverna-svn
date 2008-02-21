package net.sf.taverna.t2.renderers;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * This class renders SVG Documents.
 * 
 * Last edited by $Author: iandunlop $
 * 
 * @author Mark
 * @author Ian Dunlop
 */
public class SVGRenderer implements Renderer {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SVGRenderer.class);
	private Pattern pattern;

	public SVGRenderer() {
		pattern = Pattern.compile(".*image/svg[+]xml.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
		JSVGCanvas svgCanvas = new JSVGCanvas();
		Object resolve = null;
		try {
			resolve = dataFacade.resolve(entityIdentifier);
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (resolve != null && resolve instanceof String && !resolve.equals("")) {
			String svgContent = (String) resolve;
			File tmpFile = null;
			try {
				tmpFile = File.createTempFile("taverna", "svg");
				tmpFile.deleteOnExit();
				FileUtils.writeStringToFile(tmpFile, svgContent, "utf8");
			} catch (IOException e) {
				//TODO needs exception handling
				e.printStackTrace();
			}
			svgCanvas.setURI(tmpFile.toURI().toASCIIString());
			return svgCanvas;
		} else {
			return null;
		}
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) {
		return canHandle(mimeType);
	}

	public String getType() {
		return "SVG";
	}

}
