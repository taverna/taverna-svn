/*
 * Copyright 2005 University of Manchester
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.Parser;
import net.sf.taverna.ocula.ui.ErrorLabel;
import net.sf.taverna.ocula.ui.OculaPanel;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

/**
 * Abstract class that provides common functionality needed by the
 * *InputFrameBuilder classes.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public abstract class AbstractInputFrameBuilder implements FrameSPI {

    private static Logger log = Logger.getLogger(AbstractInputFrameBuilder.class);
    
    protected OculaPanel errorPanel;
    protected Ocula ocula;
    protected Element element;
    protected int cols = 2;
    protected Parser parser;
    protected Object result;

    protected int hGap = -1;

    protected int vGap = -1;
    
    /**
     * Factory method that allows subclasses the decide what object is used
     * as the frame.
     * @return OculaPanel instance.
     */
    protected abstract OculaPanel createInputFrame();
    
    public OculaFrame makeFrame(Ocula o, Element element) {
	this.element = element;
	ocula = o;	
	if (processElement()) {
	    return makeView();
	}
	return (OculaFrame) errorPanel;
    }
    
    /**
     * Allows subclasses to 'drive' the creation of the view.
     * @return An object that implements OculaFrame and is also a JComponent.
     */
    public abstract OculaFrame makeView();
    
    /**
     * Iterates through the elements inside the &lt;input&gt; element,
     * determines the appropriate component and adds the component to
     * the contents are of the <code>oculaPanel</code>.
     * 
     * @param oculaPanel
     */
    public void loadView(OculaPanel oculaPanel) {
	final List elements = element.getChildren();
	try {
	    result = processScript(element, oculaPanel);
	    if (result != null) {
		ocula.putContext("value", result);
	    }
	    
	    int scriptCount = 0;
	    for (Iterator it = elements.iterator(); it.hasNext();) {
		Element e = (Element) it.next();
		
		if (e.getName().equals("script")) {
		    if (scriptCount > 0) {
			log.warn("There is more than one <script> element inside" +
				"the <input> element. Only the first one" +
			"will be considered.");
		    }
		    ++scriptCount;
		    continue;
		}
		if (e.getName().equals("text")) {
		    log.debug("text element found");
		    processText(e, oculaPanel);
		}
		
		else if (e.getName().equals("button")) {
		    log.debug("button element found.");
		    processButton(e, oculaPanel);
		}
		
		else if(e.getName().equals("separator")) {
		    log.debug("separator element found.");
		    processSeparator(e, oculaPanel);
		}
		else {
		    //TODO Probably a good idea to create or reuse an exception
		    //to deal with these errors.
		    String msg = "is not a supported element.";
		    displayError(oculaPanel, "<html><body>&lt;" + e.getName() +
			    "&gt; " + msg + "</body>" +
		    "</html>");
		    oculaPanel.revalidate();
		    log.error("<" + e.getName() + "> " + msg);
		    return;
		}
	    }
	    
	    oculaPanel.getContents().revalidate();
	}
	catch(EvalError ee) {
	    //Do nothing as the processScript method has already handled the
	    //error. The exception is rethrown simply to let this method know
	    //that it should not try to populate the panel.
	}
    }
    
    /**
     * Displays an error message in the provided oculaPanel.
     * 
     * @param oculaPanel OculaPanel where error should be displayed.
     * @param msg Message to display.
     */
    private void displayError(OculaPanel oculaPanel, String msg) {
	ErrorLabel label = new ErrorLabel(msg);
	oculaPanel.getContents().removeAll();
	oculaPanel.getContents().setLayout(new BorderLayout());
	oculaPanel.getContents().add(label, BorderLayout.NORTH);
    }
    
    /**
     * Processes a &lt;button&gt; element. It bypasses the RendererHandler
     * and creates a JButton with the correct text.
     * 
     */
    private void processButton(Element buttonElement, OculaPanel oculaPanel)	{
	JButton button = null;
	try {
	    Object targetObject = processScript(buttonElement, oculaPanel);
	    if (targetObject == null) {
		String text = buttonElement.getAttributeValue("name");
		button = new JButton(text);
		parser.parseAction(buttonElement, button, null);
	    }
	    else {
		button = new JButton((String) targetObject);   
	    }
	    oculaPanel.getContents().add(button);
	}
	catch(EvalError ee) {
	    //Do nothing as the processScript method has already handled the
	    //error. The exception is rethrown simply to let this method know
	    //that it should not try to populate the panel.
	}
    }   

    /**
     * Processes the label attribute of &lt;text&gt;.
     * @param textElement &lt;text&gt; element.
     * @param oculaPanel panel where any error message should be displayed
     * @return JComponent that renders the label attribute.
     */
    private JComponent processLabel(Element textElement, OculaPanel oculaPanel) {
	String value = textElement.getAttributeValue("label");
	//If there is no label attribute, we set the label to an empty string.
	//The alternative would be to fallback to the "name" attribute. This,
	//however, would make it impossible to have an empty label, which
	//may be desirable in some cases.
	if (value == null) {
	    value = "";
	}
	return ocula.getRendererHandler().getRenderer(
		    value);
    }
    
    /**
     * Processes a &lt;text&gt; element.
     * @param textElement
     * @param oculaPanel
     */
    private void processText(Element textElement, OculaPanel oculaPanel) {
	oculaPanel.getContents().add(processLabel(textElement, oculaPanel));
	String value = textElement.getTextTrim();
	String name = textElement.getAttributeValue("name");
	JComponent component = null;
	try {
	    Object targetObject = processScript(textElement, oculaPanel);
	    if (targetObject == null) {
		String typeString = textElement.getAttributeValue("type", "normal");
		Document document = new PlainDocument();
		targetObject = document;
		if (!value.equals("")) {
		    try {
			document.insertString(0, value, null);
		    }
		    catch (BadLocationException ble) {
			// should never happen
			log.error(ble);
		    }
		}
		if (typeString.equals("password")) {
		    component = new JPasswordField(document, null, 0);
		}
		else if (typeString.equals("normal")) {
		    component = ocula.getRendererHandler().getRenderer(
			    targetObject);
		}
		else {
		    String msg1 = "the type attribute can only be ";
		    String msg2 = "'normal' or 'password'.";
		    displayError(oculaPanel, "<html><body>" + msg1 + "<p>" + msg2);
		    log.error(msg1 + msg2);
		    return;
		}
	    }
	    else {
		component = ocula.getRendererHandler().getRenderer(targetObject);
	    }
	    if (name != null) {
		ocula.putContext(name, targetObject);
	    }
	    oculaPanel.getContents().add(component);
	}
	catch(EvalError ee) {
	    //Do nothing as the processScript method has already handled the
	    //error. The exception is rethrown simply to let this method know
	    //that it should not try to populate the panel.
	}
    }
    
    /**
     * Processes the &lt;separator&gt; elemenet. At the moment, it simply adds
     * an empty panel with the height given by the 'height' attribute. Other
     * capabilities may be added in the future.
     * @param separatorElement
     * @param panel
     */
    private void processSeparator(Element separatorElement, OculaPanel panel) {
	String heightString = separatorElement.getAttributeValue("height", "5");
	if (heightString == null) {
	    //We fall back to the default of 5 in this case
	    log.warn("Separator does not have a height attribute.");
	}
	int height = 0;
	try {
	    height = Integer.parseInt(heightString);
	    JPanel newPanel = new JPanel();
	    newPanel.setOpaque(false);
	    newPanel.setPreferredSize(new Dimension(0, height));
	    panel.getContents().add(newPanel);
	}
	catch(NumberFormatException nfe) {
	    displayError(panel, "<html><body>The height " +
		    "attribute of &lt;separator&gt;<p> must be a number.</body>" +
		    "</html>");
	    panel.revalidate();
	    log.error("The height attribute of <separator> must be a number, " +
		    heightString);
	}
	
    }
    
    /**
     * Processes a &lt;script&gt; element and returns the result of the script.
     * If an exception is thrown, it shows an error message and writes
     * the details to the log.
     * @param e Any element that contains a &lt;script&gt; element.
     * @param panel The panel where the error message should be displayed.
     * @return The first object returned from the execution of the script.
     */
    private Object processScript(Element e, OculaPanel panel) throws EvalError {
	Object targetObject = null;
	if (e.getChild("script") ==  null) {
	    return null;
	}
	
	try {
	    targetObject = parser.parseScript(e)[0];
	}
	catch (EvalError ee) {
	    displayError(panel, "<html><body>" +
	    		"Error evaluating script.<p>" +
	    		"See error log for more details.</body></html>");
	    panel.revalidate();
	    log.error("Error evaluating the script: " + ee);
	    throw ee;
	}
	return targetObject;
    }
    
    /**
     * Does minimal processing on the root element. It simply reads the cols
     * attribute.
     * 
     * @return true if there are no problems processing the element.
     */
    protected boolean processElement()	{
	parser = new Parser(ocula);
	String colsString = element.getAttributeValue("cols");
	String hGapString = element.getAttributeValue("hGap");
	String vGapString = element.getAttributeValue("vGap");
	try {
	    if (colsString != null) {
		cols = Integer.parseInt(colsString);
	    }
	}
	catch (NumberFormatException nfe) {
	    logNumberFormatException("cols", colsString);
	    return false;
	}
	try {
	    if (hGapString != null) {
		hGap = Integer.parseInt(hGapString);
	    }
	}
	catch (NumberFormatException nfe) {
	    logNumberFormatException("hGap", hGapString);
	    return false;
	}
	try {
	    if (vGapString != null) {
		vGap = Integer.parseInt(vGapString);
	    }
	}
	catch (NumberFormatException nfe) {
	    logNumberFormatException("vGap", vGapString);
	    return false;
	}
	return true;
    }
    
    /**
     * Logs an error in cases where an attribute that expects a number receives
     * something else.
     * @param oculaPanel panel where any errors should be reported.
     * @param attributeName the name of the attribute in the element.
     * @param notNumber the String that was received instead of a number.
     */
    private void logNumberFormatException(String attributeName, String notNumber) {
	String msg1 = attributeName + " attribute must hold" +
			" a number ";
	String msg2 = "instead of '" + notNumber + "'.";
	errorPanel = createInputFrame();
	displayError(errorPanel, "<html><body>" + msg1 + "<p>" + msg2 +
		"</body></html>" );
	log.error(msg1 + msg2);
    }
}