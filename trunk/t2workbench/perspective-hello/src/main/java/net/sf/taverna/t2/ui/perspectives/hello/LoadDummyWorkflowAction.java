package net.sf.taverna.t2.ui.perspectives.hello;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
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
	public Action getAction() {
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
				BeanshellActivityConfigurationBean b = new BeanshellActivityConfigurationBean();
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
				ModelMap.getInstance().setModel("currentDataflow", dataFlow);
				logger.info("Loaded workflow: " + dataFlow.getLocalName() + " "
						+ dataFlow.getInternalIdentier());
				JOptionPane.showMessageDialog(null, "Loaded example workflow");
			}
		};
	}
}
