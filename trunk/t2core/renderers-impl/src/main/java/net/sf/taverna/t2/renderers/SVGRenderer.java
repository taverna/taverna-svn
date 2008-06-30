package net.sf.taverna.t2.renderers;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

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

	public String getType() {
		return "SVG";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		JSVGCanvas svgCanvas = new JSVGCanvas();
		Object resolve = null;
		try {
			resolve = referenceService.renderIdentifier(reference,
					Object.class, null);
		} catch (Exception e) {
			throw new RendererException("Could not resolve " + reference, e);
		}
		if (resolve != null && resolve instanceof String && !resolve.equals("")) {
			String svgContent = (String) resolve;
			File tmpFile = null;
			try {
				tmpFile = File.createTempFile("taverna", "svg");
				tmpFile.deleteOnExit();
				FileUtils.writeStringToFile(tmpFile, svgContent, "utf8");
			} catch (IOException e) {
				throw new RendererException("Could not create SVG renderer", e);
			}
			try {
				svgCanvas.setURI(tmpFile.toURI().toASCIIString());
			} catch (Exception e) {
				throw new RendererException("Could not create SVG renderer", e);
			}
			return svgCanvas;
		}
		return null;
	}

}
