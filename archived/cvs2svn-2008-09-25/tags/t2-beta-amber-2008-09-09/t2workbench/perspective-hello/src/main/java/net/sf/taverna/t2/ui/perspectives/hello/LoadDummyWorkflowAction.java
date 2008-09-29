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
package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class LoadDummyWorkflowAction extends AbstractMenuAction {

	private static Logger logger = Logger
			.getLogger(LoadDummyWorkflowAction.class);

	private static final String DUMMY_WORKFLOW_T2FLOW = "dummy-workflow.t2flow";

	public LoadDummyWorkflowAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#file"),
				21);
	}

	@Override
	public Action createAction() {
		return new AbstractAction("Load example workflow") {
			public void actionPerformed(ActionEvent e) {
				try {
					loadWorkflow();
				} catch (Exception ex) {
					logger.warn("Could not load workflow", ex);
				}
			}

			private void loadWorkflow() throws IOException, JDOMException,
					DeserializationException, EditException {
				InputStream dummyWorkflowXMLstream = getClass()
						.getResourceAsStream(DUMMY_WORKFLOW_T2FLOW);
				XMLDeserializerImpl deserializer = new XMLDeserializerImpl();

				if (dummyWorkflowXMLstream == null) {
					throw new IOException("Unable to find resource for :"
							+ DUMMY_WORKFLOW_T2FLOW);
				}
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(dummyWorkflowXMLstream);
				Dataflow dataFlow = deserializer.deserializeDataflow(document
						.getRootElement());
				ModelMap.getInstance().setModel(ModelMapConstants.CURRENT_DATAFLOW, dataFlow);
				logger.info("Loaded workflow: " + dataFlow.getLocalName() + " "
						+ dataFlow.getInternalIdentier());
				JOptionPane.showMessageDialog(null, "Loaded example workflow");
			}
		};
	}
}
