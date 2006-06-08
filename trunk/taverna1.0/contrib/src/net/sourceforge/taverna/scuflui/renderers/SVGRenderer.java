package net.sourceforge.taverna.scuflui.renderers;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.swing.JSVGCanvas;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.RendererSPI;
import org.embl.ebi.escience.scuflui.renderers.AbstractRenderer.ByPattern;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class renders SVG Documents.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.4 $
 */
public class SVGRenderer extends ByPattern implements RendererSPI{

    /**
     * @param name
     * @param icon
     * @param pattern
     */
    public SVGRenderer(String name, Icon icon, Pattern pattern) {
        super(name, icon, pattern);
    }

    /**
     * @see org.embl.ebi.escience.scuflui.renderers.RendererSPI#isTerminal()
     */
    public boolean isTerminal() {
        return true;
    }

    /**
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @see org.embl.ebi.escience.scuflui.renderers.RendererSPI#getComponent(org.embl.ebi.escience.scuflui.renderers.RendererRegistry, org.embl.ebi.escience.baclava.DataThing)
     */
    public JComponent getComponent(RendererRegistry renderers, DataThing dataThing) throws RendererException {
        JSVGCanvas svgCanvas = new JSVGCanvas();
        //svgCanvas.setURI(f.toURL().toString());
		
        Object data = dataThing.getDataObject();
        String mimetype = dataThing.getSyntacticType();
		System.out.println(" *** mimeType: " + mimetype);
        if(data instanceof String && data != null && !data.equals("")) {
            
			if ("'image/svg+xml'".equals(mimetype)){
                String svgContent = (String)data;
                
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
    
                // Create the builder and parse the file
                Document svgDoc;
                try {
                    svgDoc = factory.newDocumentBuilder().parse(svgContent);
                } catch (SAXException e) {
                    throw new RendererException(e);
                } catch (IOException e) {
                    throw new RendererException(e);
                } catch (ParserConfigurationException e) {
                    throw new RendererException(e);
                }
                //Document svgDoc = 
                svgCanvas.setDocument(svgDoc);
            }
            return svgCanvas;
        } else {
            return null;
        }

    }

}
