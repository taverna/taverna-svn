/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A task to parse a Moby Datatype
 * 
 * @author Eddie Kawas
 */
public class MobyParseDatatypeTask implements ProcessorTaskWorker {
	private static Logger logger = Logger.getLogger(MobyParseDatatypeTask.class);

	private MobyParseDatatypeProcessor proc;

	/**
	 * 
	 * @param p
	 *            the processor that this parser is based upon
	 */
	public MobyParseDatatypeTask(Processor p) {
		this.proc = (MobyParseDatatypeProcessor) p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker#execute(java.util.Map,
	 *      uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask)
	 */
	public Map execute(java.util.Map workflowInputMap, IProcessorTask parentTask)
			throws TaskExecutionException {

		HashMap<String, DataThing> output = new HashMap<String, DataThing>();
		try {

			String inputMapKey = proc.getInputPorts()[0].getName();
			// inputMap wasnt as expected
			if (!workflowInputMap.containsKey(inputMapKey))
				return new HashMap();

			DataThing input = (DataThing) workflowInputMap.get(inputMapKey);
			
			if (input.getDataObject() instanceof String) {
				//logger.error(inputMapKey + " is a string!\n");
				String inputXML = (String) input.getDataObject();
				for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
					OutputPort outPort = proc.getBoundOutputPorts()[x];
					String outputPortName = outPort.getName();
					String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(inputXML);
					ArrayList<String> names = new ArrayList<String>();
					int type = 0;		
					// get the type, names list, etc
					if (outputPortName.equalsIgnoreCase("namespace")) {
						// extract the namespace from the top element
						names.add(proc.getArticleNameUsedByService());
						type = ParseMobyXML.NAMESPACE;
					} else if (outputPortName.equalsIgnoreCase("id")) {
						// extract the id from the top element
						names.add(proc.getArticleNameUsedByService());
						type = ParseMobyXML.ID;
					} else {
						names = getNames(outputPortName);
						if (outputPortName.endsWith("_ns")) {
							type = ParseMobyXML.NAMESPACE;
						} else if (outputPortName.endsWith("_id")) {
							type = ParseMobyXML.ID;
						} else {
							type = ParseMobyXML.VALUE;
						}
					}
					ArrayList<String> stuff = new ArrayList<String>();
					for (int i = 0; i < invocations.length; i++) {
						String invocation = invocations[i];
						if (XMLUtilities.isCollection(invocation)) {
							String[] simples = XMLUtilities.getAllSimplesByArticleName(proc.getArticleNameUsedByService(), invocation);
							for (int j = 0; j < simples.length; j++) {
								ArrayList<String> content = ParseMobyXML.getContentForDataType(names, type, XMLUtilities.createMobyDataElementWrapper(simples[j],"a1", null));
								stuff.addAll(content);
							}
						} else {
							ArrayList<String> content = ParseMobyXML.getContentForDataType(names, type, invocations[i]);
							stuff.addAll(content);
						}
					}
					output.put(outputPortName, new DataThing(stuff));
				}

			} else if (input.getDataObject() instanceof List) {
				//logger.error(inputMapKey + " is a list!\n");
				List<String> list = (List) input.getDataObject();
				// holder contains a list of strings indexed by output port name
				// TODO put stuff in the map and in the end put it in the output map
				HashMap<String, ArrayList<String>> holder = new HashMap<String, ArrayList<String>>();
				for (Iterator<String> it = list.iterator(); it.hasNext();) {
					String inputXML = (String) it.next();
					for (int x = 0; x < proc.getBoundOutputPorts().length; x++) {
						OutputPort outPort = proc.getBoundOutputPorts()[x];
						String outputPortName = outPort.getName();
						String[] invocations = XMLUtilities.getSingleInvokationsFromMultipleInvokations(inputXML);
						ArrayList<String> names = new ArrayList<String>();
						int type = 0;		
						// get the type, names list, etc
						if (outputPortName.equalsIgnoreCase("namespace")) {
							// extract the namespace from the top element
							names.add(proc.getArticleNameUsedByService());
							type = ParseMobyXML.NAMESPACE;
						} else if (outputPortName.equalsIgnoreCase("id")) {
							// extract the id from the top element
							names.add(proc.getArticleNameUsedByService());
							type = ParseMobyXML.ID;
						} else {
							names = getNames(outputPortName);
							if (outputPortName.endsWith("_ns")) {
								type = ParseMobyXML.NAMESPACE;
							} else if (outputPortName.endsWith("_id")) {
								type = ParseMobyXML.ID;
							} else {
								type = ParseMobyXML.VALUE;
							}
						}
						ArrayList<String> stuff = new ArrayList<String>();
						for (int i = 0; i < invocations.length; i++) {
							String invocation = invocations[i];
							if (XMLUtilities.isCollection(invocation)) {
								String[] simples = XMLUtilities.getAllSimplesByArticleName(proc.getArticleNameUsedByService(), invocation);
								for (int j = 0; j < simples.length; j++) {
									ArrayList<String> content = ParseMobyXML.getContentForDataType(names, type, XMLUtilities.createMobyDataElementWrapper(simples[j],"a1", null));
									stuff.addAll(content);
								}
							} else {
								ArrayList<String> content = ParseMobyXML.getContentForDataType(names, type, invocations[i]);
								stuff.addAll(content);
							}
						}
						if (holder.containsKey(outputPortName)) {
							ArrayList<String> al = holder.get(outputPortName);
							al.addAll(stuff);
							holder.put(outputPortName, al);
						} else {
							holder.put(outputPortName, stuff);
						}
					}
				}
				// fill output map
				for (Iterator<String> it = holder.keySet().iterator(); it.hasNext();) {
					String key = it.next();
					output.put(key, new DataThing(holder.get(key)));
				}
			}
				
				
			return output;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new TaskExecutionException("Error parsing moby data: " + ex.getMessage());
		}
	}
	
	private ArrayList<String> getNames(String names) {
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> temp = new ArrayList<String>();
		if (names == null || names.trim().length() == 0)
			return list;
		Scanner s = new Scanner(names).useDelimiter("_'");
		while (s.hasNext()) {
			temp.add(s.next());
		}
		s.close();
		
		for (String str : temp) {
			if (str.indexOf("'_") >= 0) {
				String[] strings = str.split("'_");
				for (int i = 0; i < strings.length; i++) {
					list.add(strings[i].replaceAll("'", ""));
				}
			} else {
				list.add(str.replaceAll("'", ""));
			}
		}
		
		if (list.size() == 1) {
			if (endsWithPrimitive(list.get(0))) {
				String name = list.remove(0);
				int i = name.lastIndexOf("_");
				name = name.substring(0, i);
				list.add(name);
			}
		} else if (isPrimitive(list.get(list.size()-1))) {
			// remove the last entry if its a primitive ... legacy reasons
			list.remove(list.size()-1);
		}
		return list;
	}
	private static boolean endsWithPrimitive(String name) {
		if (name.endsWith("_Integer") || name.endsWith("_String") || name.endsWith("_Float")
				|| name.endsWith("_DateTime") || name.endsWith("_Boolean"))
			return true;
		return false;
	}
	
	private static boolean isPrimitive(String name) {
		if (name.equals("Integer") || name.equals("String") || name.equals("Float")
				|| name.equals("DateTime") || name.equals("Boolean"))
			return true;
		return false;
	}
}
