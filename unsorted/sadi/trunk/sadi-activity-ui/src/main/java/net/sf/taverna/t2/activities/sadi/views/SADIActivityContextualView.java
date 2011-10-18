/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sadi.views;

import java.awt.Frame;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;

import net.sf.taverna.t2.activities.sadi.SADIActivity;
import net.sf.taverna.t2.activities.sadi.SADIActivityConfigurationBean;
import net.sf.taverna.t2.activities.sadi.SADIActivityPort;
import net.sf.taverna.t2.activities.sadi.actions.SADIActivityConfigurationAction;
import net.sf.taverna.t2.activities.sadi.views.SADIHtmlPanel.Table;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.rdfpath.RDFPath;
import ca.wilkinsonlab.sadi.utils.LabelUtils;

import com.hp.hpl.jena.rdf.model.Property;

public class SADIActivityContextualView extends ActivityContextualView<SADIActivityConfigurationBean> {

	private static final long serialVersionUID = 1L;
	
	private SADIHtmlPanel mainFrame;

	public SADIActivityContextualView(SADIActivity activity) {
		super(activity);
	}

	public SADIActivity getActivity() {
		return (SADIActivity) super.getActivity();
	}

	private void buildHtmlTables() {
		Table table = mainFrame.createTable();
		String type = "details";
		Service service = getActivity().getService();
		table.addProperty("Name", service.getName(), type);
		String description = service.getDescription();
		if (!"".equals(description)) {
			table.addProperty("Description", description, type);
		}
		table.addProperty("Location", getConfigBean().getServiceURI(), type);
		table.addProperty("Registry", getConfigBean().getGraphName(), type);
		table = mainFrame.createTable();
		table.addSection("Service Inputs");
		Iterator<ActivityInputPort> iterator = getActivity().getInputPorts().iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			SADIActivityPort sadiPort = (SADIActivityPort) iterator.next();
			addPortInfo(table, sadiPort, i % 2 == 0 ? "even" : "odd");
		}

		table.addSection("Service Outputs");
		Iterator<OutputPort> iterator2 = getActivity().getOutputPorts().iterator();
		for (int i = 0; iterator2.hasNext(); i++) {
			SADIActivityPort sadiPort = (SADIActivityPort) iterator2.next();
			type = i % 2 == 0 ? "even" : "odd";
			addPortInfo(table, sadiPort, type);
		}
	}

	protected static void addPortInfo(Table table, SADIActivityPort sadiPort, String type) {
		table.addProperty("Name", sadiPort.getName(), type);
		RDFPath path = sadiPort.getRDFPath();
		if (!path.isEmpty()) {
			Property p = path.getLastPathElement().getProperty();
			table.addProperty("Property", LabelUtils.getLabel(p), p.getURI(), type);
		}
		table.addProperty("Type", sadiPort.getValuesFromLabel(), sadiPort.getValuesFromURI(), type);
		table.addProperty("Depth", String.valueOf(sadiPort.getDepth()), type);
	}

	@Override
	public String getViewTitle() {
		return "SADI service";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new SADIActivityConfigurationAction((SADIActivity) getActivity(), owner);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

	@Override
	public void refreshView() {
		mainFrame.clearTables();
		buildHtmlTables();
		mainFrame.update();
	}

	@Override
	public JComponent getMainFrame() {
		if (mainFrame == null) {
			mainFrame = new SADIHtmlPanel();
			refreshView();
		}
		return mainFrame;
	}

	public static void main(String[] args) throws ActivityConfigurationException {
		final JFrame frame = new JFrame();
		SADIActivity sadiActivity = new SADIActivity();
		SADIActivityConfigurationBean configurationBean = new SADIActivityConfigurationBean();
		configurationBean.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean.setGraphName("http://sadiframework.org/registry/");
//		configurationBean.setServiceURI("http://sadiframework.org/services/getGOTerm");
		configurationBean.setServiceURI("http://sadiframework.org/examples/linear");
//		configurationBean.setServiceURI("http://sadiframework.org/examples/ermineJgo");
		sadiActivity.configure(configurationBean);

		SADIActivityContextualView contextView = new SADIActivityContextualView(sadiActivity);
		frame.add(contextView.getMainFrame());
		frame.pack();
		frame.setVisible(true);		
	}
}
