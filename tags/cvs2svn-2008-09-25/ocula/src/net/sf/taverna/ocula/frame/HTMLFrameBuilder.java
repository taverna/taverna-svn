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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JEditorPane;

import net.sf.taverna.ocula.ui.HTMLPane;
import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.OculaPanel;
import net.sf.taverna.ocula.ui.ResultSetPanel;

import org.apache.log4j.Logger;

/**
 * Handles the &lt;htmlframe&gt; element and builds a frame that subclasses
 * ResultSetPanel and is able to render html (inline or external).
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class HTMLFrameBuilder extends AbstractHTMLFrameBuilder implements FrameSPI {

    private static Logger log = Logger.getLogger(HTMLFrameBuilder.class);

    protected String name;
    protected Icon icon;

    public String getElementName() {
	return "htmlframe";
    }

    protected OculaPanel createHTMLFrame() {
	log.debug("creating frame");
	return new HTMLFrame(name, icon);
    }
    
    protected void processElement() {
	name = element.getAttributeValue("name");
	if (name == null) {
	    name = "No Name";
	}
	String iconName = element.getAttributeValue("icon");
	if (iconName == null) {
	    iconName = "NoIcon";
	}
	icon = Icons.getIcon(iconName);
	super.processElement();
    }
    
    /**
     * Creates the appropriate frame, activates the progress bar and loads
     * the view.
     */
    public OculaFrame makeView() {
	final HTMLFrame hf = (HTMLFrame) createHTMLFrame();
	hf.getProgressBar().setValue(0);
	hf.getProgressBar().setIndeterminate(true);
	loadView(hf, new Runnable() {
	    public void run() {
		hf.getProgressBar().setValue(100);
		hf.getProgressBar().setIndeterminate(false);
	    }
	});
	return hf;
    }
    
    /**
     * {@link ResultSetPanel} subclass that implements OculaFrame and
     * IHTMLFrame and renders HTML content.
     * 
     * @author Ismael Juma (ismael@juma.me.uk)
     *
     */
    class HTMLFrame extends ResultSetPanel implements OculaFrame, IHTMLFrame {

	protected JEditorPane htmlPane;

	public HTMLFrame(String name, Icon icon) {
	    super(name, icon);
	    contentsPanel.setLayout(new BorderLayout());
	    htmlPane = new HTMLPane();
	    contentsPanel.add(htmlPane, BorderLayout.CENTER);
	}

	public void setPage(String urlString) throws IOException,
		MalformedURLException {
	    URL url = new URL(urlString);
	    htmlPane.setPage(url);
	}

	public void setText(String contents) {
	    htmlPane.setText(contents);
	}
	
	public void setPreferredWidth(final int width) {
	    AbstractHTMLFrameBuilder.setPreferredWidth(htmlPane, this, width);
	}
	
    }
}
