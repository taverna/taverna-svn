/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import java.awt.Color;
/**
 * Contains utility methods to deal with opening windows
 * and suchlike in a way that makes no assumptions about
 * the existance of a JDesktop pane.
 * @author Tom Oinn
 */
public class UIUtils {
    
    public static FrameCreator DEFAULT_FRAME_CREATOR = new FrameCreator() {
	    public void createFrame(ScuflModel targetModel, ScuflUIComponent targetComponent, int posX, int posY, int sizeX, int sizeY) {
		final ScuflUIComponent component = targetComponent;
		final ScuflModel model = targetModel;
		JFrame newFrame = new JFrame(component.getName());
		newFrame.getContentPane().setLayout(new BorderLayout());
		newFrame.getContentPane().add(new JScrollPane((JComponent)targetComponent),
					    BorderLayout.CENTER);
		newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
			    component.detachFromModel();
			}
		    });
		if (component.getIcon() != null) {
		    newFrame.setIconImage(component.getIcon().getImage());
		}
		component.attachToModel(model);
		newFrame.setSize(sizeX, sizeY);
		newFrame.setLocation(posX, posY);
		newFrame.setVisible(true);
	    }
	};

    public interface FrameCreator {
	public void createFrame(ScuflModel targetModel, ScuflUIComponent targetComponent, int posX, int posY, int sizeX, int sizeY);
    }
    
    public static void createFrame(ScuflModel targetModel, ScuflUIComponent targetComponent, int posX, int posY, int sizeX, int sizeY) {
	DEFAULT_FRAME_CREATOR.createFrame(targetModel, targetComponent, posX, posY, sizeX, sizeY);
    }
    
}
