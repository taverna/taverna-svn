package net.sf.taverna.t2.util.tools;

import java.io.IOException;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Return a complete Dataflow or the constituent parts as an XML formatted
 * string, mainly for use with the Provenance system
 * 
 * @author Ian Dunlop
 * 
 */
public class DataflowSerialiser {

	private Dataflow dataflow;

	public DataflowSerialiser(Dataflow dataflow) {
		this.dataflow = dataflow;
	}

	public String serialiseWorkflow() {

		String serialiseProcessor = serialiseProcessor(dataflow.getProcessors());
		String serialiseInputPorts = serialiseInputPorts(dataflow
				.getInputPorts());
		String serialiseOutpustPorts = serialiseOutputPorts(dataflow
				.getOutputPorts());
		String serialiseLinks = serialiseLinks(dataflow.getLinks());

		return "<dataflow>\n" + "<name>" + dataflow.getLocalName() + "</name>\n" + serialiseProcessor + serialiseInputPorts
				+ serialiseOutpustPorts + serialiseLinks + "</dataflow>\n";

	}

	/**
	 * Gets the sink and source links for the dataflow and check if they are
	 * nested dataflows (processors?)
	 * 
	 * @param links
	 * @return
	 */
	private String serialiseLinks(List<? extends Datalink> links) {
		String datalinks = "<datalinks>\n";
		for (Datalink link : links) {
			datalinks = datalinks + "<datalink>\n";
			if (link.getSink() instanceof ProcessorPort) {
				datalinks = datalinks
						+ "<sink>\n<processor>"
						+ ((ProcessorPort) link.getSink()).getProcessor()
								.getLocalName() + "</processor>\n";
				datalinks = datalinks + "<inputport>" + link.getSink().getName()
						+ "</inputport>\n</sink>\n";
			} else {
				datalinks = datalinks + "<sink>\n<inputport>"
						+ link.getSink().getName() + "</inputport>\n</sink>\n";
			}
			if (link.getSource() instanceof ProcessorPort) {
				
				datalinks = datalinks
						+ "<source>\n<processor>"
						+ ((ProcessorPort) link.getSource()).getProcessor()
								.getLocalName() + "</processor>\n";
				datalinks = datalinks + "<outputport>" + link.getSource().getName()
						+ "</outputport>\n</source>\n";
			} else {
				datalinks = datalinks + "<source>\n<outputport>"
						+ link.getSource().getName() + "</outputport>\n</source>\n";
			}
			datalinks = datalinks + "</datalink>\n";
		}
		datalinks = datalinks + "</datalinks>\n";

		return datalinks;
	}

	private String serialiseOutputPorts(
			List<? extends DataflowOutputPort> outputPorts) {
		String ports = "<outputPorts>\n";
		for (DataflowOutputPort outputPort : outputPorts) {
			ports = ports + "<port>" + outputPort.getName() + "</port>\n";
		}
		ports = ports + "</outputPorts>";

		return ports;
	}

	private String serialiseInputPorts(
			List<? extends DataflowInputPort> inputPorts) {
		String ports = "<inputPorts>\n";
		for (DataflowInputPort inputPort : inputPorts) {
			ports = ports + "<port>" + inputPort.getName() + "</port>\n";
		}
		ports = ports + "</inputPorts>\n";

		return ports;
	}

	private String serialiseProcessor(List<? extends Processor> processors) {
		String processorString = "<processors>\n";
		for (Processor processor : processors) {

			processorString = processorString + "<processor name=\""
					+ processor.getLocalName() + "\">\n";
			List<? extends Activity<?>> activityList = processor
					.getActivityList();
			processorString = processorString + "<activities>\n";
			for (Activity activity : activityList) {
				// processorString = processorString + "<activity>\n";
				Element activityAsXML = null;
				try {
					activityAsXML = Tools.activityAsXML(activity);
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				XMLOutputter outputter = new XMLOutputter();
				String outputString = outputter.outputString(activityAsXML);
				processorString = processorString + outputString;
				// + "</activity>\n";
			}
			processorString = processorString + "\n</activities>\n";

			processorString = processorString + "</processor>\n";
		}
		processorString = processorString + "</processors>\n";

		return processorString;
	}

}
