package uk.org.taverna.scufl2.api.common;

import static org.junit.Assert.assertSame;

import java.net.URI;
import java.util.List;

import org.junit.Test;

import uk.org.taverna.scufl2.api.ExampleWorkflow;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.iterationstrategy.CrossProduct;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;

public class TestURIToolsResolve {
	private static final URI BUNDLE_URI = URI
			.create("http://ns.taverna.org.uk/2010/workflowBundle/28f7c554-4f35-401f-b34b-516e9a0ef731/");
	private static final URI HELLOWORLD_URI = BUNDLE_URI
			.resolve("workflow/HelloWorld/");
	private static final URI HELLO_URI = HELLOWORLD_URI
			.resolve("processor/Hello/");
	private static final URI PROFILE_URI = BUNDLE_URI
			.resolve("profile/tavernaWorkbench/");
	private URITools uriTools = new URITools();
	private Scufl2Tools scufl2Tools = new Scufl2Tools();
	private WorkflowBundle wfBundle = new ExampleWorkflow()
			.makeWorkflowBundle();

	@Test
	public void resolveActivity() throws Exception {
		Activity helloScript = wfBundle.getMainProfile().getActivities().getByName("HelloScript");
		assertSame(helloScript, uriTools.resolveUri(PROFILE_URI.resolve("activity/HelloScript/"), wfBundle));
	}

	@Test
	public void resolveActivityInput() throws Exception {
		Activity helloScript = wfBundle.getMainProfile().getActivities()
				.getByName("HelloScript");

		assertSame(helloScript.getInputPorts().getByName("personName"),
				uriTools.resolveUri(PROFILE_URI
						.resolve("activity/HelloScript/in/personName"),
						wfBundle));
	}

	@Test
	public void resolveActivityOutput() throws Exception {
		Activity helloScript = wfBundle.getMainProfile().getActivities()
				.getByName("HelloScript");
		assertSame(helloScript.getOutputPorts().getByName("hello"),
				uriTools.resolveUri(
						PROFILE_URI.resolve("activity/HelloScript/out/hello"),
						wfBundle));
	}

	@Test
	public void resolveBundle() throws Exception {
		assertSame(wfBundle, uriTools.resolveUri(BUNDLE_URI, wfBundle));
	}


	@Test
	public void resolveConfiguration() throws Exception {
		Configuration config = wfBundle.getMainProfile().getConfigurations().getByName("Hello");

		assertSame(config,
				uriTools.resolveUri(
						PROFILE_URI.resolve("configuration/Hello/"),
						wfBundle));
	}

