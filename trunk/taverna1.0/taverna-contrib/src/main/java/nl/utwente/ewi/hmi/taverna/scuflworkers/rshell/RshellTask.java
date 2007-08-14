/*
 * CVS
 * $Author: stain $
 * $Date: 2007-08-14 12:23:40 $
 * $Revision: 1.2 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellPortTypes.SymanticTypes;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.rosuda.JRclient.REXP;
import org.rosuda.JRclient.RFileInputStream;
import org.rosuda.JRclient.RFileOutputStream;
import org.rosuda.JRclient.RList;
import org.rosuda.JRclient.RSrvException;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A task to invoke a RProcessor. Connect to the Rserv by using JRclient, and
 * execute R script. Converts inputs and outputs as appropriate
 *
 * @author Stian Soiland, Ingo Wassink
 */
public class RshellTask implements ProcessorTaskWorker {
	private static int BUF_SIZE = 1024;

	private RshellProcessor processor;

	/**
	 * Constructor
	 *
	 * @param processor
	 *            the processor to be invoked
	 */
	public RshellTask(Processor processor) {
		this.processor = (RshellProcessor) processor;
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
	 * Output will be converted to a (list of) strings.
	 *
	 * @see org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker#execute(java.util.Map,
	 *      uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask)
	 *      @param workflowInputMap the input map of the processor @param
	 *      parentTask the parent task @return output map of the processor
	 */
	@SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map workflowInputMap, IProcessorTask parentTask)
			throws TaskExecutionException {

		Map<String, Object> outputs = new HashMap<String, Object>();
		RshellConnection connection = null;
		RshellConnectionSettings settings = processor.getConnectionSettings();

		// create connection
		try {
			connection = RshellConnectionManager.INSTANCE
					.createConnection(settings);
		} catch (Exception ex) {
			throw new TaskExecutionException(
					"Could not establish connection to " + settings.getHost()
							+ " using port " + settings.getPort() + ": "
							+ ex.getMessage());
		}

		try {
			// pass input form input ports to RServe
			InputPort[] inputPorts = processor.getInputPorts();
			for (InputPort inputPort : inputPorts) {
				RshellInputPort rshellInputPort = (RshellInputPort) inputPort;
				String inputName = rshellInputPort.getName();
				DataThing inputObject = (DataThing) workflowInputMap
						.get(inputName);
				if (inputObject == null) {
					throw new TaskExecutionException("Input to rserve '"
							+ inputName + "' was defined but not provided.");
				}

				Object input = inputObject.getDataObject();
				if (rshellInputPort.getSymanticType().isFile) {
					connection.assign(inputName,
							generateFilename(rshellInputPort));
					writeRServeFile(rshellInputPort, connection, input);
				} else {

					REXP value = javaToRExp(input, rshellInputPort
							.getSymanticType());
					if (value == null) {
						throw new TaskExecutionException(
								"Input to web service '" + inputName
										+ "' could not be interpreted as a "
										+ rshellInputPort.getSymanticType()
										+ ", it is a " + input.getClass());
					}
					connection.assign(inputName, value);
				}
			}

			// create filename variables for output ports that are files
			for (OutputPort outputPort : processor.getOutputPorts()) {
				RshellOutputPort rshellOutputPort = (RshellOutputPort) outputPort;
				if (rshellOutputPort.getSymanticType().isFile) {
	                connection.assign(outputPort.getName(),
							generateFilename(rshellOutputPort));
                }
			}

			// execute script
			String script = processor.getScript();
			if (script.length() != 0) {
	            connection.voidEval(script);
            }

			// create last statement for getting output for output ports
			OutputPort[] outputPorts = processor.getOutputPorts();
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

				assert (resultList.keys().length == outputPorts.length);
				for (OutputPort outputPort : outputPorts) {
					RshellOutputPort rshellOutputPort = (RshellOutputPort) outputPort;
					String portName = rshellOutputPort.getName();

					switch (rshellOutputPort.getSymanticType()) {
					case PNG_FILE: {
						byte[] data = (byte[]) readRServeFile(rshellOutputPort,
								connection);
						outputs.put(portName, DataThingFactory.bake(data));
						break;
					}
					case TEXT_FILE: {
						String data = (String) readRServeFile(rshellOutputPort,
								connection);
						outputs.put(portName, DataThingFactory.bake(data));
						break;
					}
					default: {
						REXP expression = resultList.at(portName);
						Object object = rExpToJava(expression, rshellOutputPort
								.getSymanticType());
						outputs.put(portName, DataThingFactory.bake(object));
					}
					}
				}
				break;
			}
			case REXP.XT_VECTOR: {
				if (outputPorts.length == 0) {
	                break;
                } else {
	                throw new TaskExecutionException("Unexpected result");
                }
			}

			default:
				throw new TaskExecutionException("Unexpected result");
			}

			// close the connection
			cleanUpServerFiles(connection);
			RshellConnectionManager.INSTANCE.releaseConnection(connection);
			connection = null;

		} catch (TaskExecutionException tee) {
			if (connection != null && connection.isConnected()) {
				cleanUpServerFiles(connection);
				RshellConnectionManager.INSTANCE.releaseConnection(connection);
				connection = null;
			}
			throw tee;
		} catch (RSrvException rSrvException) {
			if (connection != null && connection.isConnected()) {
				cleanUpServerFiles(connection);
				RshellConnectionManager.INSTANCE.releaseConnection(connection);
				connection = null;
			}

			throw new TaskExecutionException("RShell failed: "
					+ rSrvException.getRequestErrorDescription());
		}

		return outputs;

	}

	/**
	 * Method for cleaing up all generated files
	 *
	 * @param connection
	 *            the connection to be used
	 */
	private void cleanUpServerFiles(RshellConnection connection) {
		for (InputPort inputPort : processor.getInputPorts()) {
			RshellInputPort rshellInputPort = (RshellInputPort) inputPort;

			if (rshellInputPort.getSymanticType().isFile) {
				try {
					connection.removeFile(generateFilename(inputPort));
				} catch (RSrvException rse) {
					// do nothing, try to delete other files
				}
			}
		}

		for (OutputPort outputPort : processor.getOutputPorts()) {
			RshellOutputPort rshellInputPort = (RshellOutputPort) outputPort;

			if (rshellInputPort.getSymanticType().isFile) {
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
	 * @throws TaskExecutionException
	 *             if symantictype is not supported
	 */
	@SuppressWarnings("unchecked")
    private REXP javaToRExp(Object value, SymanticTypes symanticType)
			throws TaskExecutionException {
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
			throw new TaskExecutionException("Symantic type " + symanticType
					+ " not supported");
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
	private Object readRServeFile(RshellOutputPort outputPort,
			RshellConnection connection) throws TaskExecutionException {

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

			throw new TaskExecutionException("Cannot read file '" + filename
					+ "' from Rserve: " + ioe.getMessage());
		}

		Object value;
		switch (outputPort.getSymanticType()) {
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
			throws TaskExecutionException {

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
		throw new TaskExecutionException("Cannot convert R expression type '"
				+ rExp.getType() + "' to the semantic type '"
				+ symanticType.description + "'");

	}

	/**
	 * Helper method for rExpToJava for converting a list to a string value
	 *
	 * @param rExp
	 *            the rExp to be converted
	 * @return the string representation
	 */
	private String rListToString(REXP rExp) throws TaskExecutionException {
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
    private String rVectorToString(REXP rExp) throws TaskExecutionException {
		Vector elements = rExp.asVector();
		ArrayList<String> strings = new ArrayList<String>();
		for (Object object : elements) {
			strings.add((String) rExpToJava((REXP) object,
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
	private void writeRServeFile(RshellInputPort inputPort,
			RshellConnection connection, Object data)
			throws TaskExecutionException {

		RFileOutputStream outputStream = null;
		String filename = generateFilename(inputPort);

		try {

			byte[] bytes;
			switch (inputPort.getSymanticType()) {
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

			throw new TaskExecutionException("Cannot read file '" + filename
					+ "' from Rserve: " + ioe.getMessage());
		}
	}

}
