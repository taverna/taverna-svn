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

/**
 * Simple interface that has the methods that Frames containing HTML may support.
 */
import java.io.IOException;
import java.net.MalformedURLException;

interface IHTMLFrame	{
    
    /**
     * Loads the HTML page located in <code>urlString</code> and renders it
     * in the frame.
     * @param urlString Location of the HTML page.
     * @throws IOException for a <code>null</code> or invalid page specification
     *         or an exception from the stream being read.
     * @throws MalformedURLException if the string is not a valid URL.
     */
    public void setPage(String urlString) throws IOException,
    	MalformedURLException;
    
    /**
     * Sets the text of this frame to the specified content and renders it
     * in the frame.
     * @param htmlString The content to be set. This should be valid HTML.
     */
    public void setText(String htmlString);
    
    /**
     * Sets the preferred width of the IHTMLFrame. Due to the fact that Swing
     * does not support setting the preferred width without setting the
     * preferred height, the implementations of this should make an effort
     * to retrieve the current preferred height after a page has been loaded. 
     * @param width the value to set the preferred width.
     */
    public void setPreferredWidth(int width);
}