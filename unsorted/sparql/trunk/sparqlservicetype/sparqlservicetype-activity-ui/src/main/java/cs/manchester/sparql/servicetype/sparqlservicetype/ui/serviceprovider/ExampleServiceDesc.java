package cs.manchester.sparql.servicetype.sparqlservicetype.ui.serviceprovider;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import cs.manchester.sparql.servicetype.sparqlservicetype.ExampleActivity;
import cs.manchester.sparql.servicetype.sparqlservicetype.ExampleActivityConfigurationBean;
import cs.manchester.sparql.servicetype.sparqlservicetype.QueryVariable;

public class ExampleServiceDesc extends ServiceDescription<ExampleActivityConfigurationBean> {

	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<? extends Activity<ExampleActivityConfigurationBean>> getActivityClass() {
		return ExampleActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an endpoint URL or method name. 
	 * 
	 */
	@Override
	public ExampleActivityConfigurationBean getActivityConfiguration() {
		ExampleActivityConfigurationBean bean = new ExampleActivityConfigurationBean();
		bean.setExampleString(exampleString);
		bean.setExampleUri(exampleUri);
		bean.setSparqlQuery(sparqlQuery);
		bean.setSparqlServiceLocation(sparqlServiceLocation);
		bean.setQueryVariables(queryVariables);
		return bean;
	}

	/**
	 * An icon to represent this service description in the service palette.
	 */
	@Override
	public Icon getIcon() {
		return ExampleServiceIcon.getIcon();
	}

	/**
	 * The display name that will be shown in service palette and will
	 * be used as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return exampleString;
	}

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList("Sparql Query");
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		// FIXME: Use your fields instead of example fields
		return Arrays.<Object>asList(exampleString, exampleUri);
	}
	/**
	 * Make sure that the configuration window appears on canvas drop 
	 */
	@PropertyAnnotation(hidden = true)
	public boolean isTemplateService() {
		return true;
	}
		
	// FIXME: Replace example fields and getters/setters with any required
	// and optional fields. (All fields are searchable in the Service palette,
	// for instance try a search for exampleString:3)
	private String exampleString;
	
	private URI exampleUri;
	
	private String sparqlServiceLocation = new String();

	private String sparqlQuery = new String();
	
	private List<QueryVariable> queryVariables = new ArrayList<QueryVariable>();;
	
	public String getExampleString() {
		return exampleString;
	}
	public URI getExampleUri() {
		return exampleUri;
	}
	public void setExampleString(String exampleString) {
		this.exampleString = exampleString;
	}
	public void setExampleUri(URI exampleUri) {
		this.exampleUri = exampleUri;
	}
	
	public String getSparqlServiceLocation() {
		return sparqlServiceLocation;
	}

	public void setSparqlServiceLocation(String sparqlServiceLocation) {
		this.sparqlServiceLocation = sparqlServiceLocation;
	}
	
	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	public List<QueryVariable> getQueryVariables() {
		return queryVariables;
	}

	public void setQueryVariables(List<QueryVariable> queryVariables) {
		this.queryVariables = queryVariables;
	}
	
}
