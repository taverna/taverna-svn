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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import java.util.*;
import net.sf.taverna.ocula.ui.*;
import net.sf.taverna.ocula.action.ActionRunner;
import net.sf.taverna.ocula.renderer.RendererHandler;
import net.sf.taverna.ocula.frame.FrameHandler;
import net.sf.taverna.ocula.frame.OculaFrame;
import net.sf.taverna.ocula.validation.*;
import java.net.*;
import java.io.*;
import bsh.*;
import org.jdom.Element;
import org.apache.log4j.Logger;

/**
 * Top level container for an instance of Ocula. This is initialized
 * as a blank panel into which pages can be loaded.
 * @author Tom Oinn
 */
public class Ocula extends JPanel {
   
    private Map context;
    JPanel mainPanel;
    private ActionRunner actionRunner;
    private RendererHandler rendererHandler;
    private FrameHandler frameHandler;
    private URL currentURL = null;
    private JButton backButton, forwardButton, reloadButton;
    public static Logger logger = Logger.getLogger(Ocula.class);

    // Elements at position 0 are the most recent in each direction
    private List history = new ArrayList();
    private List future = new ArrayList();

    /**
     * Construct an empty top level panel
     */
    public Ocula() {
	buildUI();
	setTitle("No page loaded");
	context = new HashMap();
	actionRunner = new ActionRunner(this);
	rendererHandler = new RendererHandler(this);
	frameHandler = new FrameHandler(this);
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
	mainPanel.setOpaque(false);
	JScrollPane jsp = new JScrollPane(mainPanel);
	jsp.setOpaque(false);
	jsp.getViewport().setOpaque(false);
	jsp.setBorder(BorderFactory.createEmptyBorder());
	JPanel mainPanelContainer = new PaddedPanel("ocula.background", 8, jsp);
	add(mainPanelContainer, BorderLayout.CENTER);
    }
    
    /**
     * Create the toolbar
     */
    private JToolBar buildToolBar() {
	JToolBar bar = new JToolBar();
	bar.setFloatable(false);
	bar.setRollover(true);
	backButton = new CompactJButton(Icons.getIcon("back"), 26, 26);
	bar.add(backButton);
	backButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    URL working = currentURL;
		    try {
			load((URL)history.get(0));
		    }
		    catch (Exception ex) {
			try {
			    load(working);
			}
			catch (Exception ex2) {
			    // Should never reach here, if we had a working page to jump
			    // from we've just jumped back to it.
			}
		    }
		}
	    });
	reloadButton = new CompactJButton(Icons.getIcon("reload"), 26, 26); 
	bar.add(reloadButton);
	reloadButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			load(currentURL);
		    }
		    catch (Exception ex) {
			// Should never happen as we can only reload a
			// valid page. Admittedly could happen if an 
			// action has removed something from the context
		    }
		}
	    });
	forwardButton = new CompactJButton(Icons.getIcon("forward"), 26, 26);
	bar.add(forwardButton);
	forwardButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    URL working = currentURL;
		    try {
			load((URL)future.get(0));
		    }
		    catch (Exception ex) {
			try {
			    load(working);
			}
			catch (Exception ex2) {
			    // Should never reach here, if we had a working page to jump
			    // from we've just jumped back to it.
			}
		    }
		}
	    });
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
     * Set the enabled status of the history and future buttons
     */
    public void setHistoryButtons() {
	if (history.isEmpty()) {
	    backButton.setEnabled(false);
	}
	else {
	    backButton.setEnabled(true);
	}
	if (future.isEmpty()) {
	    forwardButton.setEnabled(false);
	}
	else {
	    forwardButton.setEnabled(true);
	}
	if (currentURL == null) {
	    reloadButton.setEnabled(false);
	}
	else {
	    reloadButton.setEnabled(true);
	}
    }

    /**
     * Set the current page location
     */
    public void load(URL pageURL) throws PageValidationException, IOException {
	// Push the current URL into the history if appropriate
	if (currentURL != null && currentURL.equals(pageURL) == false) {
	    // If this new URL isn't the most recent page in the history
	    // or the future, i.e. a step back and forward, then we nuke
	    // the future (symbolic huh?)
	    if (future.isEmpty()==false && ((URL)future.get(0)).equals(pageURL)) {
		// moved one step into the future, push old page onto the history
		// and remove the first page from the future as we're now on it
		if (history.isEmpty() || ((URL)history.get(0)).equals(currentURL)==false) {
		    history.add(0, currentURL);
		}
		future.remove(0);
	    }
	    else if (history.isEmpty()==false && ((URL)history.get(0)).equals(pageURL)) {
		// moved one step back into the past
		if (future.isEmpty() || ((URL)future.get(0)).equals(currentURL)==false) {
		    future.add(0, currentURL);
		}
		history.remove(0);
	    }
	    else {
		// New page we haven't seen before
		future.clear();
		history.add(0, currentURL);
	    }
	}		
	currentURL = pageURL;
	setHistoryButtons();
	Page p = new Page(pageURL);
	setTitle(p.getTitle());
	// Do pre-load actions
	Element actions = p.getInitActions();
	try {
	    actionRunner.runAction(actions);
	}
	catch (Exception ex) {
	    logger.error("Failed to run page pre-load actions",ex);
	}
	// Load the contents of the page (should do this _after_ running any page actions)
	java.util.List contents = p.getContents();
	mainPanel.removeAll();
	mainPanel.revalidate();
	for (Iterator i = contents.iterator(); i.hasNext();) {
	    OculaFrame frame = frameHandler.getFrame((Element)i.next());
	    if (frame != null) {
		Component c = (Component)frame;
		mainPanel.add(c);
		mainPanel.revalidate();
	    }
	}
    }
    
    /**
     * Return a reference to the instance's RendererHandler, used generally by the
     * various frame creator objects to correctly populate their UIs.
     */
    public RendererHandler getRendererHandler() {
	return this.rendererHandler;
    }

    /**
     * Return a reference to the ActionRunner attached to this Ocula
     */
    public ActionRunner getActionRunner() {
	return this.actionRunner;
    }

    /**
     * Return a URL for the current page definition
     */
    public URL getCurrentURL() {
	return this.currentURL;
    }

    /**
     * Evaluate an expression in terms of the current context using the Beanshell,
     * the specified 'script' parameter has the static string 'returnValue =' 
     * prepended to it and all context variables inserted into the environment. This
     * is used as a shortcut allowing the script to be something like 'foo.getChildren()'
     * where 'foo' is something in the context that has a 'getChildren()' method, the
     * BeanShell allows untyped scripts like this which makes life a whole lot easier.
     * For complex scripts where the task is to actually run some algorithm then fetch
     * multiple results use the runScript method.
     * @param script A valid script in BeanShell interpreted java
     * @exception EvalError propogated from the BeanShell Interpreter
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

    /**
     * Evaluate a BeanShell script, all context objects are inserted into the script
     * context.
     * @param script A valid script in BeanShell interpreted java
     * @param extract Array of strings, each element in the array is a named object
     * bound within the BeanShell interpreter which should be inserted into the
     * context after the script has completed
     * @exception EvalError propogated from the BeanShell Interpreter
     */
    public void runScript(String script, String[] extract) throws EvalError {
	Interpreter i = new Interpreter();
	for (Iterator contextIterator = context.keySet().iterator(); contextIterator.hasNext();) {
	    String keyName = (String)contextIterator.next();
	    i.set(keyName, context.get(keyName));
	}
	i.eval(script);
	for (int j = 0; j < extract.length; j++) {
	    context.put(extract[j], i.get(extract[j]));
	}
    }

}
