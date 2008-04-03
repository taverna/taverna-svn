package net.sf.taverna.t2.activities.ogsadai;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;
import uk.org.ogsadai.client.toolkit.DataRequestExecutionResource;
import uk.org.ogsadai.client.toolkit.PipelineWorkflow;
import uk.org.ogsadai.client.toolkit.RequestExecutionType;
import uk.org.ogsadai.client.toolkit.ServerProxy;
import uk.org.ogsadai.client.toolkit.activities.delivery.DeliverToRequestStatus;
import uk.org.ogsadai.client.toolkit.activities.delivery.WriteToDataSource;
import uk.org.ogsadai.client.toolkit.activities.management.CreateDataSource;
import uk.org.ogsadai.client.toolkit.activities.sql.SQLQuery;
import uk.org.ogsadai.client.toolkit.activities.transform.TupleToCSV;
import uk.org.ogsadai.resource.ResourceID;

public class OgsaDaiActivity extends AbstractAsynchronousActivity<OgsaDaiActivityConfiguration> {

	@Override
	public void configure(OgsaDaiActivityConfiguration conf) throws ActivityConfigurationException {
		List<String>mimeTypes = new ArrayList<String>();
		mimeTypes.add("'text/plain'");
		addInput("SQLExpression",0, mimeTypes);
		addOutput("DataURL", 0, 0, new ArrayList<String>());
	}

	@Override
	public void executeAsynch(
			final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback)
	{
		callback.requestRun(new Runnable()
		{

			public void run() {
				DataFacade dataFacade = new DataFacade(callback.getLocalDataManager());
				EntityIdentifier ei = data.get("SQLExpression");
				try {
					String sqlExpression = (String)dataFacade.resolve(ei);
					String dataURL = getDataURL(sqlExpression);
					URLReferenceScheme ref = new URLReferenceScheme(new URL(dataURL));
					Set<ReferenceScheme> refs = new HashSet<ReferenceScheme>();
					refs.add(ref);
					DataDocumentIdentifier docid = callback.getLocalDataManager().registerDocument(refs);
					Map<String, EntityIdentifier> output = new HashMap<String, EntityIdentifier>();
					output.put("DataURL", docid);
					callback.receiveResult(output, new int[0]);

				} catch (RetrievalException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					callback.fail("OGSA-DAI call failed: "
							+ e.getClass().getName() + ", " + e.getMessage());
				}
			}


		});
	}

	@Override
	public OgsaDaiActivityConfiguration getConfiguration() {
		return null;
	}

	@Override
	protected ActivityPortBuilder getPortBuilder() {
		return ActivityPortBuilderImpl.getInstance();
	}

	private static String getDataURL(String sqlExpression)
			throws Exception
	{

		System.setProperty("axis.ClientConfigFile", "/ogsadai-client-config.wsdd");
		ServerProxy server = new ServerProxy();
		String baseURL = "http://test.ogsadai.org.uk:8080/dai/services/";
		server.setDefaultBaseServicesURL(new URL(baseURL));
		ResourceID drerID = new ResourceID("DataRequestExecutionResource");
		DataRequestExecutionResource drer = server
				.getDataRequestExecutionResource(drerID);

		CreateDataSource create = new CreateDataSource();
		DeliverToRequestStatus deliver = new DeliverToRequestStatus();
		deliver.connectInput(create.getResultOutput());

		PipelineWorkflow pipeline = new PipelineWorkflow();
		pipeline.add(create);
		pipeline.add(deliver);
		drer.execute(pipeline, RequestExecutionType.SYNCHRONOUS);

		SQLQuery query = new SQLQuery();
		query.addExpression(sqlExpression);
		query.setResourceID("MySQLDataResource");
		TupleToCSV csv = new TupleToCSV();
		csv.connectDataInput(query.getDataOutput());
		WriteToDataSource write = new WriteToDataSource();
		ResourceID dataSource = create.nextResult();
		write.setResourceID(dataSource);
		write.connectInput(csv.getResultOutput());

		pipeline = new PipelineWorkflow();
		pipeline.add(query);
		pipeline.add(csv);
		pipeline.add(write);
		drer.execute(pipeline, RequestExecutionType.ASYNCHRONOUS);
		String dataURL = baseURL + "Omii?resourceId=" + dataSource.toString();
		System.out.println("data is " + dataURL);
		return dataURL;
	}

	public static void main(String[] args) throws Exception	{
		System.out.println(getDataURL("select * from littleblackbook where id<10"));
	}

}
