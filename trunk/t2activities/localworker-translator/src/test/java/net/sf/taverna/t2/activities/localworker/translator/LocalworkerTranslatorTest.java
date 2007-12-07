package net.sf.taverna.t2.activities.localworker.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sourceforge.taverna.scuflworkers.biojava.GenBankParserWorker;
import net.sourceforge.taverna.scuflworkers.biojava.ReverseCompWorker;
import net.sourceforge.taverna.scuflworkers.biojava.SwissProtParserWorker;
import net.sourceforge.taverna.scuflworkers.biojava.TranscribeWorker;
import net.sourceforge.taverna.scuflworkers.xml.XPathTextWorker;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.ByteArrayToString;
import org.embl.ebi.escience.scuflworkers.java.DecodeBase64;
import org.embl.ebi.escience.scuflworkers.java.EchoList;
import org.embl.ebi.escience.scuflworkers.java.EmitLotsOfStrings;
import org.embl.ebi.escience.scuflworkers.java.EncodeBase64;
import org.embl.ebi.escience.scuflworkers.java.ExtractImageLinks;
import org.embl.ebi.escience.scuflworkers.java.FilterStringList;
import org.embl.ebi.escience.scuflworkers.java.FlattenList;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.PadNumber;
import org.embl.ebi.escience.scuflworkers.java.RegularExpressionStringList;
import org.embl.ebi.escience.scuflworkers.java.SendEmail;
import org.embl.ebi.escience.scuflworkers.java.SplitByRegex;
import org.embl.ebi.escience.scuflworkers.java.StringConcat;
import org.embl.ebi.escience.scuflworkers.java.StringListMerge;
import org.embl.ebi.escience.scuflworkers.java.StringSetDifference;
import org.embl.ebi.escience.scuflworkers.java.StringSetIntersection;
import org.embl.ebi.escience.scuflworkers.java.StringSetUnion;
import org.embl.ebi.escience.scuflworkers.java.StringStripDuplicates;
import org.embl.ebi.escience.scuflworkers.java.TestAlwaysFailingProcessor;
import org.embl.ebi.escience.scuflworkers.java.WebImageFetcher;
import org.embl.ebi.escience.scuflworkers.java.WebPageFetcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Localworker translation tests
 * 
 * @author David Withers
 */
public class LocalworkerTranslatorTest {

	private LocalworkerTranslator translator;

	@Before
	public void setUp() throws Exception {
		translator = new LocalworkerTranslator();
	}

	@Test
	public void testCreateUnconfiguredActivity() {
		BeanshellActivity activity = translator.createUnconfiguredActivity();
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Test
	public void testCreateConfigTypeProcessor() throws Exception {
		BeanshellActivityConfigurationBean bean = translator
				.createConfigType(new LocalServiceProcessor(null, "EchoList",
						new EchoList()));
		assertNotNull(bean);
		assertEquals(bean.getScript(), IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/EchoList")));
	}

	@Test
	public void testCanHandle() throws Exception {
		assertTrue(translator.canHandle(new LocalServiceProcessor(null,
				"EchoList", new EchoList())));
	}

	@Ignore("Beanshell assumes String input")
	@Test
	public void testDoTranslationByteArrayToString() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ByteArrayToString", new ByteArrayToString());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("bytes", "test string".getBytes());
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("string", "test string");

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationDecodeBase64() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"DecodeBase64", new DecodeBase64());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("base64", new String(Base64.encodeBase64("test string"
				.getBytes())));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("bytes", "test string".getBytes());

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationEchoList() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"EchoList", new EchoList());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("inputlist", Collections.singletonList("test"));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("outputlist", Collections.singletonList("test"));

		invoke(activity, inputs, expectedOutputs);

