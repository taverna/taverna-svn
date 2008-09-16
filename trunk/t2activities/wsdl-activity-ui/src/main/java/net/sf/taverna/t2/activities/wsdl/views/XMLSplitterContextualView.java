/*******************************************************************************
 * Copyright (C) 2007-2008 The University of Manchester   
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
package net.sf.taverna.t2.activities.wsdl.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdl.OutputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLInputSplitterAction;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLOutputSplitterAction;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class XMLSplitterContextualView extends
		HTMLBasedActivityContextualView<XMLSplitterConfigurationBean> {

	private static final long serialVersionUID = -4329643934083676113L;

	public XMLSplitterContextualView(Activity<XMLSplitterConfigurationBean> activity) {
		super(activity);
	}

	/**
	 * Gets the component from the {@link HTMLBasedActivityContextualView} and
	 * adds buttons to it allowing XML splitters to be added
	 */
	@Override
	protected JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();
		JPanel flowPanel = new JPanel(new FlowLayout());
		
		if (getActivity() instanceof InputPortTypeDescriptorActivity) {
			AddXMLInputSplitterAction inputSplitterAction = new AddXMLInputSplitterAction(
					(InputPortTypeDescriptorActivity)getActivity(), mainFrame);
			flowPanel.add(new JButton(inputSplitterAction));
		} 
		if (getActivity() instanceof OutputPortTypeDescriptorActivity) {
			AddXMLOutputSplitterAction outputSplitterAction = new AddXMLOutputSplitterAction(
					(OutputPortTypeDescriptorActivity)getActivity(), mainFrame);
			flowPanel.add(new JButton(outputSplitterAction));
		}
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	@Override
	protected String getViewTitle() {
		return "XML splitter";
	}

	@Override
	protected String getRawTableRowsHtml() {
		return "";
	}


}
