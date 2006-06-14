package org.embl.ebi.escience.testhelpers.acceptance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ConcurrencyConstraintCreationException;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateConcurrencyConstraintNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.tools.Lang;
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;
import org.embl.ebi.escience.utils.SimpleFile;

import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * Workflow acceptance testing
 * 
 * Test that a workflow runs and that the outputs are as expected.
 * 
 * @author Stian Soiland
 * 
 */
public class WorkflowTest extends FuncTestCase {

	class TestListener implements WorkflowEventListener {
		public void workflowFailed(WorkflowFailureEvent e) {
			fail("Workflow failed: " + e.getWorkflowInstance().getErrorMessage());
		}

		public void processFailed(ProcessFailureEvent e) {
			fail("Process " + e.getProcessor() + " failed: " + e.getCause());
		}

		public void dataChanged(UserChangedDataEvent e) {
		}

		public void collectionConstructed(CollectionConstructionEvent e) {
		}

		public void workflowCompleted(WorkflowCompletionEvent e) {
		}

		public void processCompleted(ProcessCompletionEvent e) {
		}

		public void processCompletedWithIteration(IterationCompletionEvent e) {
		}

		public void workflowCreated(WorkflowCreationEvent e) {
		}
	}

	/**
	 * A converter for use when reading from a file. Use with readFiles().
	 * 
	 * @author Stian Soiland
	 * 
	 */
	interface Converter {
		/**
		 * Convert a string read from a file to the proper type. The converter
		 * might inspect the filename for making its decission, including the
		 * choice to skip the file by returning null.
		 * 
		 * @param file
		 *            The file that has been read
		 * @param content
		 *            The content of the file
		 * @return The converted object, or null if it is to be ignored.
		 */
		public Object convert(File file, String content);
	}

	/**
	 * Wrap as DataThing instances.
	 * 
	 */
	class DataThingConverter implements Converter {
		public Object convert(File file, String content) {
			return new DataThing(content);
		}
	}

	/**
	 * Read matchers from "outputs" directory. Return a Matcher instance
	 * corresponding to the file extension.
	 * <dl>
	 * <dt>.txt</dt>
	 * <dd>StringMatcher, string equality</dd>
	 * <dt>.regex</dt>
	 * <dd>RegexMatcher, regular expression pattern matching</dd>
	 * <dt>.any</dt>
	 * <dd>AnyMatcher, ignores output</dd>
	 * <dt>.xml</dt>
	 * <dd>XMLMatcher, XML document matching</dd>
	 * </dl>
	 * Unknown file extensions will be ignored.
	 * 
	 * @author stain
	 * 
	 */
	class MatcherConverter implements Converter {
		public Object convert(File file, String content) {
			String name = file.getName().toLowerCase();
			if (name.endsWith(".txt")) {
				return new StringMatcher(content);
			}
			if (name.endsWith(".regex")) {
				return new RegexMatcher(content);
			}
			if (name.endsWith(".any")) {
				return new AnyMatcher();
			}
			return null;
		}
	}

	/**
	 * Interface for matching the output from a workflow.
	 * 
	 * @author Stian Soiland
	 * 
	 */
	interface Matcher {
		/**
		 * Check if the passed DataThing is as expected. Run assert* methods to
		 * check values.
		 * 
		 * @param other
		 *            The DataThing returned for the output that is to be
		 *            matched
		 */
		public void testMatching(DataThing other);
	}

	/**
	 * Blind matcher that accepts any outputs. Use to ignore outputs.
	 * 
	 */
	class AnyMatcher implements Matcher {
		public void testMatching(DataThing other) {
			return;
		}
	}

	/**
	 * Flatten out any lists before checking the string.
	 * 
	 * If the dataObjects are not String, the toString() method will be called.
	 * 
	 * If the DataThing passed to compare is a Collection, compareString() will
	 * be called once with a \n concatinated string.
	 * 
	 */
	abstract class FlatStringMatcher implements Matcher {
		public void testMatching(DataThing other) {
			String otherString = convertToString(other.getDataObject());
			testMatchesString(otherString);
		}