		List<String> list = new ArrayList<String>();
		Collections.addAll(list, "one", "two", "three");
		inputs = new HashMap<String, Object>();
		inputs.put("inputlist", list);
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("outputlist", list);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationEmitLotsOfStrings() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"EmitLotsOfStrings", new EmitLotsOfStrings());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		List<String> strings = new ArrayList<String>();
		for (int i = 0; i < 40; i++) {
			strings.add("String" + i);
		}
		expectedOutputs.put("strings", strings);

		invoke(activity, inputs, expectedOutputs);
	}

	@Ignore("Beanshell assumes String input")
	@Test
	public void testDoTranslationEncodeBase64() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"EncodeBase64", new EncodeBase64());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("bytes", "test string".getBytes());
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("base64", new String(Base64
				.encodeBase64("test string".getBytes())));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationExtractImageLinks() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ExtractImageLinks", new ExtractImageLinks());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("document", IOUtils.toString(LocalworkerTranslatorTest.class
				.getResourceAsStream("/sample_web_page.html")));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("imagelinks", Collections
				.singletonList("/intl/en_uk/images/logo.gif"));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationFilterStringList() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"FilterStringList", new FilterStringList());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> list = new ArrayList<String>();
		Collections.addAll(list, "one", "two", "three");
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("stringlist", list);
		inputs.put("regex", ".n.");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("filteredlist", Collections.singletonList("one"));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationFlattenList() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"FlattenList", new FlattenList());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<List<String>> inputList = new ArrayList<List<String>>();
		inputList.add(Collections.singletonList("one"));
		inputList.add(Collections.singletonList("two"));
		inputList.add(Collections.singletonList("three"));
		List<String> outputList = new ArrayList<String>();
		Collections.addAll(outputList, "one", "two", "three");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("inputlist", inputList);
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("outputlist", outputList);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationPadNumber() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"PadNumber", new PadNumber());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("input", "123");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("padded", "0000123");

		invoke(activity, inputs, expectedOutputs);

		inputs = new HashMap<String, Object>();
		inputs.put("input", "42");
		inputs.put("targetlength", "4");
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("padded", "0042");

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationRegularExpressionStringList() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"RegularExpressionStringList",
				new RegularExpressionStringList());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("stringlist", Collections.singletonList("test best vest"));
		inputs.put("regex", "b.st");
		inputs.put("group", "0");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("filteredlist", Collections.singletonList("best"));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationSplitByRegex() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"SplitByRegex", new SplitByRegex());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> output = new ArrayList<String>();
		Collections.addAll(output, "test", "input");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("string", "test,input");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("split", output);

		invoke(activity, inputs, expectedOutputs);

		inputs = new HashMap<String, Object>();
		inputs.put("string", "test-input");
		inputs.put("regex", "-");
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("split", output);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationSendEmail() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"SendEmail", new SendEmail());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);
	}

	@Test
	public void testDoTranslationStringConcat() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"StringConcat", new StringConcat());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("string1", "one");
		inputs.put("string2", "two");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("output", "onetwo");

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationStringListMerge() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"StringListMerge", new StringListMerge());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> input = new ArrayList<String>();
		Collections.addAll(input, "more", "test", "input");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("stringlist", input);
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("concatenated", "more\ntest\ninput");

		invoke(activity, inputs, expectedOutputs);

		inputs = new HashMap<String, Object>();
		inputs.put("stringlist", input);
		inputs.put("seperator", ":");
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("concatenated", "more:test:input");

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationStringSetDifference() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"StringSetDifference", new StringSetDifference());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> input1 = new ArrayList<String>();
		Collections.addAll(input1, "1", "2", "3");
		List<String> input2 = new ArrayList<String>();
		Collections.addAll(input2, "2", "3", "4");
		List<String> output = new ArrayList<String>();
		Collections.addAll(output, "1", "4");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("list1", input1);
		inputs.put("list2", input2);
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("difference", output);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationStringSetIntersection() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"StringSetIntersection", new StringSetIntersection());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> input1 = new ArrayList<String>();
		Collections.addAll(input1, "1", "2", "3");
		List<String> input2 = new ArrayList<String>();
		Collections.addAll(input2, "2", "3", "4");
		List<String> output = new ArrayList<String>();
		Collections.addAll(output, "2", "3");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("list1", input1);
		inputs.put("list2", input2);
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("intersection", output);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationStringStringSetUnion() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"StringSetUnion", new StringSetUnion());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> input1 = new ArrayList<String>();
		Collections.addAll(input1, "1", "2", "3");
		List<String> input2 = new ArrayList<String>();
		Collections.addAll(input2, "2", "3", "4");
		Set<String> output = new HashSet<String>();
		Collections.addAll(output, "1", "2", "3", "4");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("list1", input1);
		inputs.put("list2", input2);
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("union", new ArrayList<String>(output));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationStringStripDuplicates() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"StringStripDuplicates", new StringStripDuplicates());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> input = new ArrayList<String>();
		Collections.addAll(input, "a", "b", "c", "b", "a", "d");
		List<String> output = new ArrayList<String>();
		Collections.addAll(output, "a", "b", "c", "d");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("stringlist", input);
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("strippedlist", output);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationTestAlwaysFailingProcessor() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"TestAlwaysFailingProcessor", new TestAlwaysFailingProcessor());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("foo", "x");
		inputs.put("bar", "y");

		invoke(activity, inputs, null);
	}

	@Test
	public void testDoTranslationWebImageFetcher() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"WebImageFetcher", new WebImageFetcher());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs
				.put("url",
						"http://www.mygrid.org.uk/taverna-tests/testwebpage/testimage.gif");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("image", IOUtils
				.toByteArray(LocalworkerTranslatorTest.class
						.getResourceAsStream("/testimage.gif")));

		invoke(activity, inputs, expectedOutputs);

		inputs = new HashMap<String, Object>();
		inputs.put("url", "taverna-tests/testwebpage/testimage.gif");
		inputs.put("base", "http://www.mygrid.org.uk/");
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("image", IOUtils
				.toByteArray(LocalworkerTranslatorTest.class
						.getResourceAsStream("/testimage.gif")));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationWebPageFetcher() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"WebPageFetcher", new WebPageFetcher());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs
				.put("url",
						"http://www.mygrid.org.uk/taverna-tests/testwebpage/teststring");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("contents", "This is a test.\n");

		invoke(activity, inputs, expectedOutputs);

		inputs = new HashMap<String, Object>();
		inputs.put("url", "taverna-tests/testwebpage/teststring");
		inputs.put("base", "http://www.mygrid.org.uk/");
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("contents", "This is a test.\n");

		invoke(activity, inputs, expectedOutputs);

	}

	//Contrib local workers
	
	//XPath workers
	@Test
	public void testDoTranslationXPathFromText() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"XPathTextWorker", new XPathTextWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);
		
		assertNotNull("The script is null",activity.getConfiguration().getScript());

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("xpath","//test");
		inputs.put("xml-text","<a><test>111</test><test>222</test></a>");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		List<String> outputNodeList=new ArrayList<String>();
		List<String> outputNodeListAsXML=new ArrayList<String>();
		outputNodeList.add("111");
		outputNodeList.add("222");
		outputNodeListAsXML.add("<test>111</test>");
		outputNodeListAsXML.add("<test>222</test>");
		expectedOutputs.put("nodelist", outputNodeList);
		expectedOutputs.put("nodelistAsXML",outputNodeListAsXML);

		invoke(activity, inputs, expectedOutputs);
	}

	// biojava

	@Test
	public void testDoTranslationGenBankParserWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"GenBankParserWorker", new GenBankParserWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("fileUrl", LocalworkerTranslator.class.getResource(
				"/AY069118.gb").getFile());
		String expectedOutput = IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/AY069118.xml"));

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, Collections.singletonList("genbankdata"));
		assertEquals(1, outputs.size());

		Object output = outputs.get("genbankdata");
		if (output instanceof byte[]) {
			output = new String((byte[]) output);
		}
		assertTrue(output instanceof String);
		assertTrue(((String) output).substring(0, 100).equals(expectedOutput.substring(0, 100)));
	}

	@Test
	public void testDoTranslationSwissProtParserWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"SwissProtParserWorker", new SwissProtParserWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("fileUrl", LocalworkerTranslator.class.getResource(
				"/AAC4_HUMAN.sp").getFile());
		String expectedOutput = IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/AAC4_HUMAN.xml"));

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, Collections.singletonList("results"));
		assertEquals(1, outputs.size());

		Object output = outputs.get("results");
		if (output instanceof byte[]) {
			output = new String((byte[]) output);
		}
		assertTrue(output instanceof String);
		assertTrue(((String) output).substring(0, 100).equals(expectedOutput.substring(0, 100)));
	}

	@Test
	public void testDoTranslationTranscribeWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"TranscribeWorker", new TranscribeWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("dna_seq", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/varC4-2.dna")));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("rna_seq", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/varC4-2.rna")));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationReverseCompWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ReverseCompWorker", new ReverseCompWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("rawSeq", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/varC4-2.dna")));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("revSeq", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/varC4-2.rdna")));

		invoke(activity, inputs, expectedOutputs);
	}

	private void invoke(AbstractAsynchronousActivity<?> activity,
			Map<String, Object> inputs, Map<String, Object> expectedOutputs)
			throws Exception {
		if (expectedOutputs != null) {
			Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
					activity, inputs, expectedOutputs.keySet());
			assertEquals(expectedOutputs.size(), outputs.size());
			for (Map.Entry<String, Object> output : expectedOutputs.entrySet()) {
				assertTrue("No output for port " + output.getKey(), outputs
						.containsKey(output.getKey()));
				if (output.getValue() instanceof byte[]
						&& outputs.get(output.getKey()) instanceof byte[]) {
					assertTrue(Arrays.equals((byte[]) output.getValue(),
							(byte[]) outputs.get(output.getKey())));
				} else if (output.getValue() instanceof String
						&& outputs.get(output.getKey()) instanceof byte[]) {
					assertEquals(output.getValue(), new String((byte[]) outputs
							.get(output.getKey())));
				} else {
					assertEquals(output.getValue(), outputs
							.get(output.getKey()));
				}
			}
		} else {
			Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
					activity, inputs, new ArrayList<String>());
			if (outputs != null) {
				fail("Output should have been null");
			}
		}
	}

	private void verifyPorts(Processor processor, Activity<?> activity) {
		List<String> inputPorts = new ArrayList<String>();
		for (org.embl.ebi.escience.scufl.InputPort inputPort : processor
				.getInputPorts()) {
			inputPorts.add(inputPort.getName());
		}
		List<String> outputPorts = new ArrayList<String>();
		for (org.embl.ebi.escience.scufl.OutputPort outputPort : processor
				.getOutputPorts()) {
			outputPorts.add(outputPort.getName());
		}

		assertEquals(inputPorts.size(), activity.getInputPorts().size());
		for (InputPort port : activity.getInputPorts()) {
			assertTrue(port.getName() + " is not a valid input port",
					inputPorts.remove(port.getName()));
		}
		assertEquals(outputPorts.size(), activity.getOutputPorts().size());
		for (OutputPort port : activity.getOutputPorts()) {
			assertTrue(port.getName() + " is not a valid output port",
					outputPorts.remove(port.getName()));
		}
	}

}
