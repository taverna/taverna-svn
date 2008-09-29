/*
 * JenaConvenience.java
 *
 * Created on 07 March 2006, 17:46
 */

package uk.ac.man.cs.img.fetaEngine.store.impl.jena;

/**
 * 
 * @author Pinar
 */

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryExecution;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.QueryResultsMem;
import com.hp.hpl.jena.rdql.ResultBinding;

/** A class defining convenience functions to manipulate the Jena Triple store. */
public final class JenaConvenience {

	/**
	 * Returns a vector of resources matching variableName in rdqlQuery. This
	 * method will execute rdqlQuery on the triple store. We assume that the
	 * query contains a select statement over a single variableName. The result
	 * will be returned in a Vector.
	 */
	public static Vector processQuery(Model m, String rdqlQuery,
			String variableName) {

		Vector results = new Vector();
		try {

			Query query = new Query(rdqlQuery);
			query.setSource(m);
			QueryExecution qe = new QueryEngine(query);
			qe.init();

			long start = System.currentTimeMillis();
			QueryResults queryResults = qe.exec();

			for (Iterator iter = queryResults; iter.hasNext();) {
				ResultBinding rb = (ResultBinding) iter.next();
				Literal l = (Literal) rb.get(variableName);
				// System.out.println("RESOURCE IS --->"+l.toString());
				results.addElement(l);
			}

			/*
			 * System.out.println("\t Results are:"); QueryResultsFormatter fmt =
			 * new QueryResultsFormatter(queryResults) ; PrintWriter pw = new
			 * PrintWriter(System.out) ; fmt.printAll(pw, " | ") ; pw.flush();
			 * fmt.close() ;
			 */
			queryResults.close();
			qe.close();

		} catch (Exception ex) {
			System.err.println("Exception: " + ex);
			ex.printStackTrace(System.err);
		}
		return results;
	}

	public static Vector processQuery(Model m, String rdqlQuery,
			String[] variableNames) {

		Vector results = new Vector();
		try {
			Query query = new Query(rdqlQuery);
			query.setSource(m);
			QueryExecution qe = new QueryEngine(query);
			QueryResults queryResults = qe.exec();

			for (Iterator iter = queryResults; iter.hasNext();) {
				ResultBinding rb = (ResultBinding) iter.next();

				Vector localResult = new Vector();
				results.addElement(localResult);
				for (int i = 0; i < variableNames.length; i++) {
					String variableName = variableNames[i];
					RDFNode r = (RDFNode) rb.get(variableName);
					localResult.addElement(r);
				}
			}
			queryResults.close();
		} catch (Exception ex) {
			System.err.println("Exception: " + ex);
			ex.printStackTrace(System.err);
		}
		return results;
	}

	public static String processQuery(Model m, String rdqlQuery) {

		Query query = new Query(rdqlQuery);
		query.setSource(m);

		QueryExecution qe = new QueryEngine(query);

		QueryResults results = qe.exec();

		Model tempModel = ModelFactory.createDefaultModel();

		QueryResultsMem resultsMem = new QueryResultsMem(results);
		System.out.println(resultsMem.size());

		StringWriter writer = new StringWriter();
		resultsMem.asRDF(tempModel);

		results.close();
		tempModel.write(writer);
		return writer.toString();
	}

}
