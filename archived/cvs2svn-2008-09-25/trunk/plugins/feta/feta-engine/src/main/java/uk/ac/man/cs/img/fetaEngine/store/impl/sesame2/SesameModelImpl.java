/*
 * SesameModelImpl.java
 *
 * Created on 02 December 2005, 17:11
 */

package uk.ac.man.cs.img.fetaEngine.store.impl.sesame2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sesame.query.QueryLanguage;
import org.openrdf.sesame.repository.RURI;
import org.openrdf.sesame.repository.RValue;
import org.openrdf.sesame.repository.Repository;
import org.openrdf.sesame.sailimpl.memory.MemoryStore;
import org.openrdf.sesame.sailimpl.memory.MemoryStoreRDFSInferencer;
import org.openrdf.util.iterator.CloseableIterator;
import org.w3c.dom.Document;

import uk.ac.man.cs.img.fetaEngine.command.IQueryProvider;
import uk.ac.man.cs.img.fetaEngine.command.impl.serql.QueryProviderSERQLImpl;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineException;
import uk.ac.man.cs.img.fetaEngine.store.FetaEngineProperties;
import uk.ac.man.cs.img.fetaEngine.store.FetaPersistentRegistryIndex;
import uk.ac.man.cs.img.fetaEngine.store.IFetaModel;
import uk.ac.man.cs.img.fetaEngine.store.PedroXMLToRDF;
import uk.ac.man.cs.img.fetaEngine.store.load.FetaLoad;
import uk.ac.man.cs.img.fetaEngine.store.load.WebCrawler;
import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

/**
 * 
 * @author Pinar
 */
public class SesameModelImpl implements IFetaModel {

	private static SesameModelImpl instance;

	private Repository myRepository;

	FetaPersistentRegistryIndex registryIndex;
	
	private RegistryHouseKeeperThread houseKeeper;

	/** Creates a new instance of SesameModelImpl */
	public SesameModelImpl() {

		if (myRepository == null) {
			try {
				myRepository = new Repository(new MemoryStoreRDFSInferencer(
						new MemoryStore()));
				myRepository.initialize();				

			} catch (Exception exp) {

				exp.printStackTrace();

			}
		}
	}

