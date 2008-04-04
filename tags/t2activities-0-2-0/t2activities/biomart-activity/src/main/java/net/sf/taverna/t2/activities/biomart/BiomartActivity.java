package net.sf.taverna.t2.activities.biomart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
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
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				final DataFacade dataFacade = new DataFacade(callback
						.getContext().getDataManager());

				final Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

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
							Object filterValue = dataFacade.resolve(data
									.get(name + "_filter"), String.class);
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
							final Map<String, List<EntityIdentifier>> outputLists = new HashMap<String, List<EntityIdentifier>>();
							for (Attribute attribute : attributes) {
								outputLists.put(attribute.getQualifiedName(),
										new ArrayList<EntityIdentifier>());
							}

							biomartQuery.getMartService().executeQuery(query,
									new ResultReceiver() {

										public void receiveResult(
												Object[] resultLine, long index) {
											Map<String, EntityIdentifier> partialOutputData = new HashMap<String, EntityIdentifier>();
											for (int i = 0; i < resultLine.length; i++) {
												Attribute attribute = attributes
														.get(i);
												String outputName = attribute
														.getQualifiedName();
												int outputDepth = outputMap
														.get(outputName)
														.getDepth();
												try {
													EntityIdentifier data = dataFacade
															.register(resultLine[i], outputDepth - 1);
													partialOutputData.put(
															outputName, data);
													outputLists.get(outputName)
															.add((int) index, data);
												} catch (EmptyListException e) {
													callback.fail("Failure when registering a result", e);
												} catch (MalformedListException e) {
													callback.fail("Failure when registering a result", e);
												} catch (UnsupportedObjectTypeException e) {
													callback.fail("Failure when registering a result", e);
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
								outputData.put(outputName, dataFacade.register(
										outputLists.get(outputName),
										outputDepth));
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
								outputData.put(outputName, dataFacade.register(
										resultList[i], outputDepth));
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
						outputData.put(outputName, dataFacade.register(
								resultList[0], outputDepth));
					}

					callback.receiveResult(outputData, new int[0]);
				} catch (MartServiceException e) {
					callback.fail("Failure calling biomart", e);
				} catch (RetrievalException e) {
					callback.fail("Failure when resolving an input", e);
				} catch (NotFoundException e) {
					callback.fail("Failure when resolving an input", e);
				} catch (EmptyListException e) {
					callback.fail("Failure when registering a result", e);
				} catch (MalformedListException e) {
					callback.fail("Failure when registering a result", e);
				} catch (UnsupportedObjectTypeException e) {
					callback.fail("Failure calling biomart", e);
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
						new ArrayList<Class<? extends ReferenceScheme<?>>>(),
						String.class);
			} else {
				addInput(name, 0, true,
						new ArrayList<Class<? extends ReferenceScheme<?>>>(),
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
