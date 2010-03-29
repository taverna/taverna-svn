/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.rshell.RshellPortTypes.SemanticTypes;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * An Activity providing Rshell functionality.
 * 
 */
public class RshellActivity extends
		AbstractAsynchronousActivity<RshellActivityConfigurationBean> {
	
	private static Logger logger = Logger.getLogger(RshellActivity.class);

	private static int BUF_SIZE = 1024;

	private RshellActivityConfigurationBean configurationBean;

	private Map<String, SemanticTypes> inputSymanticTypes = new HashMap<String, SemanticTypes>();

	private Map<String, SemanticTypes> outputSymanticTypes = new HashMap<String, SemanticTypes>();
	
	private static HashMap<RshellConnectionSettings, Object> lockMap = new HashMap<RshellConnectionSettings, Object> ();
	
	private static String stringNA = "NA";
	private static String stringTrue = "TRUE";
	private static String stringFalse = "FALSE";

	@Override
	public synchronized void configure(RshellActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configureSymanticTypes(configurationBean);
		for (ActivityInputPortDefinitionBean ip : configurationBean.getInputPortDefinitions()) {
			String name = ip.getName();
			if (name != null) {
				SemanticTypes symanticType = inputSymanticTypes.get(name);
				if (symanticType != null) {
					ip.setDepth(symanticType.getDepth());
				}
			}
		}
		for (ActivityOutputPortDefinitionBean op : configurationBean.getOutputPortDefinitions()) {
			String name = op.getName();
			if (name != null) {
				SemanticTypes symanticType = outputSymanticTypes.get(name);
				if (symanticType != null) {
					op.setDepth(symanticType.getDepth());
				}
			}
		}
		configurePorts(configurationBean);
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

					RshellConnectionSettings settings = configurationBean
							.getConnectionSettings();
					
					Object lock = getLock(settings);
					
					synchronized(lock) {

					// create connection
					try {
						connection = RshellConnectionManager.INSTANCE
								.createConnection(settings);
					} catch (Exception ex) {
						callback.fail("Could not establish connection to "
								+ settings.getHost() + " using port "
								+ settings.getPort() + ": " + ex.getMessage(),
								ex);
						return;
					}

						try {

					// pass input form input ports to RServe
					for (ActivityInputPort inputPort : getInputPorts()) {
						String inputName = inputPort.getName();
						SemanticTypes symanticType = inputSymanticTypes
								.get(inputName);
						T2Reference inputId = data.get(inputName);
						if (inputId == null) {
							callback.fail("Input to rserve '" + inputName
									+ "' was defined but not provided.");
							closeConnection(connection);
							return;
						}

						Object input = referenceService.renderIdentifier(inputId, symanticType.getSemanticClass(), callback.getContext());
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
								closeConnection(connection);
								return;
							}
							if (value.isLogical()) {
								if (((REXPLogical)value).length() == 1) {
									connection.voidEval(inputName + " <- " + value.asString() + ";");
								} else {
									String[] strings = value.asStrings();
									String completeString = "c(" + StringUtils.join(strings, ",") + ")";
									connection.voidEval(inputName + " <- " + completeString + ";");
								}
							} else {
								connection.assign(inputName, value);
							}
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
						connection.assign(".tmp.", script);
						REXP r = connection.parseAndEval("try(eval(parse(text=.tmp.)),silent=TRUE)");
						if (r.inherits("try-error")) {
							String errorString = r.asString();
							if (errorString.contains(": ")) {
								errorString = errorString.substring(errorString.indexOf(": ") + 2);
							}
							throw new RserveException(connection, errorString);
						}
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
					connection.assign(".tmp.", returnString.toString());
					REXP results = connection.parseAndEval("try(eval(parse(text=.tmp.)),silent=TRUE)");
					if (results.inherits("try-error")) {
						String errorString = results.asString();
						if (errorString.contains(": ")) {
							errorString = errorString.substring(errorString.indexOf(": ") + 2);
						}
						throw new RserveException(connection, errorString);
					}

					if (results.isList()) {
						RList resultList = results.asList();

						assert (resultList.keys().length == outputPorts.size());
						for (OutputPort outputPort : outputPorts) {
							String portName = outputPort.getName();
							SemanticTypes symanticType = outputSymanticTypes
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
										.register(object, outputPort.getDepth(), true, callback.getContext()));
							}
							}
						}
					}
					else if (results.isVector()) {
						if (outputPorts.size() == 0) {
//							break;
						} else {
						}
					} else {
						callback.fail("Unexpected result");
						closeConnection(connection);
						return;
					}

					// close the connection
					closeConnection(connection);

					// send result to the callback
					callback.receiveResult(outputData, new int[0]);

				} catch (ActivityConfigurationException ace) {
					callback.fail("RShell failed", ace);
				} catch (RserveException rSrvException) {
					callback.fail("RShell failed: " + rSrvException.getMessage(),
							rSrvException);
				} catch (ReferenceServiceException e) {
					callback.fail("Error accessing input/output data", e);
				} catch (REXPMismatchException e) {
					callback.fail("RShell failed: "
							+ e.getMessage(),
							e);
				} catch (REngineException e) {
					callback.fail("RShell failed: "
							+ e.getMessage(),
							e);
				} finally {
					closeConnection(connection);
				}
					}
			}

		});

	}
	
	private static Object getLock(RshellConnectionSettings settings) {
		Object result = null;
		result = lockMap.get(settings);
		if (result == null) {
			result = new Object();
			lockMap.put(settings, result);
		}
		return result;
	}
	
	private void closeConnection(RshellConnection connection) {
		if (connection != null && connection.isConnected()) {
			cleanUpServerFiles(connection);
			RshellConnectionManager.INSTANCE
					.releaseConnection(connection);
			connection = null;
		}
		
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
				} catch (RserveException rse) {
					// do nothing, try to delete other files
				}
			}
		}

		for (OutputPort outputPort : getOutputPorts()) {

			if (outputSymanticTypes.get(outputPort.getName()).isFile) {
				try {
					connection.removeFile(generateFilename(outputPort));
				} catch (RserveException rse) {
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
	private String generateFilename(Port port) {

		String portName = port.getName();
		SemanticTypes semanticType = outputSymanticTypes
				.get(portName);

		String extension;
		if (semanticType == RshellPortTypes.SemanticTypes.PNG_FILE) {
			extension = ".png";
		} else if (semanticType == RshellPortTypes.SemanticTypes.TEXT_FILE) {
			extension = ".txt";
		}
//		else if (semanticType == RshellPortTypes.SemanticTypes.PDF_FILE) {
//			extension = ".pdf";
//		}
		else {
			extension = "";
		}
		return portName.replaceAll("\\W", "_") + extension;
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
	private REXP javaToRExp(Object value, SemanticTypes symanticType)
			throws ActivityConfigurationException {
		switch (symanticType) {
		case REXP:
			return (REXP) value;

		case BOOL:
			if (value instanceof String) {
				return stringToREXPLogical((String) value);
			}
			return new REXPLogical(REXPLogical.NA);

		case DOUBLE:
			return new REXPDouble((Double) value );
		case INTEGER:
			return new REXPInteger((Integer) value);
		case STRING:
			return new REXPString((String) value);

		case BOOL_LIST:{
			List<String> values = (List<String>) value;
			byte[] bValues = new byte[values.size()];
			int i = 0;
			for (String s : values) {
				bValues[i++] = stringToByte(s);
			}
			return new REXPLogical(bValues);
		}
		case DOUBLE_LIST: {
			List values = (List) value;
			double[] doubles = new double[values.size()];
			for (int i = 0; i < values.size(); i++) {
				doubles[i] = (Double) values.get(i);
			}
			return new REXPDouble(doubles);
		}
		case INTEGER_LIST: {
			List values = (List) value;
			int[] ints = new int[values.size()];
			for (int i = 0; i < values.size(); i++) {
				ints[i] = (Integer) values.get(i);
			}
			return new REXPInteger(ints);
		}
		case STRING_LIST: {
			List values = (List) value;
			String[] strings = new String[values.size()];
			for (int i = 0; i < values.size(); i++) {
				strings[i] = (String) values.get(i);
			}
			return new REXPString(strings);
		}

		default:
			throw new ActivityConfigurationException("Symantic type "
					+ symanticType + " not supported");
		}
	}
	
	private static REXPLogical stringToREXPLogical(String value) {
		return new REXPLogical (stringToByte(value));
	}
	
	private static byte stringToByte(String value) {
		if (value.equalsIgnoreCase(stringTrue)) {
			return REXPLogical.TRUE;
		}
		if (value.equalsIgnoreCase(stringFalse)) {
			return REXPLogical.FALSE;
		}
		return REXPLogical.NA;
		
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
	 * @throws REXPMismatchException 
	 * @throw TaskExecutionException if rExp type is unsupported
	 */
	private Object rExpToJava(REXP rExp, SemanticTypes symanticType)
			throws ActivityConfigurationException, REXPMismatchException {

		switch (symanticType) {
		case REXP:
			return rExp;

		case BOOL:
			if (rExp.isNull()) {
				return stringNA;
			}
			if (rExp.isLogical()) {
				return rExp.asString();
			}
			if (rExp.isInteger()) {
				if (rExp.asInteger() != 0) {
					return stringTrue;
				}
				return stringFalse;
			}
			if (rExp.isNumeric()) {
				if (Math.abs(rExp.asDouble()) > 0.00001) {
					return stringTrue;
				}
				return stringFalse;
			}
			if (rExp.isString()) {
				if (rExp.asString().equalsIgnoreCase("true")) {
					return stringTrue;
				}
				return stringFalse;
			}
			break;

		case BOOL_LIST:
			ArrayList<String> boolResult = new ArrayList<String>();
			if (rExp.isNull()) {
				return boolResult;
			}
			if (rExp.isLogical()) {
				for (String s : rExp.asStrings()) {
					boolResult.add(s);
				}		
				return boolResult;
			}
			if (rExp.isInteger()) {
				for (int i : rExp.asIntegers()) {
					boolResult.add (i != 0 ? stringTrue : stringFalse);
				}
				return boolResult;
			}
			if (rExp.isNumeric()) {
				for (double d : rExp.asDoubles()) {
					boolResult.add (d > 0.00001 ? stringTrue : stringFalse);
				}
				return boolResult;
			}
			if (rExp.isString()) {
				for (String s : rExp.asStrings()) {
					boolResult.add(s.equalsIgnoreCase("true") ? stringTrue : stringFalse);
				}
				return boolResult;
			}
			break;

		case DOUBLE:
			if (rExp.isNull()) {
				return new Double(0.0);
			}
			if (rExp.isLogical()) {
				REXPLogical logical = (REXPLogical) rExp;
				return (new Double(logical.isTRUE()[0] ? 1.0 : 0.0));
			}
			if (rExp.isInteger()) {
				return new Double(rExp.asIntegers()[0]);
			}
			if (rExp.isNumeric()) {
				return new Double(rExp.asDoubles()[0]);
			}
			if (rExp.isString()) {
				// Cannot cope
			}
			break;
			
		case DOUBLE_LIST:
			ArrayList<Double> doubleResult = new ArrayList<Double>();
			if (rExp.isNull()) {
				return doubleResult;
			}
			if (rExp.isLogical()) {
				REXPLogical logical = (REXPLogical) rExp;
				for (boolean b : logical.isTRUE()) {
					doubleResult.add(new Double(b ? 1.0 : 0.0));
				}
				return doubleResult;
			}
			if (rExp.isInteger()) {
				for (int i : rExp.asIntegers()) {
					doubleResult.add(new Double(i));
				}
				return doubleResult;
			}
			if (rExp.isNumeric()) {
				for (double d : rExp.asDoubles()) {
					doubleResult.add(new Double(d));
				}
				return doubleResult;
			}
			if (rExp.isString()) {
				// Cannot cope
			}
			break;

		case INTEGER:
			if (rExp.isNull()) {
				return new Integer(0);
			}
			if (rExp.isLogical()) {
				REXPLogical logical = (REXPLogical) rExp;
				return (new Integer(logical.isTRUE()[0] ? 1 : 0));			
			}
			if (rExp.isInteger()) {
				return new Integer(rExp.asIntegers()[0]);
			}
			if (rExp.isNumeric()) {
				return new Integer((int) rExp.asDoubles()[0]);
			}
			if (rExp.isString()) {
				// Cannot cope
			}
			break;

		case INTEGER_LIST:
			ArrayList<Integer> integerResult = new ArrayList<Integer>();
			if (rExp.isNull()) {
				return integerResult;
			}
			if (rExp.isLogical()) {
				REXPLogical logical = (REXPLogical) rExp;
				for (boolean b : logical.isTRUE()) {
					integerResult.add(new Integer(b ? 1 : 0));
				}
				return integerResult;
			}
			if (rExp.isInteger()) {
				for (int i : rExp.asIntegers()) {
					integerResult.add (new Integer(i));
				}
				return integerResult;
			}
			if (rExp.isNumeric()) {
				for (double d : rExp.asDoubles()) {
					integerResult.add(new Integer((int) d));
				}
				return integerResult;
			}
			if (rExp.isString()) {
				// Cannot cope
			}
			break;
			
		case STRING:
			if (rExp.isNull()) {
				return ("null");
			}
			if (rExp.isLogical()) {
				REXPLogical logical = (REXPLogical) rExp;
				return (Arrays.toString(logical.isTRUE()));							
			}
			if (rExp.isInteger()) {
				return (Arrays.toString(rExp.asIntegers()));
			}
			if (rExp.isNumeric()) {
				return (Arrays.toString(rExp.asDoubles()));
			}
			if (rExp.isString()) {
				// Always take the first
				return rExp.asString();
			}
			break;

		case STRING_LIST:
			ArrayList<String> stringResult = new ArrayList<String>();
			if (rExp.isNull()) {
				return stringResult;
			}
			if (rExp.isLogical()) {
				REXPLogical logical = (REXPLogical) rExp;
				for (boolean b : logical.isTRUE()) {
					stringResult.add(b ? "true" : "false");
				}
				return stringResult;
			}
			if (rExp.isInteger()) {
				for (int i : rExp.asIntegers()) {
					stringResult.add(Integer.toString(i));
				}
				return stringResult;
			}
			if (rExp.isNumeric()) {
				for (double d : rExp.asDoubles()) {
					stringResult.add(Double.toString(d));
				}
				return stringResult;
			}
			if (rExp.isString()) {
				for (String s : rExp.asStrings()) {
					stringResult.add(s);
				}
				return stringResult;
			}
	
			break;
			default:
		}
		
		// can end here with strange R scripts
		throw new ActivityConfigurationException(
				"Cannot convert R expression '" + rExp.toString()
						+ "' to the semantic type '" + symanticType.description
						+ "'");

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
