/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.tools.Lang;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RSrvException;
import org.rosuda.JRclient.Rconnection;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A task to invoke a RProcessor. Connect to the Rserv by using JRclient, and
 * execute R script. Converts inputs and outputs as appropriate
 * 
 * @author Stian Soiland
 */
public class RservTask implements ProcessorTaskWorker {

	private RservProcessor proc;

	public RservTask(Processor p) {
		this.proc = (RservProcessor) p;
	}
	
	/*
	 * Convert REXP to suitable (list of) strings so to be outputted as
	 * l('text/plain')
	 */
	public Object rexpToJava(REXP exp) {
		if (exp.getType() == REXP.XT_ARRAY_INT) {
			ArrayList result = new ArrayList();
			int ints[] = exp.asIntArray();
			for (int i = 0; i < ints.length; i++) {
				result.add(Integer.toString(ints[i]));
			}
			return result;
		} else if (exp.getType() == REXP.XT_ARRAY_DOUBLE) {
			ArrayList result = new ArrayList();
			double doubles[] = exp.asDoubleArray();
			if (doubles != null) {
				for (int i = 0; i < doubles.length; i++) {
					result.add(Double.toString(doubles[i]));
				}
			}
			return result;
		} else if (exp.getType() == REXP.XT_VECTOR) {
			Vector vec = exp.asVector();
			// Recursively call ourself for each element in the vector and
			// return the List of
			// results.
			return Lang.map("rexpToJava", vec, this);
		} else {
			// This should handle INT and DOUBLE and BOOL and STR,
			// and possibly other weird stuff, according to JRclient source
			// code.
			Object content = exp.getContent();
			return content.toString();
		}
	}

	/*
	 * Convert workflow input to an REXP using the given javaType. For instance,
	 * if javaType is double[], and input is a list of strings, each string will
	 * be converted to a double, and the array will be wrapped in an REXP.
	 * 
	 * If the object could not be converted, null is returned.
	 */
	static public REXP javaToRexp(Object input, Class javaType) {
		// Simple case, just return as is
		if (javaType.equals(REXP.class) && javaType.isInstance(input)) {
			return (REXP) input;
		}
		// Unwrap collections (because we need to convert the kids)
		List children = null;
		if (input instanceof List) {
			children = (List) input;
		}

		if (javaType.equals(int[].class)) {
			if (children != null) {
				int[] ints = new int[children.size()];
				for (int j = 0; j < children.size(); j++) {
					// Assume children are Strings
					ints[j] = Integer.parseInt((String) children.get(j));
				}
				return new REXP(ints);
			}
			if (javaType.isInstance(input)) {
				return new REXP((int[]) input);
			}
		}

		if (javaType.equals(double[].class)) {
			if (children != null) {
				double[] doubles = new double[children.size()];
				for (int j = 0; j < children.size(); j++) {
					// Assume children are Strings
					doubles[j] = Double.parseDouble((String) children.get(j));
				}
				return new REXP(doubles);
			}
			if (javaType.isInstance(input)) {
				return new REXP((double[]) input);
			}
		}

		if (javaType.equals(String[].class)) {
			if (children != null) {
				// Assume children are Strings, return as is
				return new REXP((String[]) children.toArray(new String[0]));
			}
			if (javaType.isInstance(input)) {
				return new REXP((String[]) input);
			}
			// FIXME: Convert other object types to strings?
		}
		// FIXME: Should be a real exception
		/* Unhandled, return null */
		return null;
	}

	/*
	 * Connect and log in to Rserv using the connection info and authorization
	 * in the processor. A TaskExecutionException is thrown if the connection or
	 * login fails.
	 */
	public Rconnection connect() throws TaskExecutionException {
		Rconnection connection;
		String hostname = proc.getHostname();
		if (hostname.equals("")) {
			hostname = "127.0.0.1";
		}
		int port = proc.getPort();
		try {
			if (port == 0) {
				connection = new Rconnection(hostname);
			} else {
				connection = new Rconnection(hostname, port);
			}
		} catch (RSrvException e) {
			throw new TaskExecutionException("Could not connect to Rserv on "
					+ hostname + ": " + e.getMessage());
		}
		if (connection.needLogin()) {
			try {
				connection.login(proc.getUsername(), proc.getPassword());
			} catch (RSrvException e) {
				throw new TaskExecutionException("Could not login "
						+ proc.getUsername() + " on Rserv connection to "
						+ hostname + ": " + e.getMessage());
			}
		}
		return connection;
	}

	/*
	 * Process R script on the workflow inputs.
	 * 
	 * Inputs will be converted to suitable R types as specified in the
	 * RservInputPort and defined in the RservConfigPanel.
	 * 
	 * Each RservInputPort is available as a local variable in the R
	 * environment, which will be fresh on each execute()
	 * 
	 * With the current implementation, the Rserv server must be running on
	 * localhost on the default port without requiring any authentication.
	 * 
	 * Output will be converted to a (list of) strings.
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker#execute(java.util.Map,
	 *      uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask)
	 */
	public Map execute(Map workflowInputMap, IProcessorTask parentTask)
			throws TaskExecutionException {
		// FIXME: Check for Rserv availability
		Rconnection r = connect();
		InputPort[] ports = (InputPort[]) this.proc.getInputPorts();
		for (int i = 0; i < ports.length; i++) {
			RservInputPort port = (RservInputPort) ports[i];
			String inputName = port.getName();
			DataThing inputObject = (DataThing) workflowInputMap.get(inputName);
			if (inputObject == null) {
				throw new TaskExecutionException("Input to web service '"
						+ inputName + "' was defined but not provided.");
			}
			Object input = inputObject.getDataObject();
			String javaTypeName = port.getJavaType();
			Class javaType = (Class) RservInputPort.javaTypes.get(javaTypeName);
			REXP value = javaToRexp(input, javaType);
			if (value == null) {
				throw new TaskExecutionException("Input to web service '"
						+ inputName + "' could not be interpreted as a "
						+ javaTypeName + ", it is a " + input.getClass());
			}
			try {
				r.assign(inputName, value);
			} catch (RSrvException e) {
				throw new TaskExecutionException("Input to web service '"
						+ inputName + "' could not be bound to Rserv: "
						+ e.getMessage());

			}
		}
		String script = this.proc.getScript();
		System.out.println("Running in R:");
		System.out.println(script);
		// FIXME: Should catch R errors to present them for the user, for
		// instance invalid syntax or unknown variables
		REXP res;
		try {
			res = r.eval(script);
		} catch (RSrvException e) {
			throw new TaskExecutionException("Could not evaluate R script: "
					+ e.getMessage());
		}
		System.out.println(res);
		Map outputs = new HashMap();
		// TODO: Should be possible to return as REXP
		outputs.put("value", new DataThing(rexpToJava(res)));
		return outputs;

	}
}
