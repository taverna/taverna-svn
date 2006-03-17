/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WorkflowLauncher.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-03-17 14:54:27 $
 *               by   $Author: sowen70 $
 * Created on 16-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scufl.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * A utility class that wraps the process of executing a workflow, allowing
 * workflows to be easily executed independantly of the GUI.
 * 
 * @author Stuart Owen
 */

public class WorkflowLauncher {
	private ScuflModel model;

	private String progressReportXML;

	/**
	 * Instantiate the WorkflowLauncher, constructing an instance of the
	 * ScuflModel from the XML provided by the given input stream.
	 * 
	 * @param xmlStream
	 * @throws ProcessorCreationException
	 * @throws DataConstraintCreationException
	 * @throws UnknownProcessorException
	 * @throws UnknownPortException
	 * @throws DuplicateProcessorNameException
	 * @throws MalformedNameException
	 * @throws ConcurrencyConstraintCreationException
	 * @throws DuplicateConcurrencyConstraintNameException
	 * @throws XScuflFormatException
	 */
	public WorkflowLauncher(InputStream xmlStream) throws ProcessorCreationException, DataConstraintCreationException,
			UnknownProcessorException, UnknownPortException, DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException, DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		model = openWorkflowModel(xmlStream);
	}

	/**
	 * Instantiate the WorkflowLauncher, constructing an instance of the
	 * ScuflModel from the XML in the provided url
	 * 
	 * @param xmlFilename
	 * @throws FileNotFoundException
	 * @throws ProcessorCreationException
	 * @throws DataConstraintCreationException
	 * @throws UnknownProcessorException
	 * @throws UnknownPortException
	 * @throws DuplicateProcessorNameException
	 * @throws MalformedNameException
	 * @throws ConcurrencyConstraintCreationException
	 * @throws DuplicateConcurrencyConstraintNameException
	 * @throws XScuflFormatException
	 */
	public WorkflowLauncher(URL url) throws FileNotFoundException, ProcessorCreationException, IOException,
			DataConstraintCreationException, UnknownProcessorException, UnknownPortException,
			DuplicateProcessorNameException, MalformedNameException, ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		model = openWorkflowModel(url.openStream());
	}

	private ScuflModel openWorkflowModel(InputStream xmlStream) throws ProcessorCreationException,
			DataConstraintCreationException, UnknownProcessorException, UnknownPortException,
			DuplicateProcessorNameException, MalformedNameException, ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		ScuflModel model = new ScuflModel();
		XScuflParser.populate(xmlStream, model, null);
		return model;
	}

	protected ScuflModel getModel() {
		return model;
	}

	/**
	 * Provides access to the progress report after the workflow has be executed
	 * 
	 * @return progress report
	 */
	public String getProgressReportXML() {
		return progressReportXML;
	}

	public Map execute(Map inputs) throws WorkflowSubmissionException, InvalidInputException {
		return this.execute(inputs, null);
	}

	/**
	 * Executes the workflow with the provided inputs (which is a Map of
	 * DataThings) Returns the outputs of the workflow (which is also a Map of
	 * DataThings). A workflowEventListener can be provided to allow handling of
	 * events as the workflow is executed, or can be ommitted by passing 'null'
	 * 
	 * @param inputs
	 * @param workflowEventListener
	 * @return
	 * @throws WorkflowSubmissionException
	 * @throws InvalidInputException
	 * @see org.embl.ebi.escience.baclava.DataThing
	 * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener
	 */
	public Map execute(Map inputs, WorkflowEventListener workflowEventListener) throws WorkflowSubmissionException,
			InvalidInputException {

		if (workflowEventListener != null) {
			WorkflowEventDispatcher.DISPATCHER.addListener(workflowEventListener);
		}

		EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
		WorkflowInstance workflowInstance = enactor.compileWorkflow(model, inputs, null);
		try {
			workflowInstance.run();
			String status = workflowInstance.getStatus();

			while (!status.equals("COMPLETE") && !status.equals("FAILED")) {
				status = workflowInstance.getStatus();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (InvalidInputException e) {
			throw e;
		} finally {
			progressReportXML = workflowInstance.getProgressReportXMLString();
		}

		return workflowInstance.getOutput();
	}

	/**
	 * Allows a workflow to be executed stand-alone. Usage: <workflowURL> [<inputName>
	 * <inputDataURL>] ...
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		Map inputs = new HashMap();
		Map outputs = null;

		if (args.length % 2 != 1) {
			System.err.print("Usage: <workflowFilename> [<inputName> <inputDataURL>] ...");
			System.exit(-1);
		}

		try {
			URL workflowURL = new URL(args[0]);

			if (args.length > 1) {
				for (int i = 1; i < args.length; i += 2) {
					String inputName = args[i];
					inputs.put(inputName, loadDataThing(new URL(args[i + 1])));
				}
			}

			WorkflowLauncher launcher = new WorkflowLauncher(workflowURL);
			try {
				outputs = launcher.execute(inputs, null);
			} catch (Exception e) {
				System.err.println("Error executing workflow: " + e.getMessage());
			}

			saveResults(outputs, workflowURL, launcher.getProgressReportXML());

		} catch (Exception e) {
			System.err.println("Error executing workflow: " + e.getMessage());
		}
		System.exit(0);
	}

	private static void saveResults(Map outputs, URL workflowURL, String progressReport) throws IOException {
		String workflowOutputDir = "";
		if (workflowURL.getFile().lastIndexOf(File.separatorChar) != -1) {
			workflowOutputDir = workflowURL.getFile().substring(
					workflowURL.getFile().lastIndexOf(File.separatorChar) + 1);
		} else {
			workflowOutputDir = workflowURL.getFile();
		}
		workflowOutputDir += "_output";

		if (outputs != null) {
			for (Iterator iterator = outputs.keySet().iterator(); iterator.hasNext();) {
				String outputName = (String) iterator.next();
				DataThing thing = (DataThing) outputs.get(outputName);
				DataThing.writeObjectToFileSystem(new File(workflowOutputDir), outputName, thing.getDataObject(),
						"");
			}
		}
		DataThing.writeObjectToFileSystem(new File(workflowOutputDir), "progressReport", progressReport, ".xml");
	}

	private static DataThing loadDataThing(URL dataURL) throws Exception {
		String line;
		String data = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(dataURL.openStream()));
		while ((line = reader.readLine()) != null) {
			data += line;
			data += "\n";
		}

		return new DataThing(data);
	}
}
