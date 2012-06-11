package cs.manchester.sparql.servicetype.sparqlservicetype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class ExampleActivity extends
		AbstractAsynchronousActivity<ExampleActivityConfigurationBean>
		implements AsynchronousActivity<ExampleActivityConfigurationBean> {

	/*
	 * Best practice: Keep port names as constants to avoid misspelling. This
	 * would not apply if port names are looked up dynamically from the service
	 * operation, like done for WSDL services.
	 */
	private static final String IN_FIRST_INPUT = "firstInput";
	private static final String IN_EXTRA_DATA = "extraData";

	private static final String OUT_RAW_OUTPUT = "rawOutput";
	private static final String OUT_REPORT = "report";
	
	private ExampleActivityConfigurationBean configBean;

	@Override
	public void configure(ExampleActivityConfigurationBean configBean)
			throws ActivityConfigurationException {

		// Any pre-config sanity checks
		//if (configBean.getExampleString().equals("invalidExample")) {
		//	throw new ActivityConfigurationException(
		//			"Example string can't be 'invalidExample'");
		//}
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;
		
		// OPTIONAL: 
		// Do any server-side lookups and configuration, like resolving WSDLs

		// myClient = new MyClient(configBean.getExampleUri());
		// this.service = myClient.getService(configBean.getExampleString());

		
		// REQUIRED: (Re)create input/output ports depending on configuration
		configurePorts();
	}

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();

		// FIXME: Replace with your input and output port definitions
		

		
		// Hard coded input port, expecting a single String
		addInput(IN_FIRST_INPUT, 0, true, null, String.class);

		// Optional ports depending on configuration
		if (!configBean.getQueryVariables().isEmpty()) {
			
			for (QueryVariable variable : configBean.getQueryVariables()) {

				System.out.println("    to expose as output port " + variable.getVariableName());

				addOutput(variable.getVariableName(), 1);

			}
			
		}
		// Single value output port (depth 0)
		addOutput(OUT_RAW_OUTPUT, 1);
		// Output port with list of values (depth 1)


	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {
			
			public void run() {
				InvocationContext context = callback
						.getContext();
				ReferenceService referenceService = context
						.getReferenceService();
				// Resolve inputs 				
					//String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT), 
					//	String.class, context);
				
				// Support our configuration-dependendent input
				/*
				boolean optionalPorts = configBean.getExampleString().equals("specialCase"); 
				
				List<byte[]> special = null;
				// We'll also allow IN_EXTRA_DATA to be optionally not provided
				if (optionalPorts && inputs.containsKey(IN_EXTRA_DATA)) {
					// Resolve as a list of byte[]
					special = (List<byte[]>) referenceService.renderIdentifier(
							inputs.get(IN_EXTRA_DATA), byte[].class, context);
				}
				*/
				Query query = QueryFactory.create(configBean.getSparqlQuery());
				System.out.println(" The result variables are : " + query.getResultVars());
				QueryExecution qexec = QueryExecutionFactory.sparqlService(configBean.getSparqlServiceLocation(), query);
				
				/*
				try {
					
					System.out.println(" describe : - " + qexec.execDescribe());

				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				*/
				// TODO: Do the actual service invocation
				
				System.out.println(" starting invocation " );
				ResultSet result = null;
				boolean askResult = false;
				
				try {
					//results = this.service.invoke(firstInput, special)
					
					try {
						
						if (query.isSelectType()) {
							
							result = qexec.execSelect();
						
						}
						
						if (query.isAskType()) {
							
							askResult = qexec.execAsk();
							
							System.out.println(" ask result out : " + askResult );
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} catch (Exception ex) {
					callback.fail("Could not invoke Example service " + configBean.getExampleUri(),
							ex);
					// Make sure we don't call callback.receiveResult later 
					return;
				}

				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
		//	********************************* SELECT *********************************
											
				//System.out.println(" result is " + result + " result vars " + result.getResultVars());
				List<String> myResultList = new ArrayList<String>();
				
				HashMap<String, List<Object>> variableResults = new HashMap<String, List<Object>>();
				
				// create an empty list for each variable to populate
				for (QueryVariable var : configBean.getQueryVariables()) { 
					
					variableResults.put(var.getVariableName(), new ArrayList<Object>());
					
				}
				
				//System.out.println(" result to string is : " + result.toString() + " " + result.nextSolution());
				
				if (query.isSelectType()) {

					if (result !=null) {
						while (result.hasNext()) {

							System.out.println(" Getting QuerySolution");
							QuerySolution sol = result.nextSolution();

							//System.out.println(" get sols (toString) " + sol.toString());
							//System.out.println(" get  " + sol.get("name"));
							//System.out.println(" get literal     " + sol.getLiteral("name"));
							//System.out.println(" get resource    " + sol.getResource("name"));	

							myResultList.add(sol.toString());

							for (QueryVariable var : configBean.getQueryVariables()) { 

								try { 

									//System.out.println(" debug class " + sol.getClass());
								
									//System.out.println(" debug resource " + sol.getResource(var.getVariableName()));
									//System.out.println(" debug " + sol.getLiteral(var.getVariableName()).getString());
									
									if (sol.get(var.getVariableName()).isLiteral()) {
										
										variableResults.get(var.getVariableName()).add(sol.getLiteral(var.getVariableName()).getString());
										System.out.println(" is Literal : " + sol.getLiteral(var.getVariableName()).getString());
									}
									
									if (sol.get(var.getVariableName()).isResource()) {
										
										variableResults.get(var.getVariableName()).add(sol.getResource(var.getVariableName()).getURI());
										System.out.println(" is Resource : " + sol.getResource(var.getVariableName()).getURI());									
									}
																		
									//variableResults.get(var.getVariableName()).add(sol.getResource(var.getVariableName()));
								
									
								} catch (NullPointerException e) {

									variableResults.get(var.getVariableName()).add("***NOT FOUND***");

								}

							}	//end of for-loop
				
						}	//end of resultSet loop
					}


					System.out.println("list output " + myResultList.toString());
					// Register outputs

					T2Reference simpleRef = referenceService.register(myResultList, 1, true, context);

					outputs.put(OUT_RAW_OUTPUT, simpleRef);

					// Register variable outputs
					for (QueryVariable var : configBean.getQueryVariables()) {

						T2Reference ref = referenceService.register(variableResults.get(var.getVariableName()), 1, true, context);
						outputs.put(var.getVariableName(), ref);

					}
					
					
					
				}
		//	********************************* SELECT (end) *********************************

				
		//	********************************* ASK *********************************
				
			if (query.isAskType()) {	
				
				// silly...
				List temp = new ArrayList();
				temp.add(Boolean.toString(askResult));
				
				T2Reference ref = referenceService.register(temp, 1, true, context);

				outputs.put(OUT_RAW_OUTPUT, ref);
				
			}
			
		//	********************************* ASK (end) *********************************
		
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
				
				
			}
		});
	}

	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

}
