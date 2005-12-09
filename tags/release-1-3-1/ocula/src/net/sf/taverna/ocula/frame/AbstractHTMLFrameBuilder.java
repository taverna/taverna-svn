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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.ui.OculaPanel;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * Abstract class that contains the common functionality required by the
 * classes that handle the &lt;htmlframe&gt; and &lt;simplehtmlframe&gt;
 * elements.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public abstract class AbstractHTMLFrameBuilder implements FrameSPI {

    private static Logger log = Logger
	    .getLogger(AbstractHTMLFrameBuilder.class);

    private static final int INLINE = 0;

    private static final int EXTERNAL = 1;

    private int mode = INLINE;

    protected Element element;
    
    protected Ocula ocula;

    public OculaFrame makeFrame(Ocula o, final Element element) {
	this.element = element;
	this.ocula = o;
	processElement();
	return makeView();
    }

    /**
     * Abstract factory method that allows subclasses to decide what object
     * should be used as the frame.
     * 
     * @return OculaPanel that implements OculaFrame.
     */
    protected abstract OculaPanel createHTMLFrame();

    /**
     * Does some initial processing on the root element.
     *
     */
    protected void processElement() {
	String typeString = element.getAttributeValue("type");
	if (typeString != null) {
	    if (typeString.equals("inline")) {
		mode = INLINE;
	    }

	    else if (typeString.equals("external")) {
		mode = EXTERNAL;
	    }

	    else {
		log.error("type has an invalid value");
	    }
	}
    }

    /**
     * This abstract method allows subclasses to define how the view is
     * created.
     * @return The OculaFrame created.
     */
    protected abstract OculaFrame makeView();

    /**
     * Provides a default implementation for loading the view.
     * 
     * @param frame the IHTMLFrame where the view should be loaded.
     */
    protected void loadView(final IHTMLFrame frame) {
	loadView(frame, null);
    }

    /**
     * Provides a default implementation for loading the view.
     * 
     * @param frame the IHTMLFrame where the view should be loaded
     * @param r Runnable that should called upon the completion of the
     *          loading. This Runnable will be run from the event
     *          queue.
     */
    protected void loadView(final IHTMLFrame frame, final Runnable r) {
	// We set the preferred width to be the size of the JFrame - 50, to
	// allow the panel to fit within the JFrame.
	frame.setPreferredWidth(ocula.getParentFrame().getSize().width - 50);
	if (mode == EXTERNAL) {
	    loadViewExternal(frame, r);
	}
	else if (mode == INLINE) {
	    loadViewInline(frame, r);
	}

    }

    /**
     * Loads the view using the EXTERNAL mode and calls r from the event
     * queue thread when this operation is over.
     * 
     * @param frame IHtmlFrame where the page is supposed to be changed.
     * @param r Runnable to be called when the operation is finished.
     */
    protected void loadViewExternal(IHTMLFrame frame, Runnable r) {
	Element urlElement = element.getChild("url");
	if (urlElement == null) {
	    log.error("'external' mode specified, but no <url> element present.");
	}
	try {
	    frame.setPage(urlElement.getValue());
	    r.run();
	}
	catch (MalformedURLException mue) {
	    log.error("Malformed URL", mue);
	}
	catch (IOException ioe) {
	    log.error("IOException", ioe);
	}
    }

    /**
     * Loads the view using the INLINE mode in a new Thread and calls
     * <code>r</code> from the event queue thread when the activity is over.
     * @param frame IHtmlFrame where the text is supposed to be replaced.
     * @param r Runnable to be called when the operation is finished.
     */
    protected void loadViewInline(final IHTMLFrame frame, final Runnable r) {
	log.debug("Using INLINE mode.");
	final Element inlineElement = element.getChild("html");
	new Thread() {
	    public void run() {
		XMLOutputter out = new XMLOutputter();
		frame.setText(out.outputString(inlineElement));
		if (r != null) {
		    SwingUtilities.invokeLater(r);
		}
	    }
	}.start();
    }
    
    /**
     * Helper method that sets the preferred width of a JEditorPane and a 
     * JPanel (which usually contains the JEditorPane). Since Swing does not
     * support setting the preferred width without setting the preferred height,
     * this method waits until a page property change occurs, retrieves the
     * preferred height and then using the provided value for preferred width,
     * sets a new preferred size.
     * 
     * @param htmlPane JEditorPane whose preferred width must be changed.
     * @param panel JPanel whose preferred width must be changed.
     * @param width new preferred width.
     */
    protected static void setPreferredWidth(JEditorPane htmlPane,
	    final JPanel panel, final int width) {
	htmlPane.addPropertyChangeListener("page", new PropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent evt) {
		JComponent src = (JComponent) evt.getSource();
		Dimension preferredSize = src.getPreferredSize();
		// TODO This is a bit hacky. For some reason, if we don't change
		// the preferred height, the bottom part of the page gets
		// clipped. Adding 100 seems to work, but this requires more
		// testing and if possible a cleaner solution.
		preferredSize.height += 100;
		preferredSize.width = width;
		src.setPreferredSize(preferredSize);
		panel.setPreferredSize(preferredSize);
		panel.revalidate();
	    }
	});
    }
}
