package net.sf.taverna.t2.workbench.models.graph.svg;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import net.sf.taverna.t2.lang.io.StreamDevourer;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfiguration;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGLocatable;
import org.w3c.dom.svg.SVGMatrix;

/**
 * Utility methods.
 * 
 * @author David Withers
 */
public class SVGUtil {

	public static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

	private static Logger logger = Logger.getLogger(SVGUtil.class);

	private static SAXSVGDocumentFactory docFactory;

	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		logger.info("Using XML parser " + parser);
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	/**
	 * Converts a point in screen coordinates to a point in document coordinates.
	 * 
	 * @param locatable
	 * @param screenPoint
	 * @return
	 */
	public static SVGOMPoint screenToDocument(SVGLocatable locatable, SVGOMPoint screenPoint) {
        SVGMatrix mat = locatable.getScreenCTM().inverse();
        return (SVGOMPoint) screenPoint.matrixTransform(mat);
	}
	
	/**
	 * Generates an SVGDocument from DOT text by calling out to GraphViz.
	 * 
	 * @param dotText
	 * @return
	 * @throws IOException
	 */
	public static SVGDocument getSVG(String dotText) throws IOException {
		// FIXME: Should use MyGridConfiguration.getProperty(), 
		// but that would not include the system property
		// specified at command line on Windows (runme.bat) 
		// and OS X (Taverna.app)
		String dotLocation = (String)WorkbenchConfiguration.getInstance().getPropertyMap().get("taverna.dotlocation");
		if (dotLocation == null) {
			dotLocation = "dot";
		}
		logger.debug("Invoking dot...");
		Process dotProcess = Runtime.getRuntime().exec(
				new String[] { dotLocation, "-Tsvg" });
		StreamDevourer devourer = new StreamDevourer(dotProcess
				.getInputStream());
		devourer.start();
		// Must create an error devourer otherwise stderr fills up and the
		// process stalls!
		StreamDevourer errorDevourer = new StreamDevourer(dotProcess
				.getErrorStream());
		errorDevourer.start();
		PrintWriter out = new PrintWriter(dotProcess.getOutputStream(), true);
		out.print(dotText);
		out.flush();
		out.close();
		
		String svgText = devourer.blockOnOutput();
		// Avoid TAV-424, replace buggy SVG outputted by "modern" GraphViz versions.
		// http://www.graphviz.org/bugs/b1075.html
		// Contributed by Marko Ullgren
		svgText = svgText.replaceAll("font-weight:regular","font-weight:normal");

		// Fake URI, just used for internal references like #fish
		return docFactory.createSVGDocument("http://taverna.sf.net/diagram/generated.svg", 
			new StringReader(svgText));
	}

}
