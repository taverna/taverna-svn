/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: XMLSplitterScuflContextMenuFactory.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:37 $
 *               by   $Author: davidwithers $
 * Created on 22-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter;
import org.embl.ebi.escience.scuflworkers.java.XMLOutputSplitter;

/**
 * 
 * A factory that generates scufl context menu items for input and output ports
 * that are based upon an XML schema.
 * 
 * @author Stuart Owen
 *
 */

public class XMLSplitterScuflContextMenuFactory {

	private static Logger logger = Logger.getLogger(XMLSplitterScuflContextMenuFactory.class);

	private static XMLSplitterScuflContextMenuFactory instance = new XMLSplitterScuflContextMenuFactory();

	/**
	 * 
	 * @return an instance of the XMLSplitterScuflContextMenuFactory
	 */
	public static XMLSplitterScuflContextMenuFactory instance() {
		return instance;
	}

	/**
	 * Generates a List of JMenuItems, if appropriate, for the given port
	 * @param port
	 * @return
	 */
	public List<JMenuItem> contextItemsForPort(Port port) {
		if (port instanceof InputPort) {
			return contextItemsForInputPort((InputPort) port);
		} else if (port instanceof OutputPort) {
			return contextItemsForOutputPort((OutputPort) port);
		} else {
			return new ArrayList<JMenuItem>();
		}
	}

	private List<JMenuItem> contextItemsForInputPort(final InputPort inputPort) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();

		if (XMLInputSplitter.isSplittable(inputPort)) {

			final ScuflModel model = inputPort.getProcessor().getModel();

			JMenuItem xmlHelperItem = new JMenuItem("Add XML splitter");
			JMenuItem xmlHelperItemWithName = new JMenuItem("Add XML splitter with name");
			result.add(xmlHelperItem);
			result.add(xmlHelperItemWithName);
			ActionListener xmlHelperListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						XMLInputSplitter splitter = new XMLInputSplitter();
						String name = "";
						if (ae.getActionCommand().indexOf("name") != -1) {
							name = (String) JOptionPane.showInputDialog(null, "Name for the new processor?",
									"Name required", JOptionPane.QUESTION_MESSAGE, null, null, "");
							if (name != null) {
								name = model.getValidProcessorName(name);
							}
						} else {
							name = inputPort.getName() + "XML";
						}
						if (name != null) {
							splitter.setUpInputs(inputPort);
							LocalServiceProcessor processor = new LocalServiceProcessor(model, model
									.getValidProcessorName(name), splitter);
							model.addProcessor(processor);
							model
									.addDataConstraint(new DataConstraint(model, processor.getOutputPorts()[0],
											inputPort));
						}
					} catch (Exception e) {
						logger.error("Error adding XML input splitter", e);
					}
				}
			};
			xmlHelperItem.addActionListener(xmlHelperListener);
			xmlHelperItemWithName.addActionListener(xmlHelperListener);
		}
		return result;
	}

	private List<JMenuItem> contextItemsForOutputPort(final OutputPort outputPort) {
		List<JMenuItem> result = new ArrayList<JMenuItem>();

		if (XMLOutputSplitter.isSplittable(outputPort)) {

			final ScuflModel model = outputPort.getProcessor().getModel();

			JMenuItem xmlHelperItem = new JMenuItem("Add XML splitter");
			JMenuItem xmlHelperItemWithName = new JMenuItem("Add XML splitter with name");
			result.add(xmlHelperItem);
			result.add(xmlHelperItemWithName);
			ActionListener xmlHelperListener = new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						XMLOutputSplitter splitter = new XMLOutputSplitter();
						if (splitter.doesTypeContainCyclicReferences(outputPort)) {
							if (JOptionPane
									.showConfirmDialog(
											null,
											"This data structure contains cyclic references which may result in failure when the workflow is run. Continue?",
											"Cyclic References", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
								return;
							}
						}
						String name = "";
						if (ae.getActionCommand().indexOf("name") != -1) {
							name = (String) JOptionPane.showInputDialog(null, "Name for the new processor?",
									"Name required", JOptionPane.QUESTION_MESSAGE, null, null, "");
							if (name != null) {
								name = model.getValidProcessorName(name);
							}
						} else {
							name = outputPort.getName() + "XML";
						}
						if (name != null) {
							splitter.setUpOutputs(outputPort);
							LocalServiceProcessor processor = new LocalServiceProcessor(model, model
									.getValidProcessorName(name), splitter);
							model.addProcessor(processor);
							model
									.addDataConstraint(new DataConstraint(model, outputPort,
											processor.getInputPorts()[0]));
						}
					} catch (Exception e) {
						logger.error("Error adding XML Output splitter", e);
					}
				}
			};
			xmlHelperItem.addActionListener(xmlHelperListener);
			xmlHelperItemWithName.addActionListener(xmlHelperListener);
		}
		return result;
	}

}
