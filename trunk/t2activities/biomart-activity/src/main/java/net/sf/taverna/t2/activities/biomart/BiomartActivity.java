package net.sf.taverna.t2.activities.biomart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.MartServiceXMLHandler;
import org.biomart.martservice.ResultReceiver;
import org.biomart.martservice.ResultReceiverException;
import org.biomart.martservice.config.QueryConfigUtils;
import org.biomart.martservice.query.Attribute;
import org.biomart.martservice.query.Dataset;
import org.biomart.martservice.query.Filter;
import org.biomart.martservice.query.Query;

/**
 * <p>
 * An Activity providing Biomart functionality.
 * </p>
 * 
 * @author David Withers
 */
public class BiomartActivity extends
		AbstractAsynchronousActivity<BiomartActivityConfigurationBean> {

	private static boolean STREAM_RESULTS = true;
	
	private BiomartActivityConfigurationBean configurationBean;

	private Map<String, OutputPort> outputMap;

	// private QueryListener queryListener;

	private MartQuery biomartQuery;

	public BiomartActivity() {
	}

	@Override
	public void configure(BiomartActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		biomartQuery = MartServiceXMLHandler.elementToMartQuery(configurationBean.getQuery(), null);
		buildInputPorts();
		buildOutputPorts();
		buildOutputPortMap();
	}

	@Override
	public BiomartActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				final ReferenceService referenceService = callback.getContext().getReferenceService();

				final Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				try {
					// Get a query including data source etc, creating
					// a copy so that any filter value settings are not
					// overwritten by input values
					biomartQuery.calculateLinks();
					Query query = new Query(biomartQuery.getQuery());

					// Configure any filters
					List<Filter> filters = query.getFilters();
					for (Filter filter : filters) {
						String name = filter.getQualifiedName();
						if (data.containsKey(name + "_filter")) {
							Object filterValue = referenceService.renderIdentifier(data
									.get(name + "_filter"), String.class, callback.getContext());
							if (filterValue instanceof String) {
								filter.setValue((String) filterValue);
							} else if (filterValue instanceof List) {
								List<?> idList = (List<?>) filterValue;
								filter.setValue(QueryConfigUtils
										.listToCsv(idList));
							}
						}
					}

					if (biomartQuery.getQuery().getFormatter() == null) {
						if (STREAM_RESULTS) {
							final List<Attribute> attributes = biomartQuery
									.getAttributesInLinkOrder();
							final Map<String, List<T2Reference>> outputLists = new HashMap<String, List<T2Reference>>();
							for (Attribute attribute : attributes) {
								outputLists.put(attribute.getQualifiedName(),
										new ArrayList<T2Reference>());
							}

							biomartQuery.getMartService().executeQuery(query,
									new ResultReceiver() {

										public void receiveResult(
												Object[] resultLine, long index) {
											Map<String, T2Reference> partialOutputData = new HashMap<String, T2Reference>();
											for (int i = 0; i < resultLine.length; i++) {
												Attribute attribute = attributes
														.get(i);
												String outputName = attribute
														.getQualifiedName();
												int outputDepth = outputMap
														.get(outputName)
														.getDepth();
												try {
													T2Reference data = referenceService
															.register(resultLine[i], outputDepth - 1, true, callback.getContext());
													partialOutputData.put(
															outputName, data);
													outputLists.get(outputName)
															.add((int) index, data);
												} catch (ReferenceServiceException e) {
													callback.fail("Failure when calling the reference service", e);
												}
											}
											callback.receiveResult(
													partialOutputData,
													new int[] { (int) index });
										}
									});

							for (Attribute attribute : attributes) {
								String outputName = attribute
										.getQualifiedName();
								int outputDepth = outputMap.get(outputName)
										.getDepth();
								outputData.put(outputName, referenceService.register(
										outputLists.get(outputName),
										outputDepth, true, callback.getContext()));
							}

						} else {
							// shouldn't need to reorder attributes for MartJ
							// 0.5
							Object[] resultList = biomartQuery.getMartService()
									.executeQuery(query);
							List<Attribute> attributes = biomartQuery
									.getAttributesInLinkOrder();
							assert resultList.length == attributes.size();
							for (int i = 0; i < resultList.length; i++) {
								Attribute attribute = attributes.get(i);
								String outputName = attribute
										.getQualifiedName();
								int outputDepth = outputMap.get(outputName)
										.getDepth();
								outputData.put(outputName, referenceService.register(
										resultList[i], outputDepth, true, callback.getContext()));
							}
						}
					} else {
						Object[] resultList = biomartQuery.getMartService()
								.executeQuery(query);
						assert resultList.length == 1;
						Dataset dataset = biomartQuery.getQuery().getDatasets()
								.get(0);
						String outputName = dataset.getName();
						int outputDepth = outputMap.get(outputName).getDepth();
						outputData.put(outputName, referenceService.register(
								resultList[0], outputDepth, true, callback.getContext()));
					}

					callback.receiveResult(outputData, new int[0]);
				} catch (MartServiceException e) {
					callback.fail("Failure calling biomart", e);
				} catch (ReferenceServiceException e) {
					callback.fail("Failure when calling the reference service", e);
				} catch (ResultReceiverException e) {
					callback.fail("Failure when receiving a result from biomart", e);
				}
			}

		});

	}

	private void buildInputPorts() {
		List<Filter> filters = biomartQuery.getQuery().getFilters();
		// Create new input ports corresponding to filters
		for (Filter filter : filters) {
			String name = filter.getQualifiedName() + "_filter";
			if (filter.isList()) {
				addInput(name, 1, true,
						new ArrayList<Class<? extends ExternalReferenceSPI>>(),
						String.class);
			} else {
				addInput(name, 0, true,
						new ArrayList<Class<? extends ExternalReferenceSPI>>(),
						String.class);
			}
		}
	}

	private void buildOutputPorts() {
		Query query = biomartQuery.getQuery();
		List<Attribute> attributes = query.getAttributes();
		String formatter = query.getFormatter();
		if (formatter == null) {
			// Create new output ports corresponding to attributes
			for (Attribute attribute : attributes) {
				String name = attribute.getQualifiedName();
				if (attribute.getAttributes() != null) {
					addOutput(name, 2, STREAM_RESULTS?1:2);
				} else {
					addOutput(name, 1, STREAM_RESULTS?0:1);
				}
			}
		} else if (attributes.size() > 0) {
			// create one port using the dataset name
			Attribute attribute = attributes.get(0);
			String name = attribute.getContainingDataset().getName();
			addOutput(name, 0, 0);
		}
	}

	private void buildOutputPortMap() {
		outputMap = new HashMap<String, OutputPort>();
		for (OutputPort outputPort : getOutputPorts()) {
			outputMap.put(outputPort.getName(), outputPort);
		}
	}

}
