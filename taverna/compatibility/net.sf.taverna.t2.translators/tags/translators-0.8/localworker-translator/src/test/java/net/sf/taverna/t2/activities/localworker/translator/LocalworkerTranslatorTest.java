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
package net.sf.taverna.t2.activities.localworker.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
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
import net.sourceforge.taverna.scuflworkers.io.ConcatenateFileListWorker;
import net.sourceforge.taverna.scuflworkers.io.DataRangeTask;
import net.sourceforge.taverna.scuflworkers.io.EnvVariableWorker;
import net.sourceforge.taverna.scuflworkers.io.FileListByExtTask;
import net.sourceforge.taverna.scuflworkers.io.FileListByRegexTask;
import net.sourceforge.taverna.scuflworkers.io.LocalCommand;
import net.sourceforge.taverna.scuflworkers.io.TextFileReader;
import net.sourceforge.taverna.scuflworkers.io.TextFileWriter;
import net.sourceforge.taverna.scuflworkers.xml.XPathTextWorker;
import net.sourceforge.taverna.scuflworkers.xml.XSLTWorker;

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
import org.embl.ebi.escience.scuflworkers.java.SliceList;
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

import net.sourceforge.taverna.scuflworkers.ncbi.NucleotideFastaWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.NucleotideGBSeqWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.NucleotideINSDSeqXMLWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.NucleotideTinySeqXMLWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.ProteinFastaWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.ProteinGBSeqWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.ProteinINSDSeqXMLWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.ProteinTinySeqXMLWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.PubMedEFetchWorker;
import net.sourceforge.taverna.scuflworkers.ncbi.PubMedESearchWorker;

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

	@Ignore("POJO problem")
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

	@Ignore("POJO problem")
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

	@Ignore("POJO Problem")
	@Test
	public void testDoTranslationSliceList() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"SliceList", new SliceList());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<String> input = new ArrayList<String>();
		Collections.addAll(input, "one", "two", "three", "four");
		List<String> outputList = new ArrayList<String>();
		Collections.addAll(outputList, "two", "three");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("inputlist", input);
		inputs.put("fromindex", "1");
		inputs.put("toindex", "3");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("outputlist", outputList);

		// invoke(activity, inputs, expectedOutputs);
		// Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
		// activity, inputs, expectedOutputs.keySet());
		// assertEquals(1, outputs.size());
		//		
		// Object output = outputs.get("outputlist");
		// if (output instanceof List) {
		// List<String> newList = new ArrayList<String>();
		// for (Object outputElement : (List) output) {
		// if (outputElement instanceof byte[]) {
		// newList.add(new String((byte[]) outputElement));
		// }
		// }
		// output = newList;
		// }
		// assertEquals(expectedOutputs.get("outputlist"), output);
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

		try {
			invoke(activity, inputs, null);
			fail("should have thrown an exception!");
		} catch (Throwable t) {
			// Okay, we should see an exception here
		}
	}

	@Ignore("Integration test")
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

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("image", byte[].class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());

		Object output = outputs.get("image");
		assertTrue(output instanceof byte[]);
		assertTrue(Arrays.equals((byte[]) output, IOUtils
				.toByteArray(LocalworkerTranslatorTest.class
						.getResourceAsStream("/testimage.gif"))));

		inputs = new HashMap<String, Object>();
		inputs.put("url", "taverna-tests/testwebpage/testimage.gif");
		inputs.put("base", "http://www.mygrid.org.uk/");

		expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("image", byte[].class);

		outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);
		assertEquals(1, outputs.size());

		output = outputs.get("image");
		assertTrue(output instanceof byte[]);
		assertTrue(Arrays.equals((byte[]) output, IOUtils
				.toByteArray(LocalworkerTranslatorTest.class
						.getResourceAsStream("/testimage.gif"))));
	}

	@Ignore("Integration test")
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
		expectedOutputs.put("contents", "This is a test.");

		invoke(activity, inputs, expectedOutputs);

		inputs = new HashMap<String, Object>();
		inputs.put("url", "taverna-tests/testwebpage/teststring");
		inputs.put("base", "http://www.mygrid.org.uk/");
		expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("contents", "This is a test.");

		invoke(activity, inputs, expectedOutputs);

	}

	// Contrib local workers

	// XML workers
	@Test
	public void testDoTranslationXPathFromText() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"XPathTextWorker", new XPathTextWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		assertNotNull("The script is null", activity.getConfiguration()
				.getScript());

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("xpath", "//test");
		inputs.put("xml-text", "<a><test>111</test><test>222</test></a>");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		List<String> outputNodeList = new ArrayList<String>();
		List<String> outputNodeListAsXML = new ArrayList<String>();
		outputNodeList.add("111");
		outputNodeList.add("222");
		outputNodeListAsXML.add("<test>111</test>");
		outputNodeListAsXML.add("<test>222</test>");
		expectedOutputs.put("nodelist", outputNodeList);
		expectedOutputs.put("nodelistAsXML", outputNodeListAsXML);

		invoke(activity, inputs, expectedOutputs);
	}
	
	@Test
	public void testDoTranslationXSLTWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"XSLTWorker", new XSLTWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		assertNotNull("The script is null", activity.getConfiguration()
				.getScript());

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("inFileURL", "http://www.myexperiment.org/files/137/download");
		inputs.put("xslFileURL", "http://www.myexperiment.org/files/138/download");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("outputStr", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/xsltWorker-out.xml")));

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
		URI uri = LocalworkerTranslator.class.getResource("/AY069118.gb")
				.toURI();
		File newFile = new File(uri);
		newFile.getAbsolutePath();

		// inputs.put("fileUrl", LocalworkerTranslator.class.getResource(
		// "/AY069118.gb").getFile());
		inputs.put("fileUrl", newFile.getAbsolutePath());
		String expectedOutput = IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/AY069118.xml"));

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("genbankdata", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());

		Object output = outputs.get("genbankdata");
		assertTrue(output instanceof String);
		assertTrue(((String) output).substring(0, 100).equals(
				expectedOutput.substring(0, 100)));
	}
	
	@Test
	public void testDoTranslationGenBankParserWorker_url() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"GenBankParserWorker", new GenBankParserWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("fileUrl", "http://www.myexperiment.org/files/135/download/franck.txt");
		String expectedOutput = IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/genback-url-out.xml"));

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("genbankdata", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());

		Object output = outputs.get("genbankdata");
		assertTrue(output instanceof String);
		assertTrue(((String) output).substring(0, 100).equals(
				expectedOutput.substring(0, 100)));
	}

	@Test
	public void testDoTranslationSwissProtParserWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"SwissProtParserWorker", new SwissProtParserWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		URI uri = LocalworkerTranslator.class.getResource("/AAC4_HUMAN.sp")
				.toURI();
		File newFile = new File(uri);
		newFile.getAbsolutePath();
		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("fileUrl", LocalworkerTranslator.class.getResource(
		// "/AAC4_HUMAN.sp").getFile());
		inputs.put("fileUrl", newFile.getAbsolutePath());
		String expectedOutput = IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/AAC4_HUMAN.xml"));

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("results", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());

		Object output = outputs.get("results");
		assertTrue(output instanceof String);
		assertTrue(((String) output).substring(0, 100).equals(
				expectedOutput.substring(0, 100)));
	}
	
	@Test
	public void testDoTranslationSwissProtParserWorker_url() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"SwissProtParserWorker", new SwissProtParserWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);


		Map<String, Object> inputs = new HashMap<String, Object>();

		inputs.put("fileUrl", "http://www.myexperiment.org/files/136/download/P30512.txt");
		String expectedOutput = IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/swissprot-url-out.xml"));

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("results", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());

		Object output = outputs.get("results");
		assertTrue(output instanceof String);
		assertTrue(((String) output).substring(0, 100).equals(
				expectedOutput.substring(0, 100)));
	}
	@Test
	public void testDoTranslationTranscribeWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"TranscribeWorker", new TranscribeWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("dna_seq", IOUtils.toString(LocalworkerTranslator.class
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
		inputs.put("rawSeq", IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/varC4-2.dna")));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("revSeq", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/varC4-2.rdna")));

		invoke(activity, inputs, expectedOutputs);
	}

	// io

	@Test
	public void testDoTranslationTextFileReader() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"TextFileReader", new TextFileReader());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		URI uri = LocalworkerTranslator.class.getResource("/AAC4_HUMAN.sp")
				.toURI();
		File newFile = new File(uri);
		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("fileurl", LocalworkerTranslator.class.getResource(
		// "/AAC4_HUMAN.sp").getFile());
		inputs.put("fileurl", newFile.getAbsolutePath());
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("filecontents", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/AAC4_HUMAN.sp")));

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationTextFileReader_url() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"TextFileReader", new TextFileReader());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("fileurl", LocalworkerTranslator.class.getResource(
		// "/AAC4_HUMAN.sp").getFile());
		inputs.put("fileurl", "http://www.myexperiment.org/files/150/download/example.txt");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("filecontents", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/example.txt")));

		invoke(activity, inputs, expectedOutputs);
	}
	
	@Test
	public void testDoTranslationTextFileWriter() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"TextFileWriter", new TextFileWriter());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		File tempFile = File.createTempFile("test", null);
		inputs.put("outputFile", tempFile.getAbsolutePath());
		inputs.put("filecontents", IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/AAC4_HUMAN.sp")));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("outputFile", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/AAC4_HUMAN.sp")));

		invoke(activity, inputs, expectedOutputs);

		assertEquals(IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/AAC4_HUMAN.sp")), FileUtils
				.readFileToString(tempFile, null));
		tempFile.delete();
	}

	@Test
	public void testDoTranslationLocalCommand() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"LocalCommand", new LocalCommand());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("command", "echo");
		inputs.put("args", Collections.singletonList("hello"));
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("result", "hello" + System.getProperty("line.separator"));

		invoke(activity, inputs, expectedOutputs);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDoTranslationFileListByExtTask() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"FileListByExtTask", new FileListByExtTask());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		URI inURI = LocalworkerTranslator.class.getResource("/test").toURI();
		File newFile = new File(inURI);
		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("directory", LocalworkerTranslator.class
		// .getResource("/test").getFile());
		inputs.put("directory", newFile.getAbsolutePath());
		inputs.put("extension", "test");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("filelist", String.class);
		Set<String> outputList = new HashSet<String>();

		URI alpha = LocalworkerTranslator.class.getResource("/test/alpha.test")
				.toURI();
		File alphaFile = new File(alpha);
		URI beta = LocalworkerTranslator.class.getResource("/test/beta.test")
				.toURI();
		File betaFile = new File(beta);
		URI gamma = LocalworkerTranslator.class.getResource("/test/gamma.test")
				.toURI();
		File gammaFile = new File(gamma);

		// outputList.add(LocalworkerTranslator.class.getResource(
		// "/test/alpha.test").getFile());
		// outputList.add(LocalworkerTranslator.class.getResource(
		// "/test/beta.test").getFile());
		// outputList.add(LocalworkerTranslator.class.getResource(
		// "/test/gamma.test").getFile());

		outputList.add(alphaFile.getAbsolutePath());
		outputList.add(betaFile.getAbsolutePath());
		outputList.add(gammaFile.getAbsolutePath());

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(expectedOutputs.size(), outputs.size());
		Object output = outputs.get("filelist");

		assertTrue(output instanceof List);
		List returnedList = (List) output;
		assertEquals(returnedList.size(), outputList.size());

		for (String outputFile : outputList) {
			assertTrue(returnedList.remove(outputFile));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDoTranslationFileListByRegexTask() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"FileListByRegexTask", new FileListByRegexTask());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		URI inURI = LocalworkerTranslator.class.getResource("/test").toURI();
		File inFile = new File(inURI);

		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("directory", LocalworkerTranslator.class
		// .getResource("/test").getFile());
		inputs.put("directory", inFile.getAbsolutePath());
		inputs.put("regex", ".+[h|t]a\\.test");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("filelist", String.class);
		Set<String> outputList = new HashSet<String>();

		URI alpha = LocalworkerTranslator.class.getResource("/test/alpha.test")
				.toURI();
		File alphaFile = new File(alpha);
		URI beta = LocalworkerTranslator.class.getResource("/test/beta.test")
				.toURI();
		File betaFile = new File(beta);

		outputList.add(alphaFile.getAbsolutePath());
		outputList.add(betaFile.getAbsolutePath());

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(expectedOutputs.size(), outputs.size());
		Object output = outputs.get("filelist");

		assertTrue(output instanceof List);
		List returnedList = (List) output;
		assertEquals(returnedList.size(), outputList.size());

		for (String outputFile : outputList) {
			assertTrue(returnedList.remove(outputFile));
		}
		// Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
		// activity, inputs, expectedOutputs.keySet());
		// assertEquals(expectedOutputs.size(), outputs.size());
		// for (Map.Entry<String, Object> output : expectedOutputs.entrySet()) {
		// assertTrue("No output for port " + output.getKey(), outputs
		// .containsKey(output.getKey()));
		// assertEquals(output.getValue(), new
		// HashSet<String>((Collection<String>) outputs
		// .get(output.getKey())));
		// }
	}

	@Test
	public void testDoTranslationDataRangeTask() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"DataRangeTask", new DataRangeTask());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		List<List<String>> inputList = new ArrayList<List<String>>();
		inputList.add(Arrays.asList(new String[] { "a1", "b1", "c1", "d1" }));
		inputList.add(Arrays.asList(new String[] { "a2", "b2", "c2", "d2" }));
		inputList.add(Arrays.asList(new String[] { "a3", "b3", "c3", "d3" }));
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("inputArray", inputList);
		inputs.put("startingPoint", "1,1");
		inputs.put("endPoint", "3,2");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		List<List<String>> outputList = new ArrayList<List<String>>();
		outputList.add(Arrays.asList(new String[] { "b2", "c2", "d2" }));
		outputList.add(Arrays.asList(new String[] { "b3", "c3", "d3" }));
		expectedOutputs.put("outputArray", outputList);

		invoke(activity, inputs, expectedOutputs);
	}

	@Test
	public void testDoTranslationConcatenateFileListWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ConcatenateFileListWorker", new ConcatenateFileListWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		URI in1 = LocalworkerTranslator.class.getResource(
				"/concatenateTestFile1.txt").toURI();
		File inFile1 = new File(in1);

		URI in2 = LocalworkerTranslator.class.getResource(
				"/concatenateTestFile2.txt").toURI();
		File inFile2 = new File(in2);

		File temp = File.createTempFile("temp", null);
		List<String> inputList = new ArrayList<String>();
