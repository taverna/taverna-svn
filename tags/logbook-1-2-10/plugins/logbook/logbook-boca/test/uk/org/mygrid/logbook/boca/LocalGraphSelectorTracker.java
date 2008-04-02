package uk.org.mygrid.logbook.boca;

import info.aduna.collections.iterators.CloseableIterator;

import java.io.FileInputStream;
import java.util.Properties;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

import com.ibm.adtech.boca.client.DatasetService;
import com.ibm.adtech.boca.model.INamedGraph;
import com.ibm.adtech.boca.services.trackers.SelectorTracker;

public class LocalGraphSelectorTracker {

	public static void main(String[] args) throws Exception {
		DatasetService datasetService1 = null;
		DatasetService datasetService2 = null;

		try {
			Properties webserviceProperties = new Properties();
			webserviceProperties.load(new FileInputStream("conf/embeddedclient.properties"));
			datasetService1 = new DatasetService(webserviceProperties);
			datasetService1.getNotificationService()
					.setUsingNotification(false);
			datasetService2 = new DatasetService(webserviceProperties);
			URI namedGraphURI = datasetService1.getValueFactory().createURI(
					"http://example.org/ng1");
			boolean createIfNecessary = true;
			INamedGraph localGraph = null;
			INamedGraph remoteGraph = null;
			try {
				remoteGraph = datasetService2.getRemoteGraph(namedGraphURI,
						createIfNecessary);
				URI res1 = datasetService1.getValueFactory().createURI(
						"http://example.org/res1");
				URI prop1 = datasetService1.getValueFactory().createURI(
						"http://example.org/prop1");
				datasetService2.begin();
				try {
					remoteGraph.add(res1, prop1, datasetService2
							.getValueFactory().createLiteral("value1"));
					remoteGraph.add(res1, prop1, datasetService2
							.getValueFactory().createLiteral("value2"));
					datasetService2.commit();
				} catch (Exception e) {
					datasetService2.abort();
					throw e;
				}
				datasetService2.getDatasetReplicator().replicate(true);
				localGraph = datasetService1.getLocalGraph(namedGraphURI,
						createIfNecessary, false);
				SelectorTracker tracker = new SelectorTracker(null, null, null,
						datasetService1.getValueFactory().createLiteral(
								"value1"));
				datasetService1.getDatasetReplicator().addTracker(
						datasetService1.getValueFactory().createURI(
								"http://example.org#testSet"), tracker);

				CloseableIterator<Statement> itr = localGraph.getStatements();
				System.err
						.println("Statments before replication (should be empty):");
				while (itr.hasNext()) {
					System.err.println(itr.next().toString());
				}
				itr.close();
				datasetService1.getDatasetReplicator().replicate(true);
				itr = localGraph.getStatements();
				System.err
						.println("Statments after replication, should only be 'value1'");
				while (itr.hasNext()) {
					System.err.println(itr.next().toString());
				}
			} finally {
				try {
					if (remoteGraph != null)
						remoteGraph.close();
				} finally {
					if (localGraph != null)
						localGraph.close();
				}
			}
		} finally {
			try {
				if (datasetService1 != null)
					datasetService1.close();
			} finally {
				if (datasetService2 != null)
					datasetService2.close();
			}
		}
	}

}
