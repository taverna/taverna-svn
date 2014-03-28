/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
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

package net.sf.taverna.tools.apiconsumer;

import javax.swing.*;

/**
 * A JEditorPane subclass with preset style sheet. Note that the
 * setText method now takes the text inside the &lt;body&gt; tags
 * rather than the entire HTML document.
 * @author Tom Oinn
 */
public class ClassSummaryPane extends JEditorPane {
 
    private static String header = 
	"<html><head><style type=\"text/css\">"+
	"body {\n"+
	"  background-color: #eeeeee;\n"+
	"font-family: Helvetica, Arial, sans-serif;\n"+
	"font-size: 12pt;\n"+
	"}\n"+
	"blockquote {\n"+
	"  padding: 5px;\n"+
	"  background-color: #ffffff;\n"+
	"  border-width: 1px; border-style: solid; border-color: #aaaaaa;\n"+
	"}\n"+
	"</style></head><body>\n";
    
    private static String footer = "</body></html>";
    
    public ClassSummaryPane() {
	super("text/html","");
	setText("");
	setEditable(false);
    }
    
    public void setText(String newText) {
	super.setText(header + newText + footer);
    }
    
}