//		inputList.add(LocalworkerTranslator.class.getResource(
//				"/concatenateTestFile1.txt").getFile());
//		inputList.add(LocalworkerTranslator.class.getResource(
//				"/concatenateTestFile2.txt").getFile());

		inputList.add(inFile1.getAbsolutePath());
		inputList.add(inFile2.getAbsolutePath());
		inputList.add("http://www.myexperiment.org/files/150/download/example.txt");

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("filelist", inputList);
		inputs.put("outputfile", temp.getAbsolutePath());
		inputs.put("displayresults", "true");
		Map<String, Object> expectedOutputs = new HashMap<String, Object>();
		expectedOutputs.put("results", IOUtils
				.toString(LocalworkerTranslator.class
						.getResourceAsStream("/concatenateTestOut.txt")));

		invoke(activity, inputs, expectedOutputs);

		assertEquals(IOUtils.toString(LocalworkerTranslator.class
				.getResourceAsStream("/concatenateTestOut.txt")), IOUtils
				.toString(new FileReader(temp)));

		// test displayresults = false
		inputs.put("displayresults", "false");
		expectedOutputs = new HashMap<String, Object>();

		invoke(activity, inputs, expectedOutputs);
		temp.delete();
	}

	/**
	 * Check if a String starts with xml
	 * 
	 * @param text
	 * @return
	 */
	private boolean xmlStartToString(final String text) {
		String alteredText = text.replaceAll("\\<\\?xml(\\s)+version=\"1\\.0\"(\\s)*\\?\\>", "xxxx");
		return alteredText.startsWith("xxxx");
	}
	
	@Test
	public void testDoTranslationEnvVariableWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"EnvVariableWorker", new EnvVariableWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("properties", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());

		Object output = outputs.get("properties");
		assertTrue(output instanceof String);
		assertTrue(xmlStartToString((String)output));
		assertTrue(((String) output).endsWith("</property-list>"));
	}

	@Test
	public void testDoTranslationNucleotideFastaWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"NucleotideFastaWorker", new NucleotideFastaWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(((String)outputText).startsWith(">gi|18450186"));
	}
	
	@Test
	public void testDoTranslationNucleotideGBSeqWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"NucleotideGBSeqWorker", new NucleotideGBSeqWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<GBSeqid>gi|18450186</GBSeqid>"));
	}

	@Test
	public void testDoTranslationNucleotideINSDSeqXMLWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"NucleotideINSDSeqXMLWorker", new NucleotideINSDSeqXMLWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<INSDSeqid>gi|18450186</INSDSeqid>"));
	}
	
	@Test
	public void testDoTranslationNucleotideTinySeqXMLWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"NucleotideTinySeqXMLWorker", new NucleotideTinySeqXMLWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<TSeq_gi>18450186</TSeq_gi>"));
	}
	
	@Test
	public void testDoTranslationProteinFastaWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ProteinFastaWorker", new ProteinFastaWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(((String)outputText).startsWith(">gi|18450186"));
	}
	
	@Test
	public void testDoTranslationProteinGBSeqWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ProteinGBSeqWorker", new ProteinGBSeqWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<GBSeqid>gi|18450186</GBSeqid>"));
	}

	@Test
	public void testDoTranslationProteinINSDSeqXMLWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ProteinINSDSeqXMLWorker", new ProteinINSDSeqXMLWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<INSDSeqid>gi|18450186</INSDSeqid>"));
	}
	
	@Test
	public void testDoTranslationProteinTinySeqXMLWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"ProteinTinySeqXMLWorker", new ProteinTinySeqXMLWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue(xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<TSeq_gi>18450186</TSeq_gi>"));
	}
	
	@Test
	public void testDoTranslationPubMedEFetchWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"PubmedEFetchWorker", new PubMedEFetchWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("id", "18450186");
