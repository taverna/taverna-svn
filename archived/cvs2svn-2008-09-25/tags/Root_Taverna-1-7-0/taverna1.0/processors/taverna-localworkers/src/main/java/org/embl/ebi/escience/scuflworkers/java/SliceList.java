/**
 * 
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.XScufl;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Return a sublist of a list.
 * 
 * <p>
 * The depth can be set with {@link #setDepth(int)} - as done through the
 * {@link SliceListEditAction}.
 * 
 * @author Stian Soiland
 */
public class SliceList implements LocalWorkerWithPorts, XMLExtensible {

	// XML tags/attributes
	private static final String EXTENSIONS = "extensions";

	private static final String SLICELIST = "slicelist";

	private static final String DEPTH = "depth";

	// Syntactic type
	private static final String LIST_TYPE = LocalWorker.UNTYPED_ARRAY;

	// Port names
	private static final String OUTPUTLIST = "outputlist";

	private static final String INPUTLIST = "inputlist";
	
	private static final String FROMINDEX = "fromindex";
	
	private static final String TOINDEX = "toindex";

	private static Logger logger = Logger.getLogger(FlattenList.class);

	// Minimum 1, no point in not flattening (would copy)
	private int depth = 1;

	/**
	 * Set the depth of this slicelist. The depth means how deeply it should
	 * examine the input before commencing slicing the list. The minimum (and default) depth is 1, which means to sublist
	 * lists that contain elements. A depth of 2 would slice lists of lists of elements.
	 * <p>
	 * If a depth less than 1 is given, a warning is logged and the depth is set
	 * to 1 instead.
	 * 
	 * @param depth
	 *            Depth of slicelist, minimum 2
	 */
	public void setDepth(int depth) {
		if (depth < 1) {
			logger.warn("Ignoring invalid depth " + depth
					+ ", setting 1 instead");
			depth = 1;
		}
		this.depth = depth;
	}

	/**
	 * Get the depth for this slicelist. The default value is 1.
	 * 
	 * @return The current depth
	 */
	public int getDepth() {
		return depth;
	}

	// Legacy methods that should not really be called on a LocalWorkerWithPorts
	public String[] inputNames() {
		logger
				.error("Not supposed to call inputNames() on LocalWorkerWithPorts instance");
		return new String[] { INPUTLIST, FROMINDEX, TOINDEX};
	}

	public String[] inputTypes() {
		logger
				.error("Not supposed to call inputTypes() on LocalWorkerWithPorts instance");
		return new String[] { getSyntacticType() , LocalWorker.STRING, LocalWorker.STRING};
	}

	public String[] outputNames() {
		logger
				.error("Not supposed to call outputNames() on LocalWorkerWithPorts instance");
		return new String[] { OUTPUTLIST };
	}

	public String[] outputTypes() {
		logger
				.error("Not supposed to call outputTypes() on LocalWorkerWithPorts instance");
		return new String[] { LIST_TYPE };
	}

	/**
	 * Copy each entry in the input list into the output list iff it matches the
	 * supplied regular expression.
	 */
	public Map<String, DataThing> execute(Map inputs)
			throws TaskExecutionException {
		List inputList = (List) ((DataThing) (inputs.get(INPUTLIST)))
				.getDataObject();
		String fromIndexString =
			(String) ((DataThing) (inputs.get(FROMINDEX))).getDataObject();
		int fromIndex;
		String toIndexString =
			(String) ((DataThing) (inputs.get(TOINDEX))).getDataObject();
		int toIndex;
		try {
			fromIndex = Integer.parseInt(fromIndexString);
		}
		catch (NumberFormatException e) {
			throw new TaskExecutionException (e);
		}
		try {
			toIndex = Integer.parseInt(toIndexString);
		}
		catch (NumberFormatException e) {
			throw new TaskExecutionException (e);
		}
		List<Object> outputList;
		
		try {
			outputList = inputList.subList(fromIndex, toIndex);
		}
		catch (IndexOutOfBoundsException e) {
			throw new TaskExecutionException (e);
		}
		catch (IllegalArgumentException e) {
			throw new TaskExecutionException (e);
		}
		Map<String, DataThing> outputs = new HashMap<String, DataThing>();
		outputs.put(OUTPUTLIST, new DataThing(outputList));
		return outputs;
	}

	public List<InputPort> inputPorts(LocalServiceProcessor processor)
			throws DuplicatePortNameException, PortCreationException {
		InputPort port = new InputPort(processor, INPUTLIST);
		port.setSyntacticType(getSyntacticType());
		List<InputPort> ports = new ArrayList<InputPort>();
		ports.add(port);
		InputPort fromIndexPort = new InputPort(processor, FROMINDEX);
		fromIndexPort.setSyntacticType(LocalWorker.STRING);
		ports.add(fromIndexPort);
		InputPort toIndexPort = new InputPort(processor, TOINDEX);
		toIndexPort.setSyntacticType(LocalWorker.STRING);
		ports.add(toIndexPort);
		return ports;
	}

	public List<OutputPort> outputPorts(LocalServiceProcessor processor)
			throws DuplicatePortNameException, PortCreationException {
		OutputPort port = new OutputPort(processor, OUTPUTLIST);
		port.setSyntacticType(getSyntacticType());
		List<OutputPort> ports = new ArrayList<OutputPort>();
		ports.add(port);
		return ports;
	}

	/**
	 * Extract the &lt;slicelist depth="X" /&gt; number from the <extensions>
	 * element.
	 */
	public void consumeXML(Element element) {
		Element sliceList = element.getChild(SLICELIST, XScufl.XScuflNS);
		if (sliceList == null) {
			return;
		}
		Attribute depth = sliceList.getAttribute(DEPTH, XScufl.XScuflNS);
		if (depth == null) {
			return;
		}
		try {
			setDepth(depth.getIntValue());
		} catch (DataConversionException ex) {
			logger.warn("Invalid depth: " + depth.getValue());
		}
	}

	/**
	 * Export depth as &lt;extensions&gt;&lt;flattenlist depth="X"
	 * /&gt;&lt;/extensions&gt;
	 */
	public Element provideXML() {
		Element extensions = new Element(EXTENSIONS, XScufl.XScuflNS);
		Element sliceList = new Element(SLICELIST, XScufl.XScuflNS);
		sliceList.setAttribute(DEPTH, Integer.toString(getDepth()),
				XScufl.XScuflNS);
		extensions.addContent(sliceList);
		return extensions;
	}

	/**
	 * Builds the syntactic type depending on the current depth.
	 * <p>
	 * For instance, a current depth of 3 would return:
	 * 
	 * <pre>
	 * l(l(l('')))
	 * </pre>
	 * 
	 * Note that the processor ignore the 'real' mime-type (the data is just
	 * passed on), and thus uses '' as the innermost type.
	 * @return
	 */
	private String getSyntacticType() {
		String syntacticType = LocalWorker.UNTYPED;
		for (int i = 0; i < getDepth(); i++) {
			syntacticType = "l(" + syntacticType + ")";
		}
		return syntacticType;
	}

}