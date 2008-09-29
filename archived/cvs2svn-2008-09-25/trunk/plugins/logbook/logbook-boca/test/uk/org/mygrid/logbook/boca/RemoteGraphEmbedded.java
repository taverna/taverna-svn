/******************************************************************************* 
 * Copyright (c) 2004, 2006 IBM Corporation. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * File: $Source: /local/stain/backup/taverna.cvs.sf.net/plugins/logbook/logbook-boca/test/uk/org/mygrid/logbook/boca/RemoteGraphEmbedded.java,v $
 * Created by:  Ben Szekely ( <a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com </a>)
 * Created on: 10/27/06
 * Revision: $Id: RemoteGraphEmbedded.java,v 1.1 2007-12-14 12:59:52 stain Exp $
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 *******************************************************************************/
package uk.org.mygrid.logbook.boca;

import java.io.FileInputStream;
import java.util.Properties;
import org.openrdf.model.URI;
import com.ibm.adtech.boca.client.DatasetService;
import com.ibm.adtech.boca.model.INamedGraph;

/**
 * 
 * This simple example instantiates a Boca client DatasetService that communicates directly with the
 * database in the same JVM. This example works without having to run any servers.
 * 
 * In this example, we use a "RemoteGraph", to access a named graph on the Boca server via the
 * embedded DatasetService API.
 * 
 * @author Ben Szekely ( <a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com </a>
 * 
 */
public class RemoteGraphEmbedded {

	public static void main(String[] args) throws Exception {

		DatasetService datasetService = null;
		// use a try-finally to make sure the datasetServices are closed properly
		try {

			// Read properties file.
			Properties embeddedProperties = new Properties();
			embeddedProperties.load(new FileInputStream("conf/embeddedclient.properties"));

			// instantiate a dataset service
			datasetService = new DatasetService(embeddedProperties);

			// open a model to a remote named graph.
			// always use try-finally to make sure the model is closed.
			URI namedGraphURI = datasetService.getValueFactory().createURI("http://example.org/ng1");
			boolean createIfNecessary = true;
			INamedGraph remoteGraph = null;
			try {
				remoteGraph = datasetService.getRemoteGraph(namedGraphURI, createIfNecessary);
				URI res1 = datasetService.getValueFactory().createURI("http://example.org/res1");
				URI prop1 = datasetService.getValueFactory().createURI("http://example.org/prop1");

				// all operations to Boca models occur in transactions. If any operations are
				// applied outside of a begin/commit, each operation is assigned it's own
				// transaction. Thus, it is recommended that begin/commit subsume as many operations
				// as possible.
				datasetService.begin();
				try {
					// do whatever you want to the model, read write,etc..
					remoteGraph.add(res1, prop1, datasetService.getValueFactory().createLiteral("value1" ));
					remoteGraph.add(res1, prop1,  datasetService.getValueFactory().createLiteral("value2"));
					datasetService.commit();
				} catch (Exception e) {
					datasetService.abort();
					throw e;
				}

				// Push all transaction to the server synchronously. Even before this replication
				// occurs, all models created with the given DatasetService will reflect the
				// committed transactions.
				datasetService.getDatasetReplicator().replicate(true);
			} finally {
				if (remoteGraph != null)
					remoteGraph.close();
			}
		} finally {
			if (datasetService != null) {
				datasetService.close();
			}
		}
	}

}
