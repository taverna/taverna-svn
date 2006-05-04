package org.embl.ebi.escience.testhelpers.acceptance;

import java.io.File;
import java.io.FilenameFilter;

import junit.extensions.jfunc.JFuncSuite;
import junit.extensions.jfunc.textui.JFuncRunner;
import junit.framework.Test;

import org.apache.log4j.Logger;

public class WorkflowTester {

	private static Logger logger = Logger.getLogger(WorkflowTester.class);
	
	public static Test suite() {		
		String home = System.getProperty("taverna.home");
		if (home==null) {
			logger.fatal("Unable to open workflow - taverna.home is not set.");
			return null;			
		}		
		return examineDirectories(new File(home + "/test/data/workflows/"));
	}
	
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
		JFuncSuite suite = examineDirectories(directory);		
		JFuncRunner.run(suite);
		System.exit(0);
	}
	
	/**
	 * Walk through directory and find workflows. Create a WorkflowTest for each
	 * workflow.
	 * 
	 * Each workflow should be in a separate directory, and named the same as
	 * the directory, but with the extension .xml. Directories "inputs" and
	 * "outputs" should specify the inputs and the expected outputs.
	 * 
	 * @param directory Base directory for workflows to be tested
	 * @return A TestSuite of all WorkflowTest
	 */
	public static JFuncSuite examineDirectories(File directory) {		
		JFuncSuite suite = new JFuncSuite();			
		// See if we find directory/directory.xml here, which should be a workflow
		final String workflowName = directory.getName() + ".xml";
		File[] matches =  directory.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.equals(workflowName);				
			}			
		});
		if (matches == null) {
			System.err.println("Could not list directory: " + directory);
			return suite;
		}
		if (matches.length > 0) {
			// We found a workflow!			
			WorkflowTest test = (WorkflowTest) suite.getTestProxy(new WorkflowTest());
			try {
				test.testWorkflow(directory);
			} catch (Exception e) {
				logger.error("Could not build test case", e);
			}			
		} else {
			// Check out any subdirs
			File[] subdirs = directory.listFiles();			
			for (int i=0; i<subdirs.length; i++) {
				if (! subdirs[i].isDirectory()) {
					continue;			
				}
				suite.addTest(examineDirectories(subdirs[i]));
			}			
		}
		return suite;
	}


}
