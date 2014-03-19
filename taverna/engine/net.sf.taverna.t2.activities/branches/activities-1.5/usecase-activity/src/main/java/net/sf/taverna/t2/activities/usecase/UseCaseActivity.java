package net.sf.taverna.t2.activities.usecase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;

import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.activities.externaltool.ExternalToolActivityConfigurationBean;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.SupersededActivity;

public class UseCaseActivity implements SupersededActivity<UseCaseActivityConfigurationBean>{

	private UseCaseActivityConfigurationBean configuration;

	@Override
	public Activity<?> getReplacementActivity() throws ActivityConfigurationException {
		ExternalToolActivityConfigurationBean correctConfiguration = new ExternalToolActivityConfigurationBean();
		ExternalToolActivity correctActivity = new ExternalToolActivity();
		
		correctConfiguration.setRepositoryUrl(configuration.getRepositoryUrl());
		correctConfiguration.setExternaltoolid(configuration.getUsecaseid());
		
		List<UseCaseDescription> usecases = new ArrayList<UseCaseDescription>();
		try {
			usecases = UseCaseEnumeration.readDescriptionsFromUrl(configuration.getRepositoryUrl());
		}
		catch (IOException ex) {
			throw new ActivityConfigurationException("Unable to find use case description");
		}
		// retrieve the UseCaseDescription for the given configuration bean;
		
		// and store it into mydesc
		for (UseCaseDescription usecase : usecases) {
			if (!usecase.getUsecaseid().equalsIgnoreCase(configuration.getUsecaseid()))
                continue;
			correctConfiguration.setUseCaseDescription(usecase);
			break;
		}
		if (correctConfiguration.getUseCaseDescription() == null) {
			throw new ActivityConfigurationException("Unable to find use case description");
		}

		correctConfiguration.setMechanism(InvocationGroupManager.getInstance().getDefaultMechanism());
		correctActivity.configure(correctConfiguration);
		return correctActivity;
	}

	@Override
	public void configure(UseCaseActivityConfigurationBean conf)
			throws ActivityConfigurationException {
		this.configuration = conf;
	}

	@Override
	public Map<String, String> getInputPortMapping() {
		return null;
	}

	@Override
	public Set<ActivityInputPort> getInputPorts() {
		return null;
	}

	@Override
	public Map<String, String> getOutputPortMapping() {
		return null;
	}

	@Override
	public Set<OutputPort> getOutputPorts() {
		return null;
	}

	@Override
	public Edit<? extends Activity<?>> getAddAnnotationEdit(
			AnnotationChain newAnnotation) {
		return null;
	}

	@Override
	public Set<? extends AnnotationChain> getAnnotations() {
		return null;
	}

	@Override
	public Edit<? extends Activity<?>> getRemoveAnnotationEdit(
			AnnotationChain annotationToRemove) {
		return null;
	}

	@Override
	public void setAnnotations(Set<AnnotationChain> annotations) {
	}

	@Override
	public UseCaseActivityConfigurationBean getConfiguration() {
		return null;
	}

}
