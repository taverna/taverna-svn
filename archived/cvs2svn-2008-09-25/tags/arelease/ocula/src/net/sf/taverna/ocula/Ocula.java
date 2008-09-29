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

package net.sf.taverna.ocula;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import net.sf.taverna.ocula.ui.*;
import net.sf.taverna.ocula.action.ActionRunner;
import net.sf.taverna.ocula.validation.*;
import java.net.*;
import java.io.*;
import bsh.*;

/**
 * Top level container for an instance of Ocula. This is initialized
 * as a blank panel into which pages can be loaded.
 * @author Tom Oinn
 */
public class Ocula extends JPanel {
   
    private Map context;
    private JPanel mainPanel;
    private ActionRunner actionRunner;

    /**
     * Construct an empty top level panel
     */
    public Ocula() {
	buildUI();
	setTitle("No page loaded");
	context = new HashMap();
    }
    
    /**
     * Set up the UI
     */
    private void buildUI() {
	setLayout(new BorderLayout());
	JToolBar toolbar = buildToolBar();
	add(toolbar, BorderLayout.PAGE_START);
	mainPanel = new JPanel();
	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
	JPanel mainPanelContainer = new PaddedPanel("ocula.background", 8, new JScrollPane(mainPanel));
	add(mainPanelContainer, BorderLayout.CENTER);
    }
    
    /**
     * Create the toolbar
     */
    private JToolBar buildToolBar() {
	JToolBar bar = new JToolBar();
	bar.setFloatable(false);
	bar.setRollover(true);
	JButton backButton = new CompactJButton(Icons.getIcon("back"), 26, 26);
	bar.add(backButton);
	backButton.setEnabled(false);
	JButton reloadButton = new CompactJButton(Icons.getIcon("reload"), 26, 26); 
	bar.add(reloadButton);
	JButton forwardButton = new CompactJButton(Icons.getIcon("forward"), 26, 26);
	bar.add(forwardButton);
	bar.add(Box.createHorizontalGlue());
	JButton stopButton = new CompactJButton(Icons.getIcon("stop"), 26, 26);
	bar.add(stopButton);
	bar.setBackground(ColourSet.getColour("ocula.background"));
	return bar;
    }

    /**
     * Walk up the container heirarchy until we either fall
     * off the top or reach a JFrame in which case set the
     * title of the JFrame to the specified string.
     */
    public void setTitle(String title) {
	Container container = this.getParent();
	while (container != null) {
	    if (container instanceof JFrame) {
		((JFrame)container).setTitle(title);
		break;
	    }
	    container = container.getParent();
	}
    }
  
    /**
     * Insert a value into the current context
     */
    public void putContext(String key, Object value) {
	context.put(key, value);
    }

    /**
     * Clear the context
     */
    public void clearContext() {
	context.clear();
    }

    /**
     * Remove a key from the context
     */
    public void removeKey(String key) {
	context.remove(key);
    }

    /**
     * Set the current page location
     */
    public void load(URL pageURL) throws PageValidationException, IOException {
	Page p = new Page(pageURL);
    }

    /**
     * Evaluate an expression in terms of the current context using the Beanshell
     */
    public Object evaluate(String script) throws EvalError {
	script = "returnValue = "+script;
	Interpreter i = new Interpreter();
	for (Iterator contextIterator = context.keySet().iterator(); contextIterator.hasNext();) {
	    String keyName = (String)contextIterator.next();
	    i.set(keyName, context.get(keyName));
	}
	i.eval(script);
	return i.get("returnValue");
    }

}