	public static SesameModelImpl getInstance() {
		try {
			System.out.println("Debug in Get Instance");
			if (instance == null) {
				instance = new SesameModelImpl();
				instance.initStoreWithProperties();
				System.out.println("New Sesame Model Impl created");
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return instance;
	}

	public FetaPersistentRegistryIndex getRegistryIndex() {
		return registryIndex;
	}

	public void publishDescription(String operationURLstr)
			throws FetaEngineException {

		try {
			// TO DO .. write unit test for this
			String urlCont = null;
			urlCont = uk.ac.man.cs.img.fetaEngine.util.URLReader
					.getURLContentAsString(new URL(operationURLstr));
			if (urlCont != null) {
				publishDescription(operationURLstr, urlCont);
			} else {
				throw new FetaEngineException(
						"the operation URL has null value");
			}

		} catch (Exception exp) {
			exp.printStackTrace();
			throw new FetaEngineException(exp);
		}

	}

	public void publishDescription(String operationURLstr, String content)
			throws FetaEngineException {

		try {

			System.out.println("Registering Service Description with URI--> "
					+ operationURLstr);
			StringReader readr1 = new StringReader(content);

			Repository rep = getSesameRepository();
			RURI context = new RURI(rep, operationURLstr);
			// URIVertex context = new URIVertex(rep, operationURLstr);
			registryIndex.addIndexEntry(operationURLstr);
			rep.add(readr1, operationURLstr, RDFFormat.RDFXML, context);

		} catch (Exception exp) {
			exp.printStackTrace();
			throw new FetaEngineException(exp);
		}

	}

	public String retrieveByLSID(String lsid) {
		try {

			String lsidQuery = "SELECT DISTINCT source \n"
					+ " FROM CONTEXT source \n"
					+ " {somenode} protege-dc:identifier {lsid}"
					+ " WHERE {lsid} like "
					+ lsid
					+ " \n"
					+ " USING NAMESPACE \n"
					+ " mg = <http://www.mygrid.org.uk/mygrid-moby-service#>, \n"
					+ " protege-dc = <http://protege.stanford.edu/plugins/owl/dc/protege-dc.owl#>\n";

			Repository rep = getSesameRepository();

			CloseableIterator<List<RValue>> results = rep.evaluateTupleQuery(
					QueryLanguage.SERQL, lsidQuery);
			Set resultSet = new HashSet();

			while (results.hasNext()) {
				List<RValue> answer = results.next();
				RValue valueOfVariable = answer.get(0);
				resultSet.add(valueOfVariable.toString());
			}

			results.close();

			if (resultSet.size() > 0) {
				RURI context = new RURI(rep, (String) resultSet.toArray()[0]);
				StringWriter strWriter = new StringWriter();
				RDFXMLWriter rdfWRiter = new RDFXMLWriter(strWriter);
				rep.exportContext(context, rdfWRiter);

				return strWriter.toString();

			} else {
				return null;
			}

		} catch (Exception exp) {

			exp.printStackTrace();
			return null;
		}

	}

	private Repository getSesameRepository() {
		if (myRepository == null) {
			try {
				myRepository = new Repository(new MemoryStoreRDFSInferencer(
						new MemoryStore()));
				myRepository.initialize();
				// initStoreWithProperties();

			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
		return myRepository;
	}

	public String freeFormQuery(String rdfQueryStatement)
			throws FetaEngineException {
		Repository rep = getSesameRepository();
		try {
			StringWriter writer = new StringWriter();
			RDFHandler turtleWriter = new TurtleWriter(writer);
			rep.evaluateGraphQuery(QueryLanguage.SERQL, rdfQueryStatement,
					turtleWriter);
			return writer.toString();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public Set cannedQuery(CannedQueryType queryType, String paramValue)
			throws FetaEngineException {
		System.out.println("Debug in CannedQuery");
		Set resultSet = new HashSet();

		try {

			IQueryProvider provider = new QueryProviderSERQLImpl();

			String query = provider.getQueryforCommand(queryType, paramValue);
			System.out.println(query);
			Repository rep = getSesameRepository();

			// TupleSet results = rep.evaluateTupleQuery(QueryLanguage.SERQL,
			// query);

			CloseableIterator<List<RValue>> results = rep.evaluateTupleQuery(
					QueryLanguage.SERQL, query);

			while (results.hasNext()) {
				List<RValue> answer = results.next();
				RValue valueOfVariable = answer.get(0);
				RValue valueOfVariable2 = answer.get(1);
				RValue valueOfVariable3 = answer.get(2);
				resultSet.add(valueOfVariable.toString() + "$"
						+ valueOfVariable2.toString() + "$"
						+ valueOfVariable3.toString());
			}
			results.close();

		} catch (Exception exp) {
			exp.printStackTrace();
			throw new FetaEngineException(exp);
		}

		return resultSet;
	}

	public void removeDescription(String operationURLstr)
			throws FetaEngineException {
		try {

			System.out.println("Debug in remove description");
			Repository rep = getSesameRepository();
			RURI context = new RURI(rep, operationURLstr);
			// URIVertex context = new URIVertex(rep, operationURLstr);
			registryIndex.removeIndexEntry(operationURLstr);
			rep.clearContext(context);

		} catch (Exception exp) {
			exp.printStackTrace();

			throw new FetaEngineException(exp);
		}

	}

	public String getStoreContent() throws FetaEngineException {
		try {

			Repository rep = getSesameRepository();
			StringWriter strWriter = new StringWriter();
			RDFXMLWriter rdfWRiter = new RDFXMLWriter(strWriter);
			rep.export(rdfWRiter);
			return strWriter.toString();
		} catch (Exception exp) {
			exp.printStackTrace();
			throw new FetaEngineException(exp);
		}

	}

	public void registerRDFSOntology(URL ontologyURL)
			throws FetaEngineException {

		try {
			// the baseURI here is to fully qualify any relative URI that might exist in the RDF
			// in reality our service descriptions DO NOT have any relative URI
			// refs in them.
			// String baseURIString = "http://www.mygrid.org.uk/ontology";

			String baseURIString = ontologyURL.toString();

			Repository rep = getSesameRepository();
			rep.add(ontologyURL, baseURIString, RDFFormat.RDFXML);

		} catch (Exception e) {
			e.printStackTrace();
			throw new FetaEngineException(e);
		}

	}

	private void initStoreWithProperties() throws FetaEngineException {

		try {
			/* LOAD THE ONTOS */
			URL ontoURL = null;
			FetaEngineProperties fetaProps = new FetaEngineProperties();

			List ontoList = fetaProps.getFetaOntologyLocations();
			for (int i = 0; i < ontoList.size(); i++) {
				ontoURL = new URL((String) ontoList.get(i));
				registerRDFSOntology(ontoURL);
			}// for


			if (fetaProps.getProperties().containsKey(
					"fetaEngine.persistentIndex.location")) {

				// does a File already exist here
				String indexFileLocation = fetaProps.getProperties()
						.getProperty("fetaEngine.persistentIndex.location");

				// check if fileExists
				boolean fileExists = (new File(indexFileLocation)).exists();

				if (fileExists) {

					// if there is a registry index file, then this is an engine
					// re-start
					// load from the registry index instead of the Feta Load
					// locations in the properties file.

					// for each URL in the file call fetta.publishdescription()
					// of course first read them into a temporary list

					List tmpList = new ArrayList();
					BufferedReader br = new BufferedReader(new FileReader(
							indexFileLocation));
					String thisLine;
					while ((thisLine = br.readLine()) != null) {
						tmpList.add(thisLine);
					} // end while

					registryIndex = new FetaPersistentRegistryIndex(
							indexFileLocation);

					for (int j = 0; j < tmpList.size(); j++) {

						String operationURI = (String) tmpList.get(j);
						if (operationURI.toLowerCase().endsWith(".rdf")) {
							publishDescription(operationURI);
						} else if (operationURI.toLowerCase().endsWith(".xml")) {
							try {

								PedroXMLToRDF rdfConverter = new PedroXMLToRDF();
								List locList = new ArrayList();
								locList.add(operationURI);
								URL operationURL = new URL(operationURI);
								Map docAsRDF = new HashMap();
								docAsRDF = new FetaLoad().readFetaDescriptions(
										locList, 'u');
								String RDFXMLStr = rdfConverter
										.convertToRdfXml((Document) docAsRDF
												.get(operationURL.toString()));
								publishDescription(operationURI, RDFXMLStr);

							} catch (Exception exp) {
								exp.printStackTrace();
							}// try-catch
						} else {
							// do nothing
						}// if-else
					}// for
				} else {

					registryIndex = new FetaPersistentRegistryIndex(
							indexFileLocation);
				}
			} else {

				throw new FetaEngineException();
			}

			

				List locationsToPoll = fetaProps.getLocationstoCrawlandPoll();
			    if (locationsToPoll!= null){
					Set initialLocsToPoll = new HashSet();
					initialLocsToPoll.addAll(locationsToPoll);
					houseKeeper = new RegistryHouseKeeperThread((IFetaModel)this, initialLocsToPoll);
					houseKeeper.start();
			    }

					
			
		} catch (Exception iexp) {
			iexp.printStackTrace();
			throw new FetaEngineException();
		}

	}
	
	
	
	class RegistryHouseKeeperThread extends Thread {

		IFetaModel fetaRegistry;
		Set initialLocsToPoll;
		
		public RegistryHouseKeeperThread(IFetaModel registry, Set locationsToPoll) {
			
			this.fetaRegistry = registry;
			initialLocsToPoll = new HashSet();
			initialLocsToPoll.addAll(locationsToPoll);
			
		}

		public void run() {
			while (true) {

				try {
						Set descsWeCrawlFromWeb = new HashSet();
					
						for (Iterator it = this.initialLocsToPoll.iterator(); it.hasNext();) {
							WebCrawler crwlr = new WebCrawler();					
							// Put these into a set 						
							List tmp = new ArrayList(); 
							tmp = crwlr.getXMLandRDFURLs(it.next().toString());
							for (int i = 0; i<tmp.size(); i++){
								descsWeCrawlFromWeb.add((String)tmp.get(i));
								
							}						
						}
						
						Set descsWeHaveInTheRegistry = fetaRegistry.getRegistryIndex().getIndexKeys();
						
						Set interSection = new HashSet();
						interSection.addAll(descsWeCrawlFromWeb);						
						interSection.retainAll(descsWeHaveInTheRegistry);
						
						Set descsToBePulished = new HashSet();
						descsToBePulished.addAll(descsWeCrawlFromWeb);						
						descsToBePulished.removeAll(interSection);
						

						Set descsToBeUNPulished = new HashSet();
						descsToBeUNPulished.addAll(descsWeHaveInTheRegistry);						
						descsToBeUNPulished.removeAll(interSection);

						
						for (Iterator iter = descsToBeUNPulished.iterator(); iter.hasNext();) {
					
							try {

								//unpublish the description
								fetaRegistry.removeDescription(iter.next().toString());

							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						
						for (Iterator iter2 = descsToBePulished.iterator(); iter2.hasNext();) {
							
							try {
								String operationURI = iter2.next().toString();
								
								//publish the description								
								if (operationURI.toLowerCase().endsWith(".rdf")) {
									fetaRegistry.publishDescription(operationURI);
								} else if (operationURI.toLowerCase().endsWith(".xml")) {
									try {

										PedroXMLToRDF rdfConverter = new PedroXMLToRDF();
										List locList = new ArrayList();
										locList.add(operationURI);
										URL operationURL = new URL(operationURI);
										Map docAsRDF = new HashMap();
										docAsRDF = new FetaLoad().readFetaDescriptions( locList, 'u');
										String RDFXMLStr = rdfConverter
												.convertToRdfXml((Document) docAsRDF
														.get(operationURL.toString()));
										fetaRegistry.publishDescription(operationURI, RDFXMLStr);

									} catch (Exception exp) {
										exp.printStackTrace();
									}// try-catch

								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						
				} catch (Exception exp) {
					exp.printStackTrace();
				}
				try {
					Thread.sleep(100000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
					// ?? do what
				}
			}

		}
	}
	
}
