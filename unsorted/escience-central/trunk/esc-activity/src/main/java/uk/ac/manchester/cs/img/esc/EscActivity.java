package uk.ac.manchester.cs.img.esc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.connexience.server.api.API;
import com.connexience.server.api.APIConnectException;
import com.connexience.server.api.APIInstantiationException;
import com.connexience.server.api.APIParseException;
import com.connexience.server.api.APISecurityException;
import com.connexience.server.api.IDocument;
import com.connexience.server.api.IObject;
import com.connexience.server.api.IWorkflow;
import com.connexience.server.api.IWorkflowInvocation;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class EscActivity extends
		AbstractAsynchronousActivity<EscActivityConfigurationBean>
		implements AsynchronousActivity<EscActivityConfigurationBean> {

	/*
	 * Best practice: Keep port names as constants to avoid misspelling. This
	 * would not apply if port names are looked up dynamically from the service
	 * operation, like done for WSDL services.
	 */
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String REPORT = "report";
	private static final String REPORT_FILE_NAME = "report.xml";
	private static final String WORKFLOW = "workflow";
	private static final String WORKFLOW_FILE_NAME = "workflow.xml";
	
	private EscActivityConfigurationBean configBean;

	@Override
	public void configure(EscActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;

		configurePorts();
	}

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();

		// FIXME: Replace with your input and output port definitions
		
		// Hard coded input port, expecting a single String
		addInput(INPUT, 0, true, null, String.class);

		addOutput(OUTPUT, 1);
		
		if (configBean.isProduceReport()) {
			addOutput(REPORT, 0);
		}
		if (configBean.isProduceWorkflow()) {
			addOutput(WORKFLOW, 0);
		}

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {
			
			public void run() {
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
				// Resolve inputs 				
				String inString = (String) referenceService.renderIdentifier(inputs.get(INPUT), 
						String.class, context);
				
				API api = null;
				IDocument tempFile = null;
				IWorkflowInvocation inv = null;
				
				try {
					

				api = ConnectionUtil.getAPI(getConfiguration().getUrl());
				
				tempFile = (IDocument)api.createObject(IDocument.XML_NAME);
				tempFile.setName("upload" + System.currentTimeMillis());
				tempFile = api.saveDocument(api.getUserFolder(api.getUserContext()), tempFile);
				api.upload(tempFile, new ByteArrayInputStream(inString.getBytes()));
				
				IWorkflow workflow = null;
				for (IWorkflow w : api.listWorkflows()) {
					if (w.getId().equals(getConfiguration().getId())) {
						workflow = w;
						break;
					}
				}
				if (workflow == null) {
					callback.fail("Could not invoke workflow");
				}
				
				inv = api.executeWorkflow(workflow, tempFile);

				while(!(inv.getStatus().equals(IWorkflowInvocation.WORKFLOW_FINISHED_OK) || inv.getStatus().equals(IWorkflowInvocation.WORKFLOW_FINISHED_WITH_ERRORS))){
					String s = inv.getStatus();
					Thread.sleep(configBean.getPollingInterval() * 1000);
					inv = api.getWorkflowInvocation(inv.getInvocationId());
				}
				
				if (inv.getStatus().equals(IWorkflowInvocation.WORKFLOW_FINISHED_WITH_ERRORS)) {
					callback.fail("Workflow finished with errors");
				}
				
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();

				List<IObject> results = api.getFolderContents(inv);
				List resultList = new ArrayList();
				for (IObject r : results) {
					if (r instanceof IDocument) {
						IDocument d = (IDocument) r;
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						api.download(d, outStream);
						outStream.flush();
						byte[] result = outStream.toByteArray();
						if (d.getName().equals(REPORT_FILE_NAME)) {
							if (configBean.isProduceReport()) {
								outputs.put(REPORT, referenceService.register(result, 0, true, context));
							}
						} else if (d.getName().equals(WORKFLOW_FILE_NAME)) {
							if (configBean.isProduceWorkflow()) {
								outputs.put(WORKFLOW, referenceService.register(result, 0, true, context));
							}
						} else {
							resultList.add(result);
						}
					}
				}
				T2Reference outputRef = referenceService.register(resultList, 1, true, context);
				outputs.put(OUTPUT, outputRef);

				callback.receiveResult(outputs, new int[0]);
				
				}
				catch (APISecurityException e) {
					callback.fail("Permissions issue at " + configBean.getUrl(), e);
				} catch (APIParseException e) {
					callback.fail("Communication format problem", e);
				} catch (APIInstantiationException e) {
					callback.fail("API set up problem", e);
				} catch (CMException e) {
					callback.fail("Credential management failure", e);
				} catch (InterruptedException e) {
					callback.fail("Polling interrupted", e);
				} catch (APIConnectException e) {
					callback.fail("Unable to connect", e);
				} catch (MalformedURLException e) {
					callback.fail("Malformed URL", e);
				} catch (IOException e) {
					callback.fail("Data reading problem", e);
				}
				finally {
					if (!configBean.isDebug() && (api != null) && (tempFile != null)){
						try {
							api.deleteDocument(tempFile);
						} catch (APIConnectException e) {
							callback.fail("Unable to connect", e);
						} catch (APISecurityException e) {
							callback.fail("Permissions issue at " + configBean.getUrl(), e);
						} catch (APIParseException e) {
							callback.fail("Communication format problem", e);
						} catch (APIInstantiationException e) {
							callback.fail("API set up problem", e);
						}
					}
					if (!configBean.isDebug() && (api != null) && (inv != null)){
							try {
								api.deleteFolder(inv);
							} catch (APIConnectException e) {
								callback.fail("Unable to connect", e);
							} catch (APISecurityException e) {
								callback.fail("Permissions issue at " + configBean.getUrl(), e);
							} catch (APIParseException e) {
								callback.fail("Communication format problem", e);
							} catch (APIInstantiationException e) {
								callback.fail("API set up problem", e);
							}
					}

				}


			}
		});
	}

	@Override
	public EscActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

}
