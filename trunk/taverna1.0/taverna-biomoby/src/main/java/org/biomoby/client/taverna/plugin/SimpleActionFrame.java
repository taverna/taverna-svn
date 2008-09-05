/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

/**
 * This class contains some methods that are useful in creating a consistent JPanel
 * for displaying information or actions for biomoby services and datatypes.
 * 
 * @author Edward Kawas
 * 
 */
public class SimpleActionFrame extends JPanel implements UIComponentSPI {

		private static final long serialVersionUID = -6611234116434482238L;
		
		private AbstractProcessorAction action = null;

		private String name = "";
		public SimpleActionFrame(Component c, AbstractProcessorAction a, String name) {
			super(new BorderLayout());
			add(c, BorderLayout.CENTER);
			this.action = a;
			this.name = name;
		}

		public ImageIcon getIcon() {
			return action.getIcon();
		}

		public String getName() {
			return name;
		}

		public void onDisplay() {
			
		}

		public void onDispose() {
			action.frameClosing();
		}
	}