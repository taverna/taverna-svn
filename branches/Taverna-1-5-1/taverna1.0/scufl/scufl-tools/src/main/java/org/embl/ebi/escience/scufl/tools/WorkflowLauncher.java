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
 * Revision           $Revision: 1.4.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-01-31 14:25:43 $
 *               by   $Author: sowen70 $
 * Created on 16-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scufl.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * A utility class that wraps the process of executing a workflow, allowing
 * workflows to be easily executed independantly of the GUI.
 * 
 * @author Stuart Owen
 * @author Stian Soiland
 */

public class WorkflowLauncher {
	private ScuflModel model;

	private UserContext userContext;

	private String progressReportXML;

	/**
	 * Set <code>userContext</code> and instantiate the WorkflowLauncher,
	 * constructing an instance of the ScuflModel from the XML provided by the
	 * given input stream.
	 * 
	 * @param xmlStream
	 * @param userContext
	 *            a {@link UserContext}
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
	public WorkflowLauncher(InputStream xmlStream, UserContext userContext)
			throws ProcessorCreationException, DataConstraintCreationException,
			UnknownProcessorException, UnknownPortException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {	
		initialiseSPIRegistry();
		model = openWorkflowModel(xmlStream);
		this.userContext = userContext;
	}
	
	/**
	 * Set <code>userContext</code> and instantiate the WorkflowLauncher,
	 * constructing an instance of the ScuflModel from the XML provided by the
	 * given input stream.
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
	public WorkflowLauncher(InputStream xmlStream)
			throws ProcessorCreationException, DataConstraintCreationException,
			UnknownProcessorException, UnknownPortException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		this(xmlStream, null);
	}

	/**
	 * Instantiate the WorkflowLauncher, constructing an instance of the
	 * ScuflModel from the XML in the provided url
	 * 
	 * @param xmlFilename
	 * @param userContext
	 *            a {@link UserContext}
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
	public WorkflowLauncher(URL url, UserContext userContext)
			throws FileNotFoundException, ProcessorCreationException,
			IOException, DataConstraintCreationException,
			UnknownProcessorException, UnknownPortException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		this(url.openStream(), userContext);
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
	public WorkflowLauncher(URL url) throws FileNotFoundException,
			ProcessorCreationException, IOException,
			DataConstraintCreationException, UnknownProcessorException,
			UnknownPortException, DuplicateProcessorNameException,
			MalformedNameException, ConcurrencyConstraintCreationException,
			DuplicateConcurrencyConstraintNameException, XScuflFormatException {
		this(url, null);
	}

	/**
	 * Instantiate the WorkflowLauncher with a direct reference to the
	 * ScuflModel to be executed.
	 * 
	 * @param model
	 *            a {@link ScuflModel}
	 */
	public WorkflowLauncher(ScuflModel model) {		
		initialiseSPIRegistry();
		this.model = model;
	}

	/**
	 * Instantiate the WorkflowLauncher with a direct reference to the
	 * ScuflModel to be executed, together with a UserContext for the user
	 * executing this model.
	 * 
	 * @param model
	 *            a {@link ScuflModel}
	 * @param userContext
	 *            a {@link UserContext}
	 */
	public WorkflowLauncher(ScuflModel model, UserContext userContext) {
		initialiseSPIRegistry();
		this.model = model;
		this.userContext = userContext;
	}

	private ScuflModel openWorkflowModel(InputStream xmlStream)
			throws ProcessorCreationException, DataConstraintCreationException,
			UnknownProcessorException, UnknownPortException,
			DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException,
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