	@Test
	public void resolveControlLink() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		List<BlockingControlLink> blocks = scufl2Tools
				.controlLinksBlocking(hello);
		assertSame(
				blocks.get(0),
				uriTools.resolveUri(
						HELLOWORLD_URI
								.resolve("control?block=processor/Hello/&untilFinished=processor/wait4me/"),
						wfBundle));
	}

	@Test
	public void resolveDataLink() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		List<DataLink> datalinks = scufl2Tools.datalinksTo(hello
				.getInputPorts().getByName("name"));
		assertSame(
				datalinks.get(0),
				uriTools.resolveUri(
						HELLOWORLD_URI
								.resolve("datalink?from=in/yourName&to=processor/Hello/in/name"),
						wfBundle));
	}

	@Test
	public void resolveDataLinkWithMerge() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		OutputProcessorPort greeting = hello.getOutputPorts().getByName(
				"greeting");
		List<DataLink> datalinks = scufl2Tools.datalinksFrom(greeting);
		assertSame(
				datalinks.get(0),
				uriTools.resolveUri(
						HELLOWORLD_URI
								.resolve("datalink?from=processor/Hello/out/greeting&to=out/results&mergePosition=0"),
						wfBundle));
	}

	@Test
	public void resolveProcessor() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		assertSame(hello, uriTools.resolveUri(HELLO_URI, wfBundle));
	}

	@Test
	public void resolveProcessorBinding() throws Exception {
		ProcessorBinding procBind = wfBundle.getMainProfile().getProcessorBindings()
				.getByName("Hello");
		assertSame(procBind,
				uriTools.resolveUri(
						PROFILE_URI.resolve("processorbinding/Hello/"),
						wfBundle));
	}

	@Test
	public void resolveProcessorBindingIn() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		ProcessorInputPortBinding binding = scufl2Tools
				.processorPortBindingForPort(
						hello.getInputPorts().getByName("name"),
						wfBundle.getMainProfile());
		assertSame(binding,
				uriTools.resolveUri(
						PROFILE_URI.resolve("processorbinding/Hello/in/name"),
						wfBundle));
	}

	@Test
	public void resolveProcessorBindingOut() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
		.getByName("Hello");
		ProcessorOutputPortBinding binding = scufl2Tools
				.processorPortBindingForPort(
						hello.getOutputPorts().getByName("greeting"),
						wfBundle.getMainProfile());
		assertSame(binding,
				uriTools.resolveUri(
						PROFILE_URI.resolve("processorbinding/Hello/out/greeting"),
						wfBundle));
	}

	@Test
	public void resolveProcessorDispatchStack() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		assertSame(hello.getDispatchStack(), uriTools.resolveUri(
				HELLO_URI.resolve("dispatchstack/"), wfBundle));
	}

	@Test
	public void resolveProcessorDispatchStackLayer() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		assertSame(hello.getDispatchStack().get(5), uriTools.resolveUri(
				HELLO_URI.resolve("dispatchstack/5/"), wfBundle));
	}

	@Test
	public void resolveProcessorInput() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		InputProcessorPort helloName = hello.getInputPorts().getByName("name");
		assertSame(helloName,
				uriTools.resolveUri(HELLO_URI.resolve("in/name"), wfBundle));
	}

	@Test
	public void resolveProcessorIterationStrategy() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		assertSame(hello.getIterationStrategyStack().get(0),
				uriTools.resolveUri(
HELLO_URI.resolve("iterationstrategy/0/"),
						wfBundle));
	}

	@Test
	public void resolveProcessorIterationStrategyNode() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		CrossProduct rootStrategyNode = (CrossProduct) hello
				.getIterationStrategyStack().get(0).getRootStrategyNode();
		assertSame(rootStrategyNode.get(0),
				uriTools.resolveUri(
				HELLO_URI.resolve("iterationstrategy/0/root/0/"),
						wfBundle));
	}

	@Test
	public void resolveProcessorIterationStrategyRoot() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		assertSame(hello.getIterationStrategyStack().get(0)
				.getRootStrategyNode(), uriTools.resolveUri(
				HELLO_URI.resolve("iterationstrategy/0/root/"), wfBundle));
	}

	@Test
	public void resolveProcessorIterationStrategyStack() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		assertSame(
				hello.getIterationStrategyStack(),
				uriTools.resolveUri(
				HELLO_URI.resolve("iterationstrategy/"), wfBundle));
	}

	@Test
	public void resolveProcessorOutput() throws Exception {
		Processor hello = wfBundle.getMainWorkflow().getProcessors()
				.getByName("Hello");
		OutputProcessorPort greeting = hello.getOutputPorts().getByName(
				"greeting");
		assertSame(greeting, uriTools.resolveUri(
				HELLO_URI.resolve("out/greeting"), wfBundle));
	}

	@Test
	public void resolveProfile() throws Exception {
		assertSame(wfBundle.getMainProfile(), uriTools.resolveUri(PROFILE_URI, wfBundle));
	}

	@Test
	public void resolveWorkflow() throws Exception {
		assertSame(wfBundle.getMainWorkflow(),
				uriTools.resolveUri(HELLOWORLD_URI, wfBundle));
	}

	@Test
	public void resolveWorkflowInput() throws Exception {
		InputWorkflowPort yourName = wfBundle.getMainWorkflow().getInputPorts()
				.getByName("yourName");
		assertSame(yourName, uriTools.resolveUri(
				HELLOWORLD_URI.resolve("in/yourName"), wfBundle));
	}

	@Test
	public void resolveWorkflowOutput() throws Exception {
		OutputWorkflowPort results = wfBundle.getMainWorkflow()
				.getOutputPorts().getByName("results");
		assertSame(results, uriTools.resolveUri(
				HELLOWORLD_URI.resolve("out/results"), wfBundle));
	}

}
