/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.rdg.resc.jstyx.client.CStyxFile;
import uk.ac.rdg.resc.jstyx.client.StyxConnection;
import uk.ac.rdg.resc.jstyx.client.StyxFileInputStream;
import uk.ac.rdg.resc.jstyx.client.StyxFileInputStreamReader;
import uk.ac.rdg.resc.jstyx.client.StyxFileOutputStream;
import uk.ac.rdg.resc.jstyx.client.StyxFileOutputStreamWriter;
import uk.ac.soton.itinnovation.freefluo.core.task.Task;
import uk.ac.soton.itinnovation.taverna.enactor.entities.PortTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Invokes a single operation within an Inferno SGS
 * 
 * @author Tom Oinn
 */
public class InfernoTask implements ProcessorTaskWorker {

	private InfernoProcessor processor;

	public InfernoTask(Processor p) {
		this.processor = (InfernoProcessor) p;
	}

	static {
		System.setProperty("java.protocol.handler.pkgs",
				"uk.ac.rdg.resc.jstyx.client.protocol");
	}

	public Map execute(Map inputMap, ProcessorTask parentTask)
			throws TaskExecutionException {
		Map results = new HashMap();
		try {

			// Detect whether the processor outputs for literal values
			// are bound in the current workflow
			boolean returnString = false;
			boolean returnBytes = false;
			boolean returnRef = false;
			for (Iterator i = parentTask.getChildren().iterator(); i.hasNext();) {
				Task task = (Task) i.next();
				if (task instanceof PortTask) {
					PortTask outputPortTask = (PortTask) task;
					String portName = outputPortTask.getScuflPort().getName();
					if (portName.equals("stringOut")) {
						returnString = true;
					}
					if (portName.equals("binaryOut")) {
						returnBytes = true;
					}
					if (portName.equals("refStdOut")
							|| portName.equals("refStdErr")) {
						returnRef = true;
					}
				}
			}

			if (returnRef & (returnBytes | returnString)) {
				throw new TaskExecutionException(
						"Cannot return both references and literal values, invalid workflow.");
			}
			if (returnBytes & returnString) {
				throw new TaskExecutionException(
						"Cannot return both string and byte[] values, must only bind to one.");
			}

			// Connect to the Styx session
			// StyxClientSession session =
			// StyxClientSession.createSession(processor.getHost(),
			// processor.getPort());
			// session.connect();
			StyxConnection session = new StyxConnection(processor.getHost(),
					processor.getPort());

			// Create a new instance of the service
			CStyxFile cloneFile = new CStyxFile(session, processor.getService()
					+ "/clone");
			StyxFileInputStream cloneIn = new StyxFileInputStream(cloneFile);
			BufferedReader bufCloneIn = new BufferedReader(
					new StyxFileInputStreamReader(cloneIn));
			String instanceID = bufCloneIn.readLine();
			System.out.println("Got instanceID : " + instanceID);
			bufCloneIn.close();
			cloneFile.close();

			// Flag to indicate whether the input has been defined, attempts
			// to set any input (stream, text or binary literals) when this
			// is true will throw an exception
			boolean definedInput = false;

			if (inputMap.containsKey("refIn")) {
				ensureFalse(definedInput);
				// Set a URL to read data to stdin from
				CStyxFile urlFile = new CStyxFile(session, processor
						.getService()
						+ "/instances/" + instanceID + "/io/in/stdin");
				StyxFileOutputStream urlOut = new StyxFileOutputStream(urlFile);
				BufferedWriter bufUrlOut = new BufferedWriter(
						new StyxFileOutputStreamWriter(urlOut));
				// Get the URL value from the datathing in the input
				String inputURL = (String) ((DataThing) inputMap.get("refIn"))
						.getDataObject();
				bufUrlOut.write("readfrom " + inputURL);
				bufUrlOut.flush();
				bufUrlOut.close();
				definedInput = true;
				urlFile.close();
				System.out.println("Sent reference " + inputURL);
			}

			// Send params if any
			if (inputMap.containsKey("params")) {
				String parameterString = (String) ((DataThing) inputMap
						.get("params")).getDataObject();

				CStyxFile ctlFile = new CStyxFile(session, processor
						.getService()
						+ "/instances/" + instanceID + "/params");
				StyxFileOutputStream ctlOut = new StyxFileOutputStream(ctlFile);
				BufferedWriter bufCtlOut = new BufferedWriter(
						new StyxFileOutputStreamWriter(ctlOut));
				bufCtlOut.write(parameterString);
				bufCtlOut.flush();
				bufCtlOut.close();
				ctlFile.close();
				System.out.println("Sent parameters");
			}

			// Start the service
			CStyxFile ctlFile = new CStyxFile(session, processor.getService()
					+ "/instances/" + instanceID + "/ctl");
			StyxFileOutputStream ctlOut = new StyxFileOutputStream(ctlFile);
			BufferedWriter bufCtlOut = new BufferedWriter(
					new StyxFileOutputStreamWriter(ctlOut));
			bufCtlOut.write("start");
			bufCtlOut.flush();
			bufCtlOut.close();
			ctlFile.close();
			System.out.println("Sent start signal");

			if (inputMap.containsKey("stringIn")
					|| inputMap.containsKey("binaryIn")) {
				CStyxFile inFile = new CStyxFile(session, processor
						.getService()
						+ "/instances/" + instanceID + "/io/in/stdin");
				StyxFileOutputStream valueOut = new StyxFileOutputStream(inFile);
				if (inputMap.containsKey("stringIn")) {
					// Send the value of the stringIn input into the /io/in file
					ensureFalse(definedInput);
					BufferedWriter bufValueOut = new BufferedWriter(
							new StyxFileOutputStreamWriter(valueOut));
					String stringValue = (String) ((DataThing) inputMap
							.get("stringIn")).getDataObject();
					bufValueOut.write(stringValue);
					bufValueOut.flush();
					bufValueOut.close();
					definedInput = true;
					System.out.println("Sent string value");
				}
				if (inputMap.containsKey("binaryIn")) {
					// Send the byte[] value of the binaryIn input into the
					// /io/in file
					ensureFalse(definedInput);
					BufferedOutputStream bos = new BufferedOutputStream(
							valueOut);
					byte[] binaryValue = (byte[]) ((DataThing) inputMap
							.get("binaryIn")).getDataObject();
					bos.write(binaryValue);
					bos.flush();
					bos.close();
					definedInput = true;
					System.out.println("Sent binary value");
				}
				// Send an empty string
				StyxFileOutputStream endLine = new StyxFileOutputStream(inFile);
				BufferedWriter bufEndLine = new BufferedWriter(
						new StyxFileOutputStreamWriter(endLine));
				bufEndLine.write("");
				bufEndLine.flush();
				bufEndLine.close();
				inFile.close();
			}

			// Write the base URL out
			String baseURL = "styx://" + processor.getHost() + ":"
					+ processor.getPort() + "/" + processor.getService()
					+ "/instances/" + instanceID + "/";
			results.put("baseURL", new DataThing(baseURL));
			if (returnRef) {
				results.put("refStdOut", new DataThing(baseURL
						+ "io/out/stdout"));
				results.put("refStdErr", new DataThing(baseURL
						+ "io/out/stderr"));
				System.out.println("Returned references");
				session.close();
			} else if (returnString | returnBytes) {
				// Return actual value
				CStyxFile resultFile = new CStyxFile(session, processor
						.getService()
						+ "/instances/" + instanceID + "/io/out/stdout");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// Read bytes from the file directly
				boolean done = false;
				int offset = 0;
				while (!done) {
					ByteBuffer bytes = resultFile.read(offset);
					System.out.println("Fetched bytes from " + offset + " to "
							+ (offset + bytes.remaining()));
					if (bytes.hasRemaining() == false) {
						done = true;
					}
					while (bytes.hasRemaining()) {
						baos.write(bytes.get());
						offset++;
					}
				}

				// StyxFileInputStream sfis = new
				// StyxFileInputStream(resultFile);

				// URL resultURL = new URL(baseURL+"io/out");
				// InputStream uis = resultURL.openStream();
				// BufferedInputStream bis = new BufferedInputStream(sfis);

				// System.out.println("Created file input stream to get literal
				// results");
				// ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// int b;
				// int count = 0;
				// while ((b = bis.read()) != -1) {
				// count++;
				// baos.write(b);
				// }
				// System.out.println(count+" bytes read from
				// "+baseURL+"io/out");
				// bis.close();
				baos.flush();
				// Okay to close then read, behaves correctly according to J2SE
				// spec
				baos.close();
				if (returnBytes) {
					results.put("binaryOut", new DataThing(baos.toByteArray()));
				} else {
					results.put("stringOut", new DataThing(baos.toString()));
				}
				resultFile.close();
				session.close();
			}

			return results;
		} catch (Exception ex) {
			if (ex instanceof TaskExecutionException) {
				throw (TaskExecutionException) ex;
			}
			TaskExecutionException tee = new TaskExecutionException(
					"Problem invoking inferno SGS : " + ex.getMessage());
			tee.initCause(ex);
			throw tee;
		}
	}

	static void ensureFalse(boolean flag) throws TaskExecutionException {
		if (flag) {
			throw new TaskExecutionException(
					"Input may consist of at most one of url, string value or binary value");
		}
	}

}
