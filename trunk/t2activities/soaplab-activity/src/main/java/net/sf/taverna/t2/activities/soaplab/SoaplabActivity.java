package net.sf.taverna.t2.activities.soaplab;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityHealthReport;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;

/**
 * <p>
 * An Activity providing Soaplab functionality.
 * </p>
 * 
 * @author David Withers
 */
public class SoaplabActivity extends
		AbstractAsynchronousActivity<SoaplabActivityConfigurationBean> {

	private static final Logger logger = Logger
			.getLogger(SoaplabActivity.class);

	private static final int INVOCATION_TIMEOUT = 0;

	private SoaplabActivityConfigurationBean configurationBean;
	
	private Map<String, Class<?>> inputTypeMap = new HashMap<String, Class<?>>();

	public SoaplabActivity() {
	}

	@Override
	public void configure(SoaplabActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		generatePorts();
	}

	@Override
	public SoaplabActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				DataFacade dataFacade = new DataFacade(callback.getContext().getDataManager());

				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

				try {
					// Copy the contents of the data set in the input map
					// to a new Map object which just contains the raw data
					// objects
					Map<String, Object> soaplabInputMap = new HashMap<String, Object>();
					for (Map.Entry<String, EntityIdentifier> entry : data
							.entrySet()) {
						logger.info("Resolving " + entry.getKey() + " to " + inputTypeMap.get(entry.getKey()));
						soaplabInputMap.put(entry.getKey(), dataFacade
								.resolve(entry.getValue(), inputTypeMap.get(entry.getKey())));
						logger.info("  Value = " + soaplabInputMap.get(entry.getKey()));
					}

					// Invoke the web service...
					Call call = (Call) new Service().createCall();
					call.setTimeout(new Integer(INVOCATION_TIMEOUT));
					// TODO is there endpoint stored in the configuration as a
					// String or a URL?
					// URL soaplabWSDLURL = new
					// URL(configurationBean.getEndpoint());
					call.setTargetEndpointAddress(configurationBean
							.getEndpoint());

					// Invoke the job and wait for it to complete
					call.setOperationName(new QName("createAndRun"));
					String jobID = (String) call
							.invoke(new Object[] { soaplabInputMap });
					// Get the array of desired outputs to avoid pulling
					// everything back
					// TODO Decide how to get the bound ports for the processor
					// OutputPort[] boundOutputs =
					// this.proc.getBoundOutputPorts();
					OutputPort[] boundOutputs = getOutputPorts().toArray(
							new OutputPort[0]);
					String[] outputPortNames = new String[boundOutputs.length];
					for (int i = 0; i < outputPortNames.length; i++) {
						outputPortNames[i] = boundOutputs[i].getName();
						logger.debug("Adding output : " + outputPortNames[i]);
					}

					if (!isPollingDefined()) {
						// If we're not polling then use this behaviour
						call.setOperationName(new QName("waitFor"));
						call.invoke(new Object[] { jobID });
					} else {
						// Wait for the polling interval then request a status
						// and do this until the status is terminal.
						boolean polling = true;
						// Number of milliseconds to wait before the first
						// status request.
						int pollingInterval = configurationBean
								.getPollingInterval();
						while (polling) {
							try {
								Thread.sleep(pollingInterval);
							} catch (InterruptedException ie) {
								// do nothing
							}
							call.setOperationName(new QName("getStatus"));
							String statusString = (String) call
									.invoke(new Object[] { jobID });
							logger.info("Polling, status is : " + statusString);
							if (statusString.equals("RUNNING")
									|| statusString.equals("CREATED")) {
								pollingInterval = (int) ((double) pollingInterval * configurationBean
										.getPollingBackoff());
								if (pollingInterval > configurationBean
										.getPollingIntervalMax()) {
									pollingInterval = configurationBean
											.getPollingIntervalMax();
								}
							} else {
								// Either completed with an error or success
								polling = false;
							}
						}
					}

					// Get the status code
					call.setOperationName(new QName("getStatus"));
					String statusString = (String) call
							.invoke(new Object[] { jobID });
					if (statusString.equals("TERMINATED_BY_ERROR")) {
						// Get the report
						call.setOperationName(new QName("getSomeResults"));
						HashMap<String, String> temp = new HashMap<String, String>(
								(Map) call.invoke(new Object[] { jobID,
										new String[] { "report" } }));
						String reportText = temp.get("report");
						callback.fail("Soaplab call returned an error : "
								+ reportText);
						return;
					}

					// Get the results required by downstream processors
					call.setOperationName(new QName("getSomeResults"));
					HashMap<String, Object> outputMap = new HashMap<String, Object>(
							(Map) call.invoke(new Object[] { jobID,
									outputPortNames }));

					// Tell soaplab that we don't need this session any more
					call.setOperationName(new QName("destroy"));
					call.invoke(new Object[] { jobID });

					// Build the map of DataThing objects
					for (Map.Entry<String, Object> entry : outputMap.entrySet()) {
						String parameterName = entry.getKey();
						Object outputObject = entry.getValue();
						if (logger.isDebugEnabled())
							logger.debug("Soaplab : parameter '"
									+ parameterName + "' has type '"
									+ outputObject.getClass().getName() + "'");

						if (outputObject instanceof String[]) {
							// outputThing = DataThingFactory
							// .bake((String[]) outputObject);
							outputData.put(parameterName, dataFacade
									.register(Arrays.asList(outputObject)));
						} else if (outputObject instanceof byte[][]) {
							// Create a List of byte arrays, this will
							// map to l('application/octet-stream') in
							// the output document.
							// outputThing = DataThingFactory
							// .bake((byte[][]) outputObject);
							List<byte[]> list = new ArrayList<byte[]>();
							for (byte[] byteArray : (byte[][]) outputObject) {
								list.add(byteArray);
							}
							outputData.put(parameterName, dataFacade.register(list));
//							outputData.put(parameterName, dataFacade
//									.register(Arrays.asList(outputObject)));
						} else if (outputObject instanceof List) {
							List<?> convertedList = convertList((List<?>) outputObject);
							outputData.put(parameterName, dataFacade
									.register(convertedList));
						} else {
							// Fallthrough case, this mostly applies to
							// output of type byte[] or string, both of which
							// are handled perfectly sensibly by default.
							outputData.put(parameterName, dataFacade
									.register(outputObject));
						}
					}

					// success
					callback.receiveResult(outputData, new int[0]);
				} catch (RetrievalException e) {
					callback.fail("Failure calling soaplab", e);
				} catch (NotFoundException e) {
					callback.fail("Failure calling soaplab", e);
				} catch (EmptyListException e) {
					callback.fail("Failure calling soaplab", e);
				} catch (MalformedListException e) {
					callback.fail("Failure calling soaplab", e);
				} catch (UnsupportedObjectTypeException e) {
					callback.fail("Failure calling soaplab", e);
				} catch (IOException e) {
					callback.fail("Failure calling soaplab", e);
				} catch (ServiceException e) {
					callback.fail("Failure calling soaplab", e);
				}
			}

		});

	}

	public boolean isPollingDefined() {
		return configurationBean != null && (configurationBean.getPollingInterval() != 0
				|| configurationBean.getPollingBackoff() != 1.0 || configurationBean
				.getPollingIntervalMax() != 0);
	}

	private List<?> convertList(List<?> theList) {
		if (theList.size() == 0) {
			return theList;
		}

		List<byte[]> listOfBytes = new ArrayList<byte[]>();
		for (Object element : theList) {
			if (element instanceof List) {
				List<?> list = ((List<?>) element);
				if (list.size() > 0 && (list.get(0) instanceof Byte)) {
					byte[] bytes = new byte[list.size()];
					for (int j = 0; j < list.size(); j++) {
						bytes[j] = ((Byte) list.get(j)).byteValue();
					}
					listOfBytes.add(bytes);
				} else {
					// If we can't cope here just return the original
					// object
					return theList;
				}
			} else {
				return theList;
			}
		}
		return listOfBytes;
	}

	private void generatePorts() throws ActivityConfigurationException {
		// Wipe the existing port declarations
		// ports = new ArrayList();
		try {
			// Do web service type stuff[tm]
			Map<String, String>[] inputs = (Map<String, String>[]) Soap
					.callWebService(configurationBean.getEndpoint(),
							"getInputSpec");
			// Iterate over the inputs
			for (int i = 0; i < inputs.length; i++) {
				Map<String, String> input_spec = inputs[i];
				String input_name = input_spec.get("name");
				String input_type = input_spec.get("type").toLowerCase();
				// Could get other properties such as defaults here
				// but at the moment we've got nowhere to put them
				// so we don't bother.
				if (input_type.equals("string")) {
					addInput(input_name, 0, Collections
							.singletonList("'text/plain'"));
					inputTypeMap.put(input_name, String.class);
				} else if (input_type.equals("string[]")) {
					addInput(input_name, 1, Collections
							.singletonList("l('text/plain')"));
					inputTypeMap.put(input_name, String.class);
				} else if (input_type.equals("byte[]")) {
					addInput(input_name, 0, Collections
							.singletonList("'application/octet-stream'"));
					inputTypeMap.put(input_name, byte[].class);
				} else if (input_type.equals("byte[][]")) {
					addInput(input_name, 1, Collections
							.singletonList("l('application/octet-stream')"));
					inputTypeMap.put(input_name, byte[][].class);
				}
			}

			// Get outputs
			Map<String, String>[] results = (Map<String, String>[]) Soap
					.callWebService(configurationBean.getEndpoint(),
							"getResultSpec");
			// Iterate over the outputs
			for (int i = 0; i < results.length; i++) {
				Map<String, String> output_spec = results[i];
				String output_name = output_spec.get("name");
				String output_type = output_spec.get("type").toLowerCase();
				// Check to see whether the output is either report or
				// detailed_status, in
				// which cases we ignore it, this is soaplab metadata rather
				// than application
				// data.
				if ((!output_name.equalsIgnoreCase("detailed_status"))) {
					// && (!output_name.equalsIgnoreCase("report"))) {
					if (output_type.equals("string")) {
						addOutput(output_name, 0, Collections
								.singletonList("'text/plain'"));
					} else if (output_type.equals("string[]")) {
						addOutput(output_name, 1, Collections
								.singletonList("l('text/plain')"));
					} else if (output_type.equals("byte[]")) {
						addOutput(output_name, 0, Collections
								.singletonList("'application/octet-stream'"));
					} else if (output_type.equals("byte[][]")) {
						addOutput(output_name, 1, Collections
								.singletonList("l('application/octet-stream')"));
					}
				}
			}

		} catch (ServiceException se) {
			throw new ActivityConfigurationException(
					configurationBean.getEndpoint()
							+ ": Unable to create a new call to connect\n   to soaplab, error was : "
							+ se.getMessage());
		} catch (RemoteException re) {
			throw new ActivityConfigurationException(
					": Unable to call the get spec method for\n   endpoint : "
							+ configurationBean.getEndpoint()
							+ "\n   Remote exception message "
							+ re.getMessage());
		} catch (NullPointerException npe) {
			// If we had a null pointer exception, go around again - this is a
			// bug somewhere between axis and soaplab
			// that occasionally causes NPEs to happen in the first call or two
			// to a given soaplab installation. It also
			// manifests in the Talisman soaplab clients.
			generatePorts();
		}
	}
	
	public ActivityHealthReport checkActivityHealth() {
		return new ActivityHealthReport("Checking the health of this type of Activity is not yet implemented.",Status.WARNING);
	}

}
