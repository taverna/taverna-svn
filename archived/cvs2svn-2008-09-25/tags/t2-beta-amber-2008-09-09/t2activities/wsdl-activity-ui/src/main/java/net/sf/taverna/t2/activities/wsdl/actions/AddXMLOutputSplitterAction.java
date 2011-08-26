/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.wsdl.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.AddXMLSplitterEdit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.xml.sax.SAXException;

/**
 * Pops up a {@link JOptionPane} with the names of all the wsdl ports. The one
 * that is selected is added as an output splitter to the currently open
 * dataflow using the {@link AddXMLSplitterEdit}
 * 
 * @author Ian Dunlop
 * 
 */
public class AddXMLOutputSplitterAction extends AbstractAction {

	private WSDLActivity activity;
	private JComponent owner;
	private EditManager editManager = EditManager.getInstance();

	public AddXMLOutputSplitterAction(Activity<?> activity, JComponent owner) {
		this.activity = (WSDLActivity) activity;
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		WSDLParser parser = null;
		try {
			parser = new WSDLParser(activity.getConfiguration().getWsdl());
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (WSDLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SAXException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<TypeDescriptor> outputDescriptors = null;
		try {
			outputDescriptors = parser
					.getOperationOutputParameters(((WSDLActivityConfigurationBean) activity
							.getConfiguration()).getOperation());
		} catch (UnknownOperationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String[] possibilities = new String[outputDescriptors.size()];
		int i = 0;
		for (TypeDescriptor descriptor : outputDescriptors) {
			possibilities[i] = descriptor.getName();
			i++;
		}
		String s = (String) JOptionPane.showInputDialog(owner,
				"Select the port to add the splitter to",
				"Add output XML splitter", JOptionPane.PLAIN_MESSAGE, null,
				possibilities, possibilities[0]);

		Dataflow currentDataflow = FileManager.getInstance()
				.getCurrentDataflow();

		TypeDescriptor typeDescriptorForInputPort = null;
		try {
			typeDescriptorForInputPort = activity
					.getTypeDescriptorForInputPort(s);
		} catch (UnknownOperationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if (typeDescriptorForInputPort instanceof ArrayTypeDescriptor
				|| typeDescriptorForInputPort instanceof ComplexTypeDescriptor) {
			AddXMLSplitterEdit edit = new AddXMLSplitterEdit(currentDataflow,
					activity, s, false);
			try {
				editManager.doDataflowEdit(currentDataflow, edit);
			} catch (EditException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

}
