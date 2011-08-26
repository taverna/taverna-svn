/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.renderers;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.embl.ebi.escience.baclava.DataThing;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;

/**
 * Renders using the Jmol software for chemical structures
 * @author Tom Oinn
 */
public class JMolRenderer extends AbstractRenderer.ByMimeType {
    
    public JMolRenderer() {
	super("Jmol");
    }
    
    public boolean isTerminal() {
	return true;
    }
    
    public boolean canHandle(RendererRegistry renderers,
			     Object userObject,
			     String mimeType) {
	if (userObject instanceof String) {
	    if (mimeType.matches(".*chemical/x-pdb.*") ||
		mimeType.matches(".*chemical/x-mdl-molfile.*") ||
		mimeType.matches(".*chemical/x-cml.*")) {
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
	if (((JmolViewer)viewer).getAtomCount() > 300) {
	    viewer.evalString(proteinScriptString);
	}
	else {
	    viewer.evalString(scriptString);
	}	
	return panel;	
    }
    
    static final String proteinScriptString = "wireframe off; spacefill off; select protein; cartoon; colour structure; select ligand; spacefill; colour cpk; select dna; spacefill 0.4; wireframe on; colour cpk;";

    static final String scriptString = "select *; spacefill 0.4; wireframe 0.2; colour cpk;";
    
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
