/**
 * 
 */
package net.sf.taverna.t2.lineageService;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

/**
 * @author paolo
 *
 */
public class AnnotationsLoader {

	private static final String TOP_ELEMENT = "annotations";
	private static final String PROCESSOR = "processor";
	private static final String ANNOTATIONS = "annotations";


	/**
	 * 
	 * @param annotationFile  by convention we use <workflow file name>+"annotations"
	 * @return a map pname -> annotation so that the lineage query alg can use the annotation
	 * when processing pname
	 */
	public Map<String,List<String>>  getAnnotations(String annotationFile)  {


		Map<String,List<String>>  procAnnotations = new HashMap<String,List<String>>();

		// load XML file as doc
//		parse the event into DOM
		SAXBuilder  b = new SAXBuilder();
		Document d;

		try {
			d = b.build (new FileReader(annotationFile));

			if (d == null)  return null;
			
			Element root = d.getRootElement();

			// look for all processor elements
			List<Element> processors = root.getChildren();
			
			for (Element el:processors) {
				
				String pName = el.getAttributeValue("name");
				System.out.println("processor name: "+pName);
				
				List<String>  annotations = new ArrayList<String>();
				// extract all annotations for this pname

				List<Element> annotEl = el.getChildren();
				
				for (Element annotElement: annotEl) {
					
					String annot = annotElement.getAttributeValue("type");
					System.out.println("annotation: "+annot);

					// add this annotation
					annotations.add(annot);
				}

				procAnnotations.put(pName, annotations);
				
			}
			

		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return procAnnotations;


	}
}
