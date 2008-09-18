package net.sf.taverna.t2.activities.wsdl.views;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdl.OutputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdl.actions.AbstractAddXMLSplitterAction;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLInputSplitterAction;
import net.sf.taverna.t2.activities.wsdl.actions.AddXMLOutputSplitterAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;

import org.apache.log4j.Logger;

public abstract class AbstractXMLSplitterActionView<BeanType> extends
		HTMLBasedActivityContextualView<BeanType> {

	private static Logger logger = Logger
			.getLogger(AbstractXMLSplitterActionView.class);

	public AbstractXMLSplitterActionView(Activity<?> activity) {
		super(activity);
	}

	protected void addOutputSplitter(final JComponent mainFrame,
			JPanel flowPanel) {
		if (getActivity() instanceof OutputPortTypeDescriptorActivity) {
			Map<String, TypeDescriptor> descriptors;
			try {
				descriptors = ((OutputPortTypeDescriptorActivity) getActivity())
						.getTypeDescriptorsForOutputPorts();
				if (!AbstractAddXMLSplitterAction
						.filterDescriptors(descriptors).isEmpty()) {
					AddXMLOutputSplitterAction outputSplitterAction = new AddXMLOutputSplitterAction(
							(OutputPortTypeDescriptorActivity) getActivity(),
							mainFrame);
					flowPanel.add(new JButton(outputSplitterAction));
				}
			} catch (UnknownOperationException e) {
				logger.warn("Could not find operation for " + getActivity(), e);
			} catch (IOException e) {
				logger
						.warn("Could not read definition for " + getActivity(),
								e);
			}
		}
	}

	protected void addInputSplitter(final JComponent mainFrame, JPanel flowPanel) {
		if (getActivity() instanceof InputPortTypeDescriptorActivity) {
			Map<String, TypeDescriptor> descriptors;
			try {
				descriptors = ((InputPortTypeDescriptorActivity) getActivity())
						.getTypeDescriptorsForInputPorts();
				if (!AbstractAddXMLSplitterAction
						.filterDescriptors(descriptors).isEmpty()) {
					AddXMLInputSplitterAction inputSplitterAction = new AddXMLInputSplitterAction(
							(InputPortTypeDescriptorActivity) getActivity(),
							mainFrame);
					flowPanel.add(new JButton(inputSplitterAction));
				}
			} catch (UnknownOperationException e) {
				logger.warn("Could not find operation for " + getActivity(), e);
			} catch (IOException e) {
				logger
						.warn("Could not read definition for " + getActivity(),
								e);
			}
		}
	}

	protected String describePorts() {
		StringBuilder html = new StringBuilder();

		if (getActivity() instanceof InputPortTypeDescriptorActivity) {
			Map<String, TypeDescriptor> descriptors = null;
			try {
				descriptors = ((InputPortTypeDescriptorActivity) getActivity())
						.getTypeDescriptorsForInputPorts();
			} catch (UnknownOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (descriptors != null && ! descriptors.isEmpty()) {
				html.append("<tr><th colspan='2' align='left'>Inputs</th></tr>");
				descriptorsHtml(html, descriptors);
			}
		}
		if (getActivity() instanceof OutputPortTypeDescriptorActivity) {
			Map<String, TypeDescriptor> descriptors = null;
			try {
				descriptors = ((OutputPortTypeDescriptorActivity) getActivity())
						.getTypeDescriptorsForOutputPorts();
			} catch (UnknownOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (descriptors != null && ! descriptors.isEmpty()) {
				html.append("<tr><th colspan='2' align='left'>Outputs</th></tr>");
				descriptorsHtml(html, descriptors);
			}
		}

		return html.toString();
	}

	protected void descriptorsHtml(StringBuilder html,
			Map<String, TypeDescriptor> descriptors) {
		for (Entry<String, TypeDescriptor> entry : descriptors.entrySet()) {
			TypeDescriptor typeDescriptor = entry.getValue();
			html.append("<tr><tr>").append(entry.getKey());
			html.append("</td></td>");
			if (!typeDescriptor.getName().equals(entry.getKey())) {
				html.append("Original name: <code>");
				html.append(typeDescriptor);
				html.append("</code><br>");
			}
			// html.append(typeDescriptor.getName());
			if (typeDescriptor.isOptional()) {
				html.append("<em>optional</em><br>");
			}
			html.append("Depth: ");
			html.append(typeDescriptor.getDepth());
			html.append("<br>");

			html.append("<code>");
			html.append(typeDescriptor.getQname());
			html.append("</code><br>");
			html.append("</td></tr>");
		}
	}

}