	public Map execute(Map inputs) throws WorkflowSubmissionException,
			InvalidInputException {
		
		EnactorProxy enactor = FreefluoEnactorProxy.getInstance();

		final WorkflowInstance workflowInstance = enactor.compileWorkflow(
				model, inputs, userContext);

		final Object lock = new Object();

		WorkflowEventListener completionListener = new WorkflowEventAdapter() {
			public void workflowCompleted(WorkflowCompletionEvent e) {
				if (e.getWorkflowInstance() == workflowInstance) {
					synchronized (lock) {
						lock.notifyAll();
					}
				}
			}
			public void workflowFailed(WorkflowFailureEvent e) {
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};
		WorkflowEventDispatcher.DISPATCHER.addListener(completionListener);
		
		try {
			workflowInstance.run();
			synchronized (lock) {
				try {
					// We'll wait here until workflowCompleted or workflowFailed
					// happens
					lock.wait();
				} catch (InterruptedException e) {
				}
			}		
		} finally {
			progressReportXML = workflowInstance.getProgressReportXMLString();
		}
		
		return workflowInstance.getOutput();
	}

	/**
	 * Executes the workflow with the provided inputs (which is a Map of
	 * DataThings) and <code>userContext</code>. Returns the outputs of the
	 * workflow (which is also a Map of DataThings). An array of
	 * workflowEventListeners can be provided to allow handling of events as the
	 * workflow is executed.
	 * 
	 * @param inputs
	 * @param workflowEventListeners
	 * @return
	 * @throws WorkflowSubmissionException
	 * @throws InvalidInputException
	 * @see org.embl.ebi.escience.baclava.DataThing
	 * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener
	 */
	public Map execute(Map inputs,
			WorkflowEventListener[] workflowEventListeners)
			throws WorkflowSubmissionException, InvalidInputException {
		initialiseSPIRegistry();
		for (int i = 0; i < workflowEventListeners.length; i++) {
			WorkflowEventDispatcher.DISPATCHER
					.addListener(workflowEventListeners[i]);
		}

		return execute(inputs);
	}

	/**
	 * Executes the workflow with the provided inputs (which is a Map of
	 * DataThings) and <code>userContext</code>. Returns the outputs of the
	 * workflow (which is also a Map of DataThings). A workflowEventListener can
	 * be provided to allow handling of events as the workflow is executed.
	 * 
	 * @param inputs
	 * @param workflowEventListener
	 * @return
	 * @throws WorkflowSubmissionException
	 * @throws InvalidInputException
	 * @see org.embl.ebi.escience.baclava.DataThing
	 * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener
	 */
	public Map execute(Map inputs, WorkflowEventListener workflowEventListener)
			throws WorkflowSubmissionException, InvalidInputException {
		WorkflowEventDispatcher.DISPATCHER.addListener(workflowEventListener);
		return execute(inputs);

	}
	
	private void initialiseSPIRegistry() {
		Repository repository;
		try {
			LocalArtifactClassLoader acl = (LocalArtifactClassLoader) getClass()
					.getClassLoader();
			repository = acl.getRepository();						
		} catch (ClassCastException cce) {
			// Running from outside of Raven - won't expect this to work
			// properly!			
			repository = LocalRepository.getRepository(new File(
					Bootstrap.TAVERNA_CACHE));			
			for (URL remoteRepository : Bootstrap.remoteRepositories) {
				repository.addRemoteRepository(remoteRepository);
			}					
		}				
		TavernaSPIRegistry.setRepository(repository);
	}
	
	private WorkflowLauncher(String [] args)  throws MalformedURLException {
//		 For compatability with old-style code using System.getProperty("taverna.*")
		MyGridConfiguration.loadMygridProperties();
		
		// Return code to exit with, normally 0
		int error = 0;

		// Current directory is from where files are read if not otherwise
		// specified
		URL here = new URL("file:");
		
		initialiseSPIRegistry();		
		
		// Construct command line options
		Option helpOption = new Option("help", "print this message");
		// Option version = new Option("version",
		// "print the version information and exit");
		// Option quiet = new Option("quiet", "be extra quiet");
		// Option verbose = new Option("verbose", "be extra verbose");
		// Option debug = new Option("debug", "print debugging information");
		Option outputOption = OptionBuilder
				.withArgName("directory")
				.hasArg()
				.withDescription(
						"save outputs as files in directory, default "
								+ "is to make a new directory workflowName_output")
				.create("output");
		Option outputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("save outputs to a new XML document").create(
						"outputdoc");
		Option reportOption = OptionBuilder.withArgName("file").hasArg()
				.withDescription(
						"save progress report in file, default is "
								+ "progressReport.xml in the output directory")
				.create("report");
		Option inputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("load inputs from XML document").create(
						"inputdoc");

		Option inputOption = OptionBuilder.withArgName("name filename")
				.hasArgs(2).withValueSeparator('=').withDescription(
						"load the named input from file or URL")
				.create("input");

		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inputOption);
		options.addOption(inputdocOption);
		options.addOption(outputOption);
		options.addOption(outputdocOption);
		options.addOption(reportOption);

		CommandLineParser parser = new GnuParser();
		CommandLine line;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(1);
			return;
		}

