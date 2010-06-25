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
package net.sf.taverna.t2.activities.rshell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.sf.taverna.t2.activities.rshell.RshellPortTypes.SymanticTypes;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RFileInputStream;
import org.rosuda.JRclient.RFileOutputStream;
import org.rosuda.JRclient.RList;
import org.rosuda.JRclient.RSrvException;

/**
 * An Activity providing Rshell functionality.
 * 
 */
public class RshellActivity extends
		AbstractAsynchronousActivity<RshellActivityConfigurationBean> {

	private static int BUF_SIZE = 1024;

	private RshellActivityConfigurationBean configurationBean;

	private Map<String, SymanticTypes> inputSymanticTypes = new HashMap<String, SymanticTypes>();

	private Map<String, SymanticTypes> outputSymanticTypes = new HashMap<String, SymanticTypes>();

	@Override
	public void configure(RshellActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configurePorts(configurationBean);
		configureSymanticTypes(configurationBean);
	}

	@Override
	public RshellActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				RshellConnection connection = null;

				try {

					RshellConnectionSettings settings = configurationBean
							.getConnectionSettings();

					// create connection
					try {
						connection = RshellConnectionManager.INSTANCE
								.createConnection(settings);
					} catch (Exception ex) {
						callback.fail("Could not establish connection to "
								+ settings.getHost() + " using port "
								+ settings.getPort() + ": " + ex.getMessage(),
								ex);
					}

					// pass input form input ports to RServe
					for (ActivityInputPort inputPort : getInputPorts()) {
						String inputName = inputPort.getName();
						SymanticTypes symanticType = inputSymanticTypes
								.get(inputName);
						T2Reference inputId = data.get(inputName);
						if (inputId == null) {
							callback.fail("Input to rserve '" + inputName
									+ "' was defined but not provided.");
						}

						Object input = referenceService.renderIdentifier(inputId, inputPort
								.getTranslatedElementClass(), callback.getContext());
						if (symanticType.isFile) {
							connection.assign(inputName,
									generateFilename(inputPort));
							writeRServeFile(inputPort, connection, input);
						} else {

							REXP value = javaToRExp(input, symanticType);
							if (value == null) {
								callback.fail("Input to web service '"
										+ inputName
										+ "' could not be interpreted as a "
										+ symanticType + ", it is a "
										+ input.getClass());
							}
							connection.assign(inputName, value);
						}
					}

					// create filename variables for output ports that are
					// files
					for (OutputPort outputPort : getOutputPorts()) {
						if (outputSymanticTypes.get(outputPort.getName()).isFile) {
							connection.assign(outputPort.getName(),
									generateFilename(outputPort));
						}
					}

					// execute script
					String script = configurationBean.getScript();
					if (script.length() != 0) {
						connection.voidEval(script);
					}

					// create last statement for getting output for output
					// ports
					Set<OutputPort> outputPorts = getOutputPorts();
					StringBuffer returnString = new StringBuffer("list(");
					boolean first = true;
					for (OutputPort outputPort : outputPorts) {
						if (first) {
							first = false;
						} else {
							returnString.append(", ");
						}
						String portName = outputPort.getName();
						returnString.append(portName);
						returnString.append("=");
						returnString.append(portName);
					}
					returnString.append(")\n");
					REXP results = connection.eval(returnString.toString());

					// convert output and put it in results map
					switch (results.getType()) {
					case REXP.XT_LIST: {
						RList resultList = results.asList();

						assert (resultList.keys().length == outputPorts.size());
						for (OutputPort outputPort : outputPorts) {
							String portName = outputPort.getName();
							SymanticTypes symanticType = outputSymanticTypes
									.get(portName);

							switch (symanticType) {
							case PNG_FILE: {
								byte[] data = (byte[]) readRServeFile(
										outputPort, connection);
								outputData.put(portName, referenceService
										.register(data, 0, true, callback.getContext()));
								break;
							}
							case TEXT_FILE: {
								String data = (String) readRServeFile(
										outputPort, connection);
								outputData.put(portName, referenceService
										.register(data, 0, true, callback.getContext()));
								break;
							}
							default: {
								REXP expression = resultList.at(portName);
								Object object = rExpToJava(expression,
										symanticType);
								outputData.put(portName, referenceService
										.register(object, 0, true, callback.getContext()));
							}
							}
						}
						break;
					}
					case REXP.XT_VECTOR: {
						if (outputPorts.size() == 0) {
							break;
						} else {
						}
					}

					default:
						callback.fail("Unexpected result");
					}

					// close the connection
					cleanUpServerFiles(connection);
					RshellConnectionManager.INSTANCE
							.releaseConnection(connection);
					connection = null;

					// send result to the callback
					callback.receiveResult(outputData, new int[0]);

				} catch (ActivityConfigurationException ace) {
					callback.fail("RShell failed", ace);
				} catch (RSrvException rSrvException) {
					callback.fail("RShell failed: "
							+ rSrvException.getRequestErrorDescription(),
							rSrvException);
				} catch (ReferenceServiceException e) {
					callback.fail("Error accessing input/output data", e);
				} finally {
					if (connection != null && connection.isConnected()) {
						cleanUpServerFiles(connection);
						RshellConnectionManager.INSTANCE
								.releaseConnection(connection);
						connection = null;
					}
				}
			}

		});

	}

	private void configureSymanticTypes(
			RshellActivityConfigurationBean configurationBean) {
		for (RShellPortSymanticTypeBean portSymanticType : configurationBean
				.getInputSymanticTypes()) {
			inputSymanticTypes.put(portSymanticType.getName(), portSymanticType
					.getSymanticType());
		}
		for (RShellPortSymanticTypeBean symanticType : configurationBean
				.getOutputSymanticTypes()) {
			outputSymanticTypes.put(symanticType.getName(), symanticType
					.getSymanticType());
		}
	}

	/**
	 * Method for cleaing up all generated files
	 * 
	 * @param connection
	 *            the connection to be used
	 */
	private void cleanUpServerFiles(RshellConnection connection) {
		for (ActivityInputPort inputPort : getInputPorts()) {

			if (inputSymanticTypes.get(inputPort.getName()).isFile) {
				try {
					connection.removeFile(generateFilename(inputPort));
				} catch (RSrvException rse) {
					// do nothing, try to delete other files
				}
			}
		}

		for (OutputPort outputPort : getOutputPorts()) {

			if (outputSymanticTypes.get(outputPort.getName()).isFile) {
				try {
					connection.removeFile(generateFilename(outputPort));
				} catch (RSrvException rse) {
					// do nothing, try to delete other files
				}
			}
		}
	}

	/**
	 * Method for generating a valid filename for a port
	 * 
	 * @param Port
	 *            the port to generate a filename for
	 * @return the filename
	 * 
	 */
	private String generateFilename(Port Port) {
		String portName = Port.getName();
		return portName.replaceAll("\\W", "_") + ".png";
	}

	/**
	 * Convert workflow input to an REXP using the given javaType. For instance,
	 * if javaType is double[], and input is a list of strings, each string will
	 * be converted to a double, and the array will be wrapped in an REXP.
	 * 
	 * @param input
	 *            the input object to be converted
	 * @param symantic
	 *            type to which the rExp should be converted
	 * @throws ActivityConfigurationException
	 *             if symantictype is not supported
	 */
	@SuppressWarnings("unchecked")
	private REXP javaToRExp(Object value, SymanticTypes symanticType)
			throws ActivityConfigurationException {
		switch (symanticType) {
		case REXP:
			return (REXP) value;

		case BOOL: {
			String strValue = (String) value;
			boolean bool = strValue.toLowerCase().equals("true")
					|| strValue.equals("1");
			return new REXP(new String[] { (bool) ? "true" : "false" });
		}
		case DOUBLE:
			return new REXP(new double[] { Double.parseDouble((String) value) });
		case INTEGER:
			return new REXP(new int[] { Integer.parseInt((String) value) });
		case STRING:
			return new REXP(new String[] { (String) value });

		case DOUBLE_LIST: {
			List values = (List) value;
			double[] doubles = new double[values.size()];
			for (int i = 0; i < values.size(); i++) {
				doubles[i] = Double.parseDouble((String) values.get(i));
			}
			return new REXP(doubles);
		}
		case INTEGER_LIST: {
			List values = (List) value;
			int[] ints = new int[values.size()];
			for (int i = 0; i < values.size(); i++) {
				ints[i] = Integer.parseInt((String) values.get(i));
			}
			return new REXP(ints);
		}
		case STRING_LIST: {
			List values = (List) value;
			String[] strings = new String[values.size()];
			for (int i = 0; i < values.size(); i++) {
				strings[i] = (String) values.get(i);
			}
			return new REXP(strings);
		}

		default:
			throw new ActivityConfigurationException("Symantic type "
					+ symanticType + " not supported");
		}
	}

	/**
	 * Method for reading the png image
	 * 
	 * @param outputPort
	 *            the output port to read the file from
	 * @param connection
	 *            the connection to be used
	 * @return data the data of the file
	 */
	private Object readRServeFile(OutputPort outputPort,
			RshellConnection connection) throws ActivityConfigurationException {

		String filename = generateFilename(outputPort);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		RFileInputStream inputStream = null;
		byte[] bytes;

		try {
			inputStream = connection.openFile(filename);

			int bytesRead;
			byte[] buffer = new byte[BUF_SIZE];
			while ((bytesRead = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, bytesRead);
			}
			inputStream.close();
			outputStream.close();

			bytes = outputStream.toByteArray();
			outputStream.close();

		} catch (IOException ioe) {
			try {
				if (inputStream != null && connection.isConnected()) {
					inputStream.close();
				}
			} catch (Exception e) {
			}

			throw new ActivityConfigurationException("Cannot read file '"
					+ filename + "' from Rserve: " + ioe.getMessage());
		}

		Object value;
		switch (outputSymanticTypes.get(outputPort.getName())) {
		case TEXT_FILE:
			value = new String(bytes);
			break;
		default:
			value = bytes;
		}
		return value;
	}

	/**
	 * Convert REXP to suitable (list of)
	 * 
	 * @param rExp
	 *            the r expression
	 * @param symanticType
	 *            the type of the output port
	 * @return a Java object which is a representation of the rExpression
	 * @throw TaskExecutionException if rExp type is unsupported
	 */
	private Object rExpToJava(REXP rExp, SymanticTypes symanticType)
			throws ActivityConfigurationException {

		switch (symanticType) {
		case REXP:
			return rExp;

		case BOOL:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return Boolean.toString(false);
			case REXP.XT_ARRAY_INT:
				return Boolean.toString(rExp.asIntArray()[0] != 0);
			case REXP.XT_ARRAY_DOUBLE:
				return Boolean
						.toString(Math.abs(rExp.asDoubleArray()[0]) > 0.00001);
			case REXP.XT_BOOL:
				return Boolean.toString(rExp.asBool().isTRUE());
			case REXP.XT_DOUBLE:
				return Boolean.toString(Math.abs(rExp.asDouble()) > 0.00001);
			case REXP.XT_INT:
				return Boolean.toString(rExp.asInt() != 0);
			default:
			}
			break;

		case DOUBLE:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return 0.0;
			case REXP.XT_ARRAY_DOUBLE:
				return Double.toString(rExp.asDoubleArray()[0]);
			case REXP.XT_ARRAY_INT:
				return Double.toString(rExp.asIntArray()[0]);
			case REXP.XT_BOOL:
				return (rExp.asBool().isTRUE()) ? "1.0" : "0.0";
			case REXP.XT_DOUBLE:
				return Double.toString(rExp.asDouble());
			case REXP.XT_INT:
				return Double.toString(rExp.asInt());
			default:
			}
			break;

		case INTEGER:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return 0;
			case REXP.XT_ARRAY_DOUBLE:
				return Integer.toString((int) rExp.asDoubleArray()[0]);
			case REXP.XT_ARRAY_INT:
				return Integer.toString(rExp.asIntArray()[0]);
			case REXP.XT_BOOL:
				return (rExp.asBool().isTRUE()) ? "1" : "0";
			case REXP.XT_DOUBLE:
				return Integer.toString((int) rExp.asDouble());
			case REXP.XT_INT:
				return Integer.toString(rExp.asInt());
			default:
			}
			break;

		case STRING:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return "null";
			case REXP.XT_ARRAY_DOUBLE:
				return Arrays.toString(rExp.asDoubleArray());
			case REXP.XT_ARRAY_INT:
				return Arrays.toString(rExp.asIntArray());
			case REXP.XT_BOOL:
				return (Boolean.toString(rExp.asBool().isTRUE()));
			case REXP.XT_DOUBLE:
				return Double.toString((int) rExp.asDouble());
			case REXP.XT_INT:
				return Integer.toString(rExp.asInt());
			case REXP.XT_STR:
				return rExp.asString();
			case REXP.XT_VECTOR:
				return rVectorToString(rExp);
			case REXP.XT_LIST:
				return rListToString(rExp);
			default:
			}
			break;

		case DOUBLE_LIST:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return new ArrayList<String>();
			case REXP.XT_ARRAY_DOUBLE: {
				ArrayList<String> values = new ArrayList<String>();
				for (double value : rExp.asDoubleArray()) {
					values.add(Double.toString(value));
				}
				return values;
			}
			case REXP.XT_ARRAY_INT: {
				ArrayList<String> values = new ArrayList<String>();
				for (int value : rExp.asIntArray()) {
					values.add(Double.toString(value));
				}
				return values;
			}
			case REXP.XT_BOOL:
				return new String[] { (rExp.asBool().isTRUE()) ? "1.0" : "0.0" };
			case REXP.XT_DOUBLE:
				return new String[] { Double.toString(rExp.asDouble()) };
			case REXP.XT_INT:
				return new String[] { Double.toString(rExp.asInt()) };
			default:
			}
			break;
		case INTEGER_LIST:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return new ArrayList<String>();
			case REXP.XT_ARRAY_DOUBLE: {
				ArrayList<String> values = new ArrayList<String>();
				for (double value : rExp.asDoubleArray()) {
					values.add(Integer.toString((int) value));
				}
				return values;
			}
			case REXP.XT_ARRAY_INT: {
				ArrayList<String> values = new ArrayList<String>();
				for (int value : rExp.asIntArray()) {
					values.add(Integer.toString(value));
				}
				return values;
			}
			case REXP.XT_BOOL:
				return (rExp.asBool().isTRUE()) ? "1" : "0";
			case REXP.XT_DOUBLE:
				return new String[] { Integer.toString((int) rExp.asDouble()) };
			case REXP.XT_INT:
				return new String[] { Integer.toString(rExp.asInt()) };
			default:
			}
			break;

		case STRING_LIST:
			switch (rExp.getType()) {
			case REXP.XT_NULL:
				return new ArrayList<String>();
			case REXP.XT_ARRAY_DOUBLE: {
				ArrayList<String> values = new ArrayList<String>();
				for (double value : rExp.asDoubleArray()) {
					values.add(Double.toString((int) value));
				}
				return values;
			}
			case REXP.XT_ARRAY_INT: {
				ArrayList<String> values = new ArrayList<String>();
				for (int value : rExp.asIntArray()) {
					values.add(Integer.toString(value));
				}
				return values;
			}
			case REXP.XT_BOOL:
				return new String[] { Boolean.toString(rExp.asBool().isTRUE()) };
			case REXP.XT_DOUBLE:
				return new String[] { Double.toString((int) rExp.asDouble()) };
			case REXP.XT_INT:
				return new String[] { Integer.toString(rExp.asInt()) };
			case REXP.XT_VECTOR: {
				ArrayList<String> values = new ArrayList<String>();
				for (Object value : rExp.asVector()) {
					values.add((String) rExpToJava((REXP) value,
							SymanticTypes.STRING));
				}
				return values;
			}
			default:
			}
			break;
		default:
		}

		// should never end here
		throw new ActivityConfigurationException(
				"Cannot convert R expression type '" + rExp.getType()
						+ "' to the semantic type '" + symanticType.description
						+ "'");

	}

	/**
	 * Helper method for rExpToJava for converting a list to a string value
	 * 
	 * @param rExp
	 *            the rExp to be converted
	 * @return the string representation
	 */
	private String rListToString(REXP rExp)
			throws ActivityConfigurationException {
		StringBuffer stringRep = new StringBuffer();
		RList list = rExp.asList();
		for (String key : list.keys()) {
			stringRep.append(key);
			stringRep.append("=");
			stringRep.append(rExpToJava(list.at(key), SymanticTypes.STRING));
			stringRep.append("\n");
		}
		return stringRep.toString();
	}

	/**
	 * Helper function for converting a vector to a string
	 * 
	 * @rExp the rExp to be converted to a string
	 * @return the string representation
	 */
	@SuppressWarnings("unchecked")
	private String rVectorToString(REXP rExp)
			throws ActivityConfigurationException {
		Vector elements = rExp.asVector();
		ArrayList<String> strings = new ArrayList<String>();
		for (Object object : elements) {
			strings
					.add((String) rExpToJava((REXP) object,
							SymanticTypes.STRING));
		}
		return Arrays.toString(strings.toArray());
	}

	/**
	 * Method for writing the server file
	 * 
	 * @param inputPort
	 *            the input port to write a file to
	 * @param connection
	 *            the connection of the rserve
	 * @param Object
	 *            the data to be transfered
	 */
	private void writeRServeFile(ActivityInputPort inputPort,
			RshellConnection connection, Object data)
			throws ActivityConfigurationException {

		RFileOutputStream outputStream = null;
		String filename = generateFilename(inputPort);

		try {

			byte[] bytes;
			switch (inputSymanticTypes.get(inputPort.getName())) {
			case TEXT_FILE:
				bytes = ((String) data).getBytes();
				break;
			default:
				bytes = (byte[]) data;
			}

			outputStream = connection.createFile(filename);

			outputStream.write(bytes);
			outputStream.close();
		} catch (IOException ioe) {
			try {
				if (outputStream != null && connection.isConnected()) {
					outputStream.close();
				}
			} catch (Exception e) {
			}

			throw new ActivityConfigurationException("Cannot read file '"
					+ filename + "' from Rserve: " + ioe.getMessage());
		}
	}

}
