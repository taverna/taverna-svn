/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.renderers;

import org.jmol.api.*;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.embl.ebi.escience.baclava.*;
import org.jmol.viewer.Viewer;

/**
 * Renders using the Jmol software for chemical structures
 * @author Tom Oinn
 */
public class JMolRenderer extends AbstractRenderer.ByMimeType {
    
    public JMolRenderer() {
	super("JMol");
    }
    
    public boolean isTerminal() {
	return true;
    }
    
    public boolean canHandle(RendererRegistry renderers,
			     Object userObject,
			     String mimeType) {
	if (userObject instanceof String) {
	    if (mimeType.matches(".*chemical/x-pdb.*")) {
		return true;
	    }
	}
	return false;
    }
    
    public JComponent getComponent(RendererRegistry renderers,
				   DataThing dataThing)
	throws RendererException {
	JMolPanel panel = new JMolPanel();
	String coordinateText = (String)dataThing.getDataObject();
	JmolSimpleViewer viewer = panel.getViewer();
	viewer.openStringInline(coordinateText);
	viewer.evalString(scriptString);
	return panel;	
    }
    
    static final String scriptString = "wireframe off; spacefill off; select protein; cartoon; colour structure; select ligand; spacefill; colour cpk; select dna; spacefill 0.4; wireframe on; colour cpk;";
}    

class JMolPanel extends JPanel {
    JmolSimpleViewer viewer;
    JmolAdapter adapter;
    JMolPanel() {
	adapter = new SmarterJmolAdapter(null);
	viewer = Viewer.allocateJmolSimpleViewer(this, adapter);
	//viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
    }
    
    public JmolSimpleViewer getViewer() {
	return viewer;
    }
    
    final Dimension currentSize = new Dimension();
    final Rectangle rectClip = new Rectangle();
    
    public void paint(Graphics g) {
	getSize(currentSize);
	g.getClipBounds(rectClip);
	viewer.renderScreenImage(g, currentSize, rectClip);
    }
}
