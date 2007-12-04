package net.sf.taverna.t2.activities.biomart;

import java.util.Collections;
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
import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.HealthReportImpl;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.MartServiceUtils;
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

	private BiomartActivityConfigurationBean configurationBean;

//	private QueryListener queryListener;

	private MartQuery biomartQuery;

	public BiomartActivity() {
	}

	@Override
	public void configure(BiomartActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		biomartQuery = configurationBean.getQuery();
		buildInputPorts();
		buildOutputPorts();
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
				DataFacade dataFacade = new DataFacade(callback.getContext().getDataManager());

				Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

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
									.get(name + "_filter"),String.class);
							if (filterValue instanceof String) {
								filter.setValue((String) filterValue);
							} else if (filterValue instanceof List) {
								List<?> idList = (List<?>) filterValue;
								filter.setValue(QueryConfigUtils.listToCsv(idList));
							}
						}
					}

					Object[] resultList = biomartQuery.getMartService().executeQuery(
							query);
					if (biomartQuery.getQuery().getFormatter() == null) {
						// shouldn't need to reorder attributes for MartJ 0.5
						List<Attribute> attributes = biomartQuery.getAttributesInLinkOrder();
						assert resultList.length == attributes.size();
						for (int i = 0; i < resultList.length; i++) {
							Attribute attribute = attributes.get(i);
							outputData.put(attribute.getQualifiedName(), dataFacade.register(
									resultList[i]));
						}
					} else {
						assert resultList.length == 1;
						Dataset dataset = biomartQuery.getQuery().getDatasets().get(0);
						outputData.put(dataset.getName(), dataFacade.register(resultList[0]));
					}

					callback.receiveResult(outputData, new int[0]);
				} catch (MartServiceException e) {
					callback.fail("Failure calling biomart", e);
				} catch (RetrievalException e) {
					callback.fail("Failure calling biomart", e);
				} catch (NotFoundException e) {
					callback.fail("Failure calling biomart", e);
				} catch (EmptyListException e) {
					callback.fail("Failure calling biomart", e);
				} catch (MalformedListException e) {
					callback.fail("Failure calling biomart", e);
				} catch (UnsupportedObjectTypeException e) {
					callback.fail("Failure calling biomart", e);
				}
			}

		});

	}

	private void buildInputPorts() {
		List<Filter> filters = biomartQuery.getQuery()
				.getFilters();
		// Create new input ports corresponding to filters
		for (Filter filter : filters) {
			String name = filter.getQualifiedName() + "_filter";
			if (filter.isList()) {
				addInput(name, 1, Collections.singletonList("l('text/plain')"));
			} else {
				addInput(name, 0, Collections.singletonList("'text/plain'"));
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
					addOutput(name, 2, 2, Collections
							.singletonList("l(l('text/plain'))"));
				} else {
					addOutput(name, 1, 1, Collections
							.singletonList("l('text/plain')"));
				}
			}
		} else if (attributes.size() > 0) {
			// create one port using the dataset name
			Attribute attribute = attributes.get(0);
			String name = attribute.getContainingDataset().getName();
			addOutput(name, 0, 0, Collections.singletonList(MartServiceUtils
					.getMimeTypeForFormatter(formatter)));
		}
	}
	
	public HealthReport checkActivityHealth() {
		return new HealthReportImpl(getClass().getSimpleName(),"Checking the health of this type of Activity is not yet implemented.",Status.WARNING);
	}
}
