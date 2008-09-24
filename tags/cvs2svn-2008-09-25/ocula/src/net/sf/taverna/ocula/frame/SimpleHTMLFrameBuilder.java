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

import javax.swing.JEditorPane;

import net.sf.taverna.ocula.ui.HTMLPane;
import net.sf.taverna.ocula.ui.OculaPanel;

/**
 * Handles the &lt;simplehtmlframe&gt; element and builds a frame that
 * subclasses OculaPanel and is able to render html (inline or external).
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class SimpleHTMLFrameBuilder extends AbstractHTMLFrameBuilder implements
	FrameSPI {

    public String getElementName() {
	return "simplehtmlframe";
    }

    protected OculaPanel createHTMLFrame() {
	return new SimpleHTMLFrame();
    }

    protected OculaFrame makeView() {
	final SimpleHTMLFrame hf = (SimpleHTMLFrame) createHTMLFrame();
	loadView(hf);
	return hf;
    }

    /**
     * {@link OculaPanel} subclass that implements OculaFrame and IHTMLFrame
     * and renders HTML content.
     * 
     * @author Ismael Juma (ismael@juma.me.uk)
     *
     */
    class SimpleHTMLFrame extends OculaPanel implements OculaFrame, IHTMLFrame	{
	protected JEditorPane htmlPane;
	
	public SimpleHTMLFrame() {
	    htmlPane = new HTMLPane();
	    getContents().setLayout(new BorderLayout());
	    getContents().add(htmlPane, BorderLayout.CENTER);
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