		public String convertToString(Object other) {
			if (other.getClass().isArray()) {
				other = Lang.asObjectList(other);
			}
			if (!(other instanceof Collection)) {
				return other.toString();
			}
			StringBuffer sb = new StringBuffer();
			for (Iterator it = ((Collection) other).iterator(); it.hasNext();) {
				sb.append(convertToString(it.next()));
				// FIXME: This will insert extra \n for double nested lists
				sb.append("\n");
			}
			return sb.toString();
		}

		abstract public void testMatchesString(String other);
	}

	/**
	 * Match strings exact, but not counting white space in beginning or end.
	 * 
	 */
	class StringMatcher extends FlatStringMatcher {
		String match;

		public StringMatcher(String match) {
			this.match = match.trim();
		}

		public void testMatchesString(String other) {
			other = other.trim();
			assertEquals(match, other);
		}
	}

	/**
	 * Match strings by regular expression
	 * 
	 */
	class RegexMatcher extends FlatStringMatcher {
		String pattern;

		public RegexMatcher(String pattern) {
			this.pattern = pattern.trim();
		}

		public void testMatchesString(String other) {
			assertMatches(pattern, other);
		}
	}

	public void testWorkflow(File workflowDir) throws WorkflowSubmissionException, InvalidInputException,
			ProcessorCreationException, DataConstraintCreationException, UnknownProcessorException,
			UnknownPortException, DuplicateProcessorNameException, MalformedNameException,
			ConcurrencyConstraintCreationException, DuplicateConcurrencyConstraintNameException, XScuflFormatException,
			FileNotFoundException {
		if (!workflowDir.toString().endsWith("main/assembly")) { //stops it trying to run the assembly.xml as a workflow
			File workflowFile = new File(workflowDir, workflowDir.getName() + ".xml");
			File outputDir = new File(workflowDir, "outputs");
			File inputDir = new File(workflowDir, "inputs");
			Map inputs = readFiles(inputDir, new DataThingConverter());
			Map outputMatchers = readFiles(outputDir, new MatcherConverter());
			InputStream workflowStream;
			workflowStream = new FileInputStream(workflowFile);
			WorkflowLauncher launcher;
			launcher = new WorkflowLauncher(workflowStream);
			Map outputs;
			outputs = launcher.execute(inputs, new TestListener());
			checkOutputs(outputs, outputMatchers);
		}
	}

	protected void checkOutputs(Map outputs, Map outputMatchers) {
		Set outputSet = outputs.keySet();
		Set expectedOutputSet = outputMatchers.keySet();
		Set missing = new HashSet(expectedOutputSet);
		missing.removeAll(outputSet);
		Set extra = new HashSet(outputSet);
		extra.removeAll(expectedOutputSet);
		for (Iterator it = missing.iterator(); it.hasNext();) {
			fail("Missing output: " + it.next());
		}
		for (Iterator it = extra.iterator(); it.hasNext();) {
			fail("Unexpected output: " + it.next());
		}
		// Check only outputs we expect and that actually came
		outputSet.retainAll(expectedOutputSet);
		for (Iterator it = outputSet.iterator(); it.hasNext();) {
			String outputName = (String) it.next();
			DataThing thing = (DataThing) outputs.get(outputName);
			Matcher matcher = (Matcher) outputMatchers.get(outputName);
			matcher.testMatching(thing);
		}
	}

	/**
	 * Read all files in the given directory.
	 * <p>
	 * Each file in the directory is read into memory, and the data is returned
	 * in a map. The keys are the filenames, but without the extension.
	 * <p>
	 * The content is read from file, and passed to a converter. The result from
	 * the converter is used as the value in the returned map, unless the
	 * converter returns null, which means that the file will be skipped.
	 * <p>
	 * Example: A directory contains the files "names.txt" and "numbers.txt".
	 * The returned map will have the keys "names" and "numbers", with the
	 * values being a String of the whole file contents.
	 * 
	 * @param directory
	 *            Where to find files
	 * @param converter
	 *            A Converter instance that is called for each file.
	 * 
	 * @return A map of the files' (converted) content acessed by the simplified
	 *         filenames
	 */
	public static Map readFiles(File directory, Converter converter) {
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
			Object value = converter.convert(file, input);
			if (value != null) {
				inputs.put(inputName, value);
			}
		}
		return inputs;
	}

}