//		inputs.put("retmode", "xml");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue((String) outputText, xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<PMID>18450186</PMID>"));
	}
	
	@Test
	public void testDoTranslationPubMedESearchWorker() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"PubmedESearchWorker", new PubMedESearchWorker());
		BeanshellActivity activity = (BeanshellActivity) translator
				.doTranslation(processor);

		verifyPorts(processor, activity);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("term", "18450186");
//		inputs.put("retmode", "xml");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("outputText", String.class);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertEquals(1, outputs.size());
		
		Object outputText = outputs.get("outputText");
		assertTrue(outputText instanceof String);
		assertTrue((String) outputText, xmlStartToString((String)outputText));
		assertTrue((String) outputText, ((String)outputText).contains("<Id>18450186</Id>"));
	}
	
	private void invoke(AbstractAsynchronousActivity<?> activity,
			Map<String, Object> inputs, Map<String, Object> expectedOutputs)
			throws Exception {
		if (expectedOutputs != null) {
			Map<String, Class<?>> expectedOutput = new HashMap<String, Class<?>>();
			for (Map.Entry<String, Object> entry : expectedOutputs.entrySet()) {
				if (entry.getValue() instanceof List) {
					Object element = ((List) entry.getValue()).get(0);
					if (element instanceof List) {
						element = ((List) element).get(0);
					}
					expectedOutput.put(entry.getKey(), element.getClass());
				} else {
					expectedOutput.put(entry.getKey(), entry.getValue()
							.getClass());
				}
			}
			Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
					activity, inputs, expectedOutput);
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
					activity, inputs, new HashMap<String, Class<?>>());
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
