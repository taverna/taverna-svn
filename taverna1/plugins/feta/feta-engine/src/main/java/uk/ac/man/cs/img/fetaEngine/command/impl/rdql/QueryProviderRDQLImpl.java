/*
 * QueryProviderRDQLImpl.java
 *
 * Created on 02 December 2005, 16:17
 */

package uk.ac.man.cs.img.fetaEngine.command.impl.rdql;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.types.NMToken;

import uk.ac.man.cs.img.fetaEngine.command.IQueryGenerator;
import uk.ac.man.cs.img.fetaEngine.command.IQueryProvider;
import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

/**
 * 
 * @author Pinar
 */
public class QueryProviderRDQLImpl implements IQueryProvider {

	Map commandsToQueries;

	// /Select the location service name and operation name
	// generate an operationIdentifier class using this info
	// return the list of operation identifiers
	protected static String queryTemplate = "SELECT ?descLoc, ?servName, ?opName WHERE\n"
			+ "      (?s mg:hasServiceDescriptionLocation ?descLoc)\n"
			+ "      (?s mg:hasServiceNameText ?servName)\n"
			+ "      (?s mg:hasOperation ?op)\n"
			+ "      (?op mg:hasOperationNameText ?opName)\n";

	protected static String nameSpaceStmt = "      USING\n      mg for <http://www.mygrid.org.uk/ontology#>\n"
			+ "      USING\n      moby for <http://biomoby.org/RESOURCES/MOBY-S/ServiceDescription#>";

	// this namespace is for fully qualifying classes in the service schema
	// onto.

	/** Creates a new instance of QueryProviderRDQLImpl */
	public QueryProviderRDQLImpl() {
		commandsToQueries = new HashMap();

		commandsToQueries.put(CannedQueryType._ByApplication,
				new QueryProviderRDQLImpl.RdqlFindByApplication());
		commandsToQueries.put(CannedQueryType._ByDescription,
				new QueryProviderRDQLImpl.RdqlFindByDescription());
		commandsToQueries.put(CannedQueryType._ByInput,
				new QueryProviderRDQLImpl.RdqlFindByInputType());
		commandsToQueries.put(CannedQueryType._ByMethod,
				new QueryProviderRDQLImpl.RdqlFindByMethod());
		commandsToQueries.put(CannedQueryType._ByName,
				new QueryProviderRDQLImpl.RdqlFindByName());
		commandsToQueries.put(CannedQueryType._ByOutput,
				new QueryProviderRDQLImpl.RdqlFindByOutputType());
		commandsToQueries.put(CannedQueryType._ByResource,
				new QueryProviderRDQLImpl.RdqlFindByResource());
		commandsToQueries.put(CannedQueryType._ByResourceContent,
				new QueryProviderRDQLImpl.RdqlFindByResourceContent());
		commandsToQueries.put(CannedQueryType._ByType,
				new QueryProviderRDQLImpl.RdqlFindByServiceType());
		commandsToQueries.put(CannedQueryType._GetAll,
				new QueryProviderRDQLImpl.RdqlFindAllOperations());
	}

	public String getQueryforCommand(CannedQueryType commandType, String param) {

		NMToken queryType = commandType.getValue();
		return (String) ((IQueryGenerator) commandsToQueries.get(queryType))
				.getParametrizedQueryString(param);

	}

	public static class RdqlFindByName implements IQueryGenerator {

		public String getParametrizedQueryString(String name) {

			return queryTemplate
					+ "      (?op mg:hasOperationNameText ?name)\n"
					+ "  AND (?name =~ /" + name + "/i)\n" + nameSpaceStmt;
		}

	}

	public static class RdqlFindByMethod implements IQueryGenerator {

		public String getParametrizedQueryString(String method) {

			return queryTemplate + "      (?op mg:usesMethod ?m)\n"
					+ "      (?m rdf:type <" + method + ">)\n" + nameSpaceStmt;
		}

	}

	public static class RdqlFindAllOperations implements IQueryGenerator {

		public String getParametrizedQueryString(String name) {

			return queryTemplate + nameSpaceStmt;
		}

	}

	public class RdqlFindByApplication implements IQueryGenerator {

		public String getParametrizedQueryString(String application) {

			return queryTemplate + "      (?op mg:isFunctionOf ?app)\n"
					+ "      (?app rdf:type <" + application + ">)\n"
					+ nameSpaceStmt;
		}

	}

	public static class RdqlFindByDescription implements IQueryGenerator {

		public String getParametrizedQueryString(String description) {
			return queryTemplate
					+ "      (?op mg:hasOperationDescriptionText ?desc)\n"
					+ "  AND (?desc =~ /" + description + "/i)\n"
					+ nameSpaceStmt;
		}

	}

	public static class RdqlFindByInputType implements IQueryGenerator {

		public String getParametrizedQueryString(String inputType) {

			return queryTemplate + "      (?op mg:inputParameter ?par)\n"
					+ "      (?par moby:inNamespaces ?namesp)\n"
					+ "      (?namesp rdf:type <" + inputType + ">)\n"
					+ nameSpaceStmt;
		}

	}

	public static class RdqlFindByOutputType implements IQueryGenerator {

		public String getParametrizedQueryString(String outputType) {
			return queryTemplate + "      (?op mg:outputParameter ?par)\n"
					+ "      (?par moby:inNamespaces ?namesp)\n"
					+ "      (?namesp rdf:type <" + outputType + ">)\n"
					+ nameSpaceStmt;

		}

	}

	public static class RdqlFindByResource implements IQueryGenerator {

		public String getParametrizedQueryString(String resource) {
			return queryTemplate + "      (?op mg:usesResource ?r)\n"
					+ "      (?r rdf:type <" + resource + ">)\n"
					+ nameSpaceStmt;
		}

	}

	public static class RdqlFindByResourceContent implements IQueryGenerator {

		public String getParametrizedQueryString(String resourceContent) {
			return queryTemplate + "      (?op mg:hasResourceContent ?r)\n"
					+ "      (?r rdf:type <" + resourceContent + ">)\n"
					+ nameSpaceStmt;
		}

	}

	public static class RdqlFindByServiceType implements IQueryGenerator {

		public String getParametrizedQueryString(String type) {
			return queryTemplate + "      (?s mg:hasServiceType ?type)\n"
					+ "  AND (?type =~ /" + type + "/i)\n" + nameSpaceStmt;
		}

	}

	public static class RdqlFindByTask implements IQueryGenerator {

		public String getParametrizedQueryString(String task) {
			return queryTemplate + "      (?op mg:performsTask ?t)\n"
					+ "      (?t rdf:type <" + task + ">)\n" + nameSpaceStmt;
		}

	}

}
