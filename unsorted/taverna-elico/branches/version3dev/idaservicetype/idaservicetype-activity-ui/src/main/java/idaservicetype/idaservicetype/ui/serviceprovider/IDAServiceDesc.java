package idaservicetype.idaservicetype.ui.serviceprovider;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import idaservicetype.idaservicetype.IDAActivity;
import idaservicetype.idaservicetype.IDAActivityConfigurationBean;

public class IDAServiceDesc extends ServiceDescription<IDAActivityConfigurationBean> {

	private Edits edits;
	/**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description 
	 */
	@Override
	public Class<IDAActivity> getActivityClass() {
		return IDAActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * Making this bean will typically require some of the fields set on this service
	 * description, like an endpoint URL or method name. 
	 * 
	 */
	@Override
	public IDAActivityConfigurationBean getActivityConfiguration() {
		//ExampleActivityConfigurationBean bean = new ExampleActivityConfigurationBean();
		//bean.setExampleString(exampleString);
		//bean.setExampleUri(exampleUri);
		edits = new EditsImpl();
		IDAActivityConfigurationBean bean = new IDAActivityConfigurationBean();
		bean.setDataflow(edits.createDataflow());
		
		if (this.isIDATemplate) {
			bean.setTemplate(true);
			bean.setSelectedTask(this.getPreselectedTask());
		} else {
			bean.setTemplate(false);
		}
		
		return bean;
	}
	
	@PropertyAnnotation(hidden = true)
	public boolean isTemplateService() {
		return true;
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
		if (this.isIDATemplate) {
			
			return Arrays.asList("IDA" , "Task templates");
			
		} else {
			
			return Arrays.asList("IDA");

		}
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

	
	// FIXME: Replace example fields and getters/setters with any required
	// and optional fields. (All fields are searchable in the Service palette,
	// for instance try a search for exampleString:3)
	private String exampleString;
	private URI exampleUri;
	
	private boolean isIDATemplate;
	
	private String preselectedTask;
	
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

	public void setIDATemplate(boolean isIDATemplate) {
		this.isIDATemplate = isIDATemplate;
	}

	public boolean isIDATemplate() {
		return isIDATemplate;
	}

	public void setPreselectedTask(String preselectedTask) {
		this.preselectedTask = preselectedTask;
	}

	public String getPreselectedTask() {
		return preselectedTask;
	}


}