		if (line.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("executeworkflow <workflow> [..]\n"
					+ "Execute workflow and save outputs. "
					+ "Inputs can be specified by multiple "
					+ "-input options, or loaded from an "
					+ "XML input document as saved from "
					+ "Taverna. By default, a new directory "
					+ "is created named workflow.xml_output "
					+ "unless the -output or -outputdoc"
					+ "options are given. All files to be read "
					+ "can be either a local file or an URL.", options);
			System.exit(0);
		}

		Map inputs = new HashMap();
		if (line.hasOption("inputdoc")) {
			File inputDoc = new File(line.getOptionValue("inputdoc"));
			try {
				inputs = loadInputDoc(inputDoc);
			} catch (JDOMException e) {
				System.err.println("Could not parse input document " + inputDoc + ": " + e.getMessage());
				System.exit(13);
			} catch (IOException e) {
				System.err.println("Could not open input document " + inputDoc + ": " + e.getMessage());
				System.exit(14);
			}
		}
		
		if (line.hasOption("input")) {
			String[] inputParams = line.getOptionValues("input");
			for (int i = 0; i < inputParams.length; i = i + 2) {
				String inputName = inputParams[i];
				try {
					URL inputURL = new URL(here, inputParams[i + 1]);
					inputs.put(inputName, loadDataThing(inputURL));
				} catch (IndexOutOfBoundsException e) {
					System.err.println("Missing input filename for input "
							+ inputName);
					System.exit(2);
				} catch (IOException e) {
					System.err.println("Could not read input " + inputName
							+ ": " + e.getMessage());
					System.exit(3);
				}
			}
		}

		if (line.getArgs().length != 1) {
			System.err.println("One and only one workflow can be specified");
			System.exit(4);
			return;
		}
		URL workflowURL = new URL(here, line.getArgs()[0]);

		File outputDir;
		if (line.hasOption("output")) {
			outputDir = new File(line.getOptionValue("output"));
		} else {
			// We'll name it after our workflow file name
			String workflowPath = new URL(workflowURL, ".").getPath();			
			// Remove the directory part, ie. a nasty basename() on URLs
			String[] workflowPaths = workflowURL.getFile().split("/");
			String workflowName = workflowPaths[workflowPaths.length-1];	
			String outputName = workflowName + "_output";
			if (workflowURL.getProtocol().equals("file")) {
				// Store it with the workflow
				outputDir = new File(workflowPath, outputName);
			} else {
				// Store it in our current directory
				outputDir = new File(outputName);
			}
		}
		// Make sure the output directory exists
		if (!outputDir.isDirectory()) {
			if (!outputDir.mkdirs()) {
				System.err.println("Could not create output directory "
						+ outputDir);
				System.exit(5);
			}
		}

		File outputDoc = null;
		if (line.hasOption("outputdoc")) {
			outputDoc = new File(line.getOptionValue("outputdoc"));
		}

		File reportName;
		if (line.hasOption("report")) {
			reportName = new File(line.getOptionValue("report"));
		} else {
			reportName = new File(outputDir, "progressReport.xml");
		}

