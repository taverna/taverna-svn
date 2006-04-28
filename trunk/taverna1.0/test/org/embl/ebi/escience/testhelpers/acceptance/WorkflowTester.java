package org.embl.ebi.escience.testhelpers.acceptance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.utils.SimpleFile;

public class WorkflowTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args[0] == "-h" || args[0] == "--help") {
			System.out.println("Usage: WorkflowTester <directory>");
			System.out.println("Test all workflows in directory. Each workflow should be in a separate");
			System.out.println("directory named the same as the directory, and (optional) ");
			System.out.println("subdirectories for inputs and outputs.");
			System.out.println("Example:");
			System.out.println("  MegaWorkFlow/");
			System.out.println("  MegaWorkFlow/MegaWorkFlow.xml");
			System.out.println("  MegaWorkFlow/inputs");
			System.out.println("  MegaWorkFlow/inputs/numbers.txt");
			System.out.println("  MegaWorkFlow/outputs");
			System.out.println("  MegaWorkFlow/outputs/processed.txt");
			System.out.println("  MegaWorkFlow/outputs/report.txt");
			System.out.println("Here the workflow MegaWorkFlow.xml takes one input 'numbers', loaded from");
			System.out.println("numbers.txt, and the two outputs 'processed' and 'report' should match");
			System.out.println("the respective file contents.");
			System.exit(1);
		}
		
		File directory = new File(args[0]);
		if (! directory.isDirectory()) {
			System.err.println("Not a directory: " + directory);
			System.exit(2);			
		}
		examineDirectories(directory);
	}
	
	public static void examineDirectories(File directory) {
		final String workflowName = directory.getName() + ".xml";
		System.out.println("Looking for " + workflowName);
		File[] matches =  directory.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.equals(workflowName);				
			}			
		});
		if (matches == null) {
			System.err.println("Could not list directory: " + directory);
			return;
		}
		if (matches.length > 0) {
			// We found the workflow, process it!
			File workflowFile;
			try {
				// Resolve any symlinks etc.
				workflowFile = matches[0].getCanonicalFile();
			} catch (IOException e) {
				System.err.println("Could not resolve path: " + matches[0]);				
				return;				
			}
			testWorkflow(workflowFile);
		} else {
			// Check out any subdirs
			File[] subdirs = directory.listFiles();			
			for (int i=0; i<subdirs.length; i++) {
				if (! subdirs[i].isDirectory()) {
					continue;			
				}
				examineDirectories(subdirs[i]);
			}
		}
	}

	public static void testWorkflow(File workflowFile) {
		File parent = workflowFile.getParentFile();
		File outputDir = new File(parent, "outputs");
		File inputDir = new File(parent, "inputs");
		Map inputs = readFiles(inputDir);
		Map expectedOutputs = readFiles(outputDir);
		InputStream workflowStream;
		try {
			workflowStream = new FileInputStream(workflowFile);
		} catch (FileNotFoundException e) {			
			System.err.println("Could not open workflow: " + workflowFile);
			return;
		}
		WorkflowLauncher launcher;
		try {
			launcher = new WorkflowLauncher(workflowStream);
		} catch (Exception e) {
			System.err.println("Could not load workflow: " + workflowFile);
			e.printStackTrace();
			return;
		}
		Map outputs;
		try {
			// FIXME: Should be a map of DataThings
			outputs = launcher.execute(inputs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Could not execute workflow: " + workflowFile);
			e.printStackTrace();
			return;
		}
		// Check if the outputs are as expected
		checkOutputs(outputs, expectedOutputs);
	
	}


	protected static void checkOutputs(Map outputs, Map expectedOutputs) {
		Set outputSet = outputs.keySet();
		Set expectedOutputSet = expectedOutputs.keySet();
		Set missing = new HashSet(expectedOutputSet);
		missing.removeAll(outputSet);
		Set extra = new HashSet(outputSet);
		extra.removeAll(expectedOutputSet);
		for (Iterator it=missing.iterator(); it.hasNext();) {
			System.out.println("Missing output: " + it.next());			
		}
		for (Iterator it=extra.iterator(); it.hasNext();) {
			System.out.println("Unexpected output: " + it.next());			
		}
		// Check outputs we know about
		outputSet.retainAll(expectedOutputSet);	
		for (Iterator it=outputSet.iterator(); it.hasNext();) {
			String outputName = (String) it.next();
			DataThing thing = (DataThing)outputs.get(outputName);
			String expected = ((String) expectedOutputs.get(outputName)).trim();			
			// FIXME: DataThing might not contain a string!
			String got = ((String) thing.getDataObject()).trim();	
			if (! got.matches(expected)) {
				System.out.println("Output " + outputName + " not as expected");
				System.out.println("Expected:" + expected);
				System.out.println("Got:" + got);
			}
		}
	}

	/**
	 * Read all files in the given directory.
	 * <p>
	 * Each file in the directory is read into memory, and the data is returned
	 * in a map. The keys are the filenames, but without the extension.
	 * <p>
	 * Example: A directory contains the files "names.txt" and "numbers.txt".
	 * The returned map will have the keys "names" and "numbers", with the
	 * values being a String of the whole file contents.
	 * 
	 * @param directory
	 *            Where to find files
	 * @return A map of the files' content as Strings accessed by the simplified
	 *         filenames
	 */
	public static Map readFiles(File directory) {
		Map inputs = new HashMap();
		File[] files = directory.listFiles();
		if (files == null) {
			return inputs;
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (!file.isFile()) {
				continue;
			}
			// Remove extension, usually .txt
			String inputName = file.getName().replaceAll("[.][^.]*", "");
			if (inputName.equals("")) {
				// ignore files named .fish etc.
				continue;
			}
			if (inputs.containsKey(inputName)) {
				// Probably two files with different extensions
				System.err.println("Skipping duplicate input " + inputName);
				continue;
			}
			String input;
			try {
				input = SimpleFile.readFile(file);
			} catch (IOException e) {
				System.err.println("Could not read input file: " + file);
				continue;
			} 
			inputs.put(inputName, input);
		}
		return inputs;
	}
}
