/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */

package org.embl.ebi.escience.scuflworkers.soaplab;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflui.processoractions.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.graph.GraphColours;
import org.embl.ebi.escience.scufl.view.*;
import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import java.net.URL;
import java.util.*;


/**
 * Shows the soaplab metadata, initially in plain XML form
 * but we'll add a stylesheet transform for it later.
 * @author Tom Oinn
 */
public class SoaplabDescriberPanel extends AbstractProcessorAction {
    
    Color col,col2;

    public JComponent getComponent(Processor processor) {
	col = GraphColours.getColour(ProcessorHelper.getPreferredColour(processor),Color.WHITE);
	col2 = ShadedLabel.halfShade(col);
	JPanel rootPanel = new JPanel() {
		protected void paintComponent(Graphics g) {
		    final int width = getWidth();
		    final int height = getHeight();
		    Graphics2D g2d = (Graphics2D)g;
		    Paint oldPaint = g2d.getPaint();
		    g2d.setPaint(new GradientPaint(0,0,col,width,0,col2));
		    g2d.fillRect(0,0,width,height);
		    g2d.setPaint(oldPaint);
		    super.paintComponent(g);
		}
	    };
	rootPanel.setOpaque(false);
	rootPanel.setLayout(new BorderLayout());
	if (processor.isOffline()) {
	    // Can't fetch metadata when we're in offline mode
	    JEditorPane message = new JEditorPane("text/html","<html><head>"+WorkflowSummaryAsHTML.STYLE_NOBG+"</head><body><font color=\"red\">Offline mode</font><p>Taverna is currently working in offline mode, metadata must be fetched from the Soaplab server and is therefore unavailable in this mode.</body></html>");
	    message.setOpaque(false);
	    rootPanel.add(message, BorderLayout.CENTER);
	}
	else {
	    try {
		SoaplabProcessor sp = (SoaplabProcessor)processor;
		URL soaplabEndpoint = sp.getEndpoint();
		Call call = (Call) new Service().createCall();
		call.setTimeout(new Integer(0));
		call.setTargetEndpointAddress(soaplabEndpoint);
		call.setOperationName(new QName("describe"));
		String metadata = (String)call.invoke(new Object[0]);
		XMLTree tree = new XMLTree(metadata);
		tree.setOpaque(false);
		rootPanel.add(tree, BorderLayout.CENTER);
	    }
	    catch (Exception ex) {
		JEditorPane error = new JEditorPane("text/html", "<html><head>"+WorkflowSummaryAsHTML.STYLE_NOBG+"</head><body><font color=\"red\">Error</font><p>An exception occured while trying to fetch Soaplab metadata from the server. The error was :<pre>"+ex.getMessage()+"</pre></body></html>");
		ex.printStackTrace();
		error.setOpaque(false);
		rootPanel.add(error, BorderLayout.CENTER);
	    }
	}
	rootPanel.setPreferredSize(new Dimension(10,10));
	return rootPanel;
    }

    public boolean canHandle(Processor processor) {
	return (processor instanceof SoaplabProcessor);
    }

    public String getDescription() {
	return "Show soaplab metadata";
    }

    public ImageIcon getIcon() {
	return ProcessorHelper.getIconForTagName("soaplabwsdl");
    }

}