		// PUH! Ok, all the file stuff set up, let's get to work		
		Map outputs;
		WorkflowLauncher launcher;
		try {
			launcher = new WorkflowLauncher(workflowURL);
		} catch (IOException e) {
			System.err.println("Could not read workflow " + workflowURL + ": "
					+ e.getMessage());
			System.exit(6);
			return;
		} catch (XScuflFormatException e) {
			System.err.println("Could not parse workflow " + workflowURL + ": "
					+ e.getMessage());
			System.exit(15);
			return;			
		} catch (ScuflException e) {
			System.err.println("Could not load workflow " + workflowURL + ": "
					+ e);
			System.exit(7);
			return;
		}

		try {
			outputs = launcher.execute(inputs);
		} catch (InvalidInputException e) {
			System.err.println("Invalid inputs for workflow " + workflowURL
					+ ": " + e);
			System.exit(8);
			return;
		} catch (WorkflowSubmissionException e) {
			System.err.println("Could not execute workflow " + workflowURL
					+ ": " + e);
			System.exit(9);
			return;
		}
		// After this point, the workflow HAS executed, and we should not exit
		// until the end. Set error instead of System.exit();

		String report = launcher.getProgressReportXML();
		try {
			FileUtils.writeStringToFile(reportName, report, "utf8");
		} catch (IOException e) {
			System.err.println("Could not save progress report " + reportName
					+ ": " + e);
			error = 10;
		}

		/**
		 * If --outputdoc is given, avoid writing the flat output files unless
		 * --output is also given.
		 */
		if (line.hasOption("output") || !line.hasOption("outputdoc")) {
			try {
				saveOutputs(outputs, outputDir);
			} catch (IOException e) {
				System.err.println("Could not save outputs to " + outputDir
						+ ": " + e);
				error = 11;
			}
		}

		if (outputDoc != null) {
			try {
				saveOutputDoc(outputs, outputDoc);
			} catch (IOException e) {
				System.err.println("Could not save output document "
						+ outputDoc);
				error = 12;
			}
		}

		// Make sure we actually exit because some threads could be hanging
		// around
		System.exit(error);
	}

	/**
	 * Allows a workflow to be executed stand-alone. Usage: <workflowURL> [<inputName>
	 * <inputDataURL>] ...
	 * 
	 * @param args
	 * @throws MalformedURLException
	 */
	@SuppressWarnings({ "deprecation", "static-access" })
	public static void main(String args[]) throws MalformedURLException {
		new WorkflowLauncher(args);
	}

	private static Map loadInputDoc(File file) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();		
		Document inputDoc = builder.build(new FileReader(file));		
		return DataThingXMLFactory.parseDataDocument(inputDoc);				
	}

	/**
	 * Save workflow outputs as an DataThing XML document 
	 * 
	 * @param outputs Map of DataThing to save
	 * @param outputDoc File to write the document
	 * @throws IOException If the file cannot be written
	 */
	private static void saveOutputDoc(Map outputs, File outputDoc)
			throws IOException {
		Document doc = DataThingXMLFactory.getDataDocument(outputs);
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		String xmlString = xo.outputString(doc);		
		FileUtils.writeStringToFile(outputDoc, xmlString, "utf8");
	}

	/**
	 * Save workflow outputs as a directory structure. One file will be created
	 * for each output, named the same as the output. If the output is a list, a
	 * directory will be created instead, and the content will be files or
	 * directories named 1, 2, etc.
	 * 
	 * @param outputs
	 *            Map of DataThing to save
	 * @param outputDir
	 *            Base directory for structure
	 * @throws IOException
	 */
	private static void saveOutputs(Map outputs, File outputDir)
			throws IOException {
		if (outputs == null) {
			return;
		}
		for (Iterator iterator = outputs.keySet().iterator(); iterator
				.hasNext();) {
			String outputName = (String) iterator.next();
			DataThing thing = (DataThing) outputs.get(outputName);
			DataThing.writeObjectToFileSystem(outputDir, outputName, thing
					.getDataObject(), "");
		}
	}

	private static DataThing loadDataThing(URL dataURL) throws IOException {
		String data = IOUtils.toString(dataURL.openStream(), "utf8");
		return new DataThing(data);
	}
}
