/*
 * QueryProviderSERQLImpl.java
 *
 * Created on 04 December 2005, 17:41
 */

package uk.ac.man.cs.img.fetaEngine.command.impl.serql;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.types.NMToken;

import uk.ac.man.cs.img.fetaEngine.command.IQueryGenerator;
import uk.ac.man.cs.img.fetaEngine.command.IQueryProvider;
import uk.ac.man.cs.img.fetaEngine.commons.FetaModelRDF;
import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

/**
 * 
 * @author Pinar
 */
public class QueryProviderSERQLImpl implements IQueryProvider {

	Map commandsToQueries;

	// /Select the location service name and operation name
	// generate an operationIdentifier class using this info
	// return the list of operation identifiers
	protected static String queryTemplate = " SELECT descloc, servName, operName \n"
			+ " FROM {serv} mg:hasServiceDescriptionLocation {descloc}, \n"
			+ " {serv} mg:hasServiceNameText {servName},\n"
			+ " {serv} mg:hasOperation {oper} mg:hasOperationNameText {operName}\n";

	protected static String nameSpaceStmt = " USING NAMESPACE \n" + " mg = <"
			+ FetaModelRDF.MYGRID_MOBY_SERVICE_NS + ">, \n" + " protege-dc = <"
			+ FetaModelRDF.DC_PATCHED + ">\n";

	/** Creates a new instance of QueryProviderSERQLImpl */
	public QueryProviderSERQLImpl() {
		commandsToQueries = new HashMap();

		commandsToQueries.put(CannedQueryType._ByApplication,
				new QueryProviderSERQLImpl.SerqlFindByApplication());
		commandsToQueries.put(CannedQueryType._ByDescription,
				new QueryProviderSERQLImpl.SerqlFindByDescription());
		commandsToQueries.put(CannedQueryType._ByTask,
				new QueryProviderSERQLImpl.SerqlFindByTask());
		commandsToQueries.put(CannedQueryType._ByInput,
				new QueryProviderSERQLImpl.SerqlFindByInputType());
		commandsToQueries.put(CannedQueryType._ByMethod,
				new QueryProviderSERQLImpl.SerqlFindByMethod());
		commandsToQueries.put(CannedQueryType._ByName,
				new QueryProviderSERQLImpl.SerqlFindByName());
		commandsToQueries.put(CannedQueryType._ByOutput,
				new QueryProviderSERQLImpl.SerqlFindByOutputType());
		commandsToQueries.put(CannedQueryType._ByResource,
				new QueryProviderSERQLImpl.SerqlFindByResource());
		commandsToQueries.put(CannedQueryType._ByResourceContent,
				new QueryProviderSERQLImpl.SerqlFindByResourceContent());
		commandsToQueries.put(CannedQueryType._ByType,
				new QueryProviderSERQLImpl.SerqlFindByServiceType());
		commandsToQueries.put(CannedQueryType._GetAll,
				new QueryProviderSERQLImpl.SerqlFindAllOperations());
	}

	public String getQueryforCommand(CannedQueryType commandType, String param) {

		NMToken queryType = commandType.getValue();
		return (String) ((IQueryGenerator) commandsToQueries.get(queryType))
				.getParametrizedQueryString(param);

	}

	public static class SerqlFindByName implements IQueryGenerator {

		public String getParametrizedQueryString(String name) {

			return queryTemplate + "  WHERE operName  LIKE \"*" + name
					+ "*\" IGNORE CASE \n" +

					" UNION \n" +

					queryTemplate + " WHERE servName LIKE \"*" + name
					+ "*\" IGNORE CASE \n" +

					nameSpaceStmt;
		}

	}

	public static class SerqlFindByDescription implements IQueryGenerator {

		public String getParametrizedQueryString(String description) {
			return queryTemplate
					+ " , {oper} mg:hasOperationDescriptionText {descText} \n"
					+ " WHERE descText LIKE \"*"
					+ description
					+ "*\" IGNORE CASE  \n"
					+

					" UNION \n"

					+ queryTemplate
					+ " , {serv} mg:hasServiceDescriptionText {servDescText} \n"
					+ " WHERE servDescText LIKE \"*" + description
					+ "*\" IGNORE CASE  \n" +

					nameSpaceStmt;

		}

	}

	public static class SerqlFindByMethod implements IQueryGenerator {

		public String getParametrizedQueryString(String method) {
			return queryTemplate + " , {oper} mg:usesMethod {meth}, \n"
					+ "  {meth} rdf:type {<" + method + ">} \n" + nameSpaceStmt;
		}

	}

	public static class SerqlFindAllOperations implements IQueryGenerator {

		public String getParametrizedQueryString(String name) {

			return queryTemplate + nameSpaceStmt;
		}

	}

	public static class SerqlFindByApplication implements IQueryGenerator {

		public String getParametrizedQueryString(String application) {

			return queryTemplate + " , {oper} mg:isFunctionOf {meth}, \n"
					+ "  {meth} rdf:type {<" + application + ">} \n"
					+ nameSpaceStmt;
		}

	}

	public static class SerqlFindByInputType implements IQueryGenerator {

		/*
		 * Currently there is no consensus on what the behaviour of search over
		 * Input metadata should be. According to the PLUG-IN match definition
		 * from the Semantic Web Services literature when we search for a
		 * service with input of a particular semantic type, the result set
		 * should also contain services that are annotated as capable of
		 * accepting of semantic types that are MORE GENERAL then (super-class
		 * of) the type designated in the query.
		 * 
		 * However this behaviour caused confusion among some. Therefore, now,
		 * for input searches we do specialization just like other canned
		 * queries.
		 */

		public String getParametrizedQueryString(String inputType) {
			return queryTemplate + " , {oper} mg:inputParameter {par}, \n"
					+ "  {par} mg:inNamespaces {parNameSpace}, \n"
					+ "  {parNameSpace} rdf:type {<" + inputType + ">} \n"
					+ nameSpaceStmt;
		}

	}

	public static class SerqlFindByOutputType implements IQueryGenerator {

		public String getParametrizedQueryString(String outputType) {
			return queryTemplate + " , {oper} mg:outputParameter {par}, \n"
					+ "  {par} mg:inNamespaces {parNameSpace}, \n"
					+ "  {parNameSpace} rdf:type {<" + outputType + ">} \n"
					+ nameSpaceStmt;
		}

	}

	public static class SerqlFindByResource implements IQueryGenerator {

		public String getParametrizedQueryString(String resource) {
			return queryTemplate + " , {oper} mg:usesResource {par}, \n"
					+ "  {par} rdf:type {<" + resource + ">} \n"
					+ nameSpaceStmt;
		}
	}

	public static class SerqlFindByResourceContent implements IQueryGenerator {

		public String getParametrizedQueryString(String resourceContent) {
			return queryTemplate + " , {oper} mg:hasResourceContent {par}, \n"
					+ " {par} rdf:type {<" + resourceContent + ">} \n"
					+ nameSpaceStmt;
		}

	}

	public static class SerqlFindByServiceType implements IQueryGenerator {

		public String getParametrizedQueryString(String type) {

			return queryTemplate + " , {serv} protege-dc:format {tip} \n"
					+ " WHERE tip LIKE \"*" + type + "*\" IGNORE CASE \n"
					+ nameSpaceStmt;
		}

	}

	public static class SerqlFindByTask implements IQueryGenerator {

		public String getParametrizedQueryString(String task) {
			return queryTemplate + " , {oper} mg:performsTask {task}, \n"
					+ "  {task} rdf:type {<" + task + ">} \n" + nameSpaceStmt;
		}
	}
}
