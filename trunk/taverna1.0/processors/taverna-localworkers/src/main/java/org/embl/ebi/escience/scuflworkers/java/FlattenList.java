/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scuflworkers.java.actions.FlattenListEditAction;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Consume a list of lists and emit a list containing the given level of
 * flattening of the input. By default (and historical reasons) the level is 2,
 * meaning that the output list will be 1 level flatter.
 * <p>
 * The level can be set with {@link #setDepth(int)} - as done through the
 * {@link FlattenListEditAction}.
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class FlattenList implements LocalWorkerWithPorts, XMLExtensible {

	// XML tags/attributes
	private static final String EXTENSIONS = "extensions";

	private static final String FLATTENLIST = "flattenlist";

	private static final String DEPTH = "depth";

	// Syntactic type
	private static final String LIST_TYPE = "l('')";

	// Port names
	private static final String OUTPUTLIST = "outputlist";

	private static final String INPUTLIST = "inputlist";

	private static Logger logger = Logger.getLogger(FlattenList.class);

	// Minimum 2, no point in not flattening (would copy)
	private int depth = 2;

	/**
	 * Set the depth of this flattener. The depth means how deeply it should
	 * flatten lists. The minimum (and default) depth is 2, which means to strip
	 * away the outermost list. A depth of 3 would strip away the two outermost
	 * lists, etc.
	 * <p>
	 * If a depth less than 2 is given, a warning is logged and the depth is set
	 * to 2 instead.
	 * 
	 * @param depth
	 *            Depth of flattener, minimum 2
	 */
	public void setDepth(int depth) {
		if (depth < 2) {
			logger.warn("Ignoring invalid depth " + depth
				+ ", setting 2 instead");
			depth = 2;
		}
		this.depth = depth;
	}

	/**
	 * Get the depth of this flattener. The default value is 2.
	 * 
	 * @return The current depth
	 */
	public int getDepth() {
		return depth;
	}

	// Legacy methods that should not really be called on a LocalWorkerWithPorts
	public String[] inputNames() {
		logger.error("Not supposed to call inputNames() on LocalWorkerWithPorts instance");
		return new String[] { INPUTLIST };
	}

	public String[] inputTypes() {
		logger.error("Not supposed to call inputTypes() on LocalWorkerWithPorts instance");
		return new String[] { getSyntacticType() };
	}

	public String[] outputNames() {
		logger.error("Not supposed to call outputNames() on LocalWorkerWithPorts instance");
		return new String[] { OUTPUTLIST };
	}

	public String[] outputTypes() {
		logger.error("Not supposed to call outputTypes() on LocalWorkerWithPorts instance");
		return new String[] { LIST_TYPE };
	}

	/**
	 * Copy each entry in the input list into the output list iff it matches the
	 * supplied regular expression.
	 */
	public Map<String, DataThing> execute(Map inputs)
		throws TaskExecutionException {
		List inputList =
			(List) ((DataThing) (inputs.get(INPUTLIST))).getDataObject();
		List<Object> outputList = new ArrayList<Object>();
		flatten(inputList, outputList, depth - 1);
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
		return ports;
	}

	public List<OutputPort> outputPorts(LocalServiceProcessor processor)
		throws DuplicatePortNameException, PortCreationException {
		OutputPort port = new OutputPort(processor, OUTPUTLIST);
		port.setSyntacticType(LIST_TYPE);
		SemanticMarkup metaData = port.getMetadata();
		// Is this useful?
		metaData.addMIMEType("");
		List<OutputPort> ports = new ArrayList<OutputPort>();
		ports.add(port);
		return ports;
	}

	/**
	 * Extract the &lt;flattenlist depth="X" /&gt; number from the <extensions>
	 * element.
	 */
	public void consumeXML(Element element) {
		Element flattenList = element.getChild(FLATTENLIST, XScufl.XScuflNS);
		if (flattenList == null) {
			return;
		}
		Attribute depth = flattenList.getAttribute(DEPTH, XScufl.XScuflNS);
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
		Element flattenList = new Element(FLATTENLIST, XScufl.XScuflNS);
		flattenList.setAttribute(DEPTH, Integer.toString(getDepth()),
			XScufl.XScuflNS);
		extensions.addContent(flattenList);
		return extensions;
	}

	/**
	 * Flatten collection until depth is 0 by adding elements (in depth-first
	 * search order) to results.
	 * <p>
	 * For each element of the collection, if the element is a collection (and
	 * we have not yet reached depth==0), the children of that collection will
	 * be either flattened themselves into results, or added directly to results
	 * (if the depth is 0).
	 * <p>
	 * That means that <code>flatten([[0,1],2], r, 1)</code> will extend r
	 * with <code>[0,1,2]</code>, while
	 * <code>flatten([[[0,1]],[2,3], 4], r, 1)</code> will extend r with
	 * <code>[ [0,1], 2, 3, 4]</code> (thus not flattening the deep
	 * [0,1]-list, to achieve that call with depth=2 or higher)
	 * 
	 * @param collection
	 *            Collection to be iterate over
	 * @param results
	 *            Where the flattened elements are added
	 * @param depth
	 *            The maximum depth to flatten
	 */
	private void flatten(Collection collection, List<Object> results, int depth) {
		for (Object o : collection) {
			if (o instanceof Collection && depth > 0) {
				flatten((Collection) o, results, depth - 1);
			} else {
				results.add(o);
			}
		}
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
	 * 
	 * @return
	 */
	private String getSyntacticType() {
		String syntacticType = "''";
		for (int i = 0; i < getDepth(); i++) {
			syntacticType = "l(" + syntacticType + ")";
		}
		return syntacticType;
	}

}
