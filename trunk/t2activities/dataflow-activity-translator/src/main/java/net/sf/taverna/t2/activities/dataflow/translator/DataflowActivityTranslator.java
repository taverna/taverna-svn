package net.sf.taverna.t2.activities.dataflow.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.DataflowActivityConfigurationBean;
import net.sf.taverna.t2.cyclone.WorkflowModelTranslator;
import net.sf.taverna.t2.cyclone.WorkflowTranslationException;
import net.sf.taverna.t2.cyclone.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * An ActivityTranslator specifically for translating Taverna 1 Workflow
 * Processors to a Taverna 2 Dataflow Activity
 * 
 * @see ActivityTranslator
 * @author David Withers
 */
public class DataflowActivityTranslator extends
		AbstractActivityTranslator<DataflowActivityConfigurationBean> {

	@Override
	protected DataflowActivity createUnconfiguredActivity() {
		return new DataflowActivity();
	}

	@Override
	protected DataflowActivityConfigurationBean createConfigType(
			Processor processor) throws ActivityTranslationException {
		DataflowActivityConfigurationBean bean = new DataflowActivityConfigurationBean();
		try {
			Dataflow dataflow = WorkflowModelTranslator
					.doTranslation(getScuflModel(processor));
			DataflowValidationReport report = dataflow.checkValidity();
			if (report.isValid()) {
				bean.setDataflow(dataflow);
			} else {
				throw new ActivityTranslationException(
						"Error validating nested workflow");
			}
		} catch (WorkflowTranslationException e) {
			throw new ActivityTranslationException(
					"Error translating nested workflow", e);
		}
		return bean;
	}

	private ScuflModel getScuflModel(Processor processor)
			throws ActivityTranslationException {
		try {
			Method method = processor.getClass().getMethod("getInternalModel");
			return (ScuflModel) method.invoke(processor);
		} catch (SecurityException e) {
			throw new ActivityTranslationException(
					"The was a Security exception whilst trying to invoke getInternalModel through introspection",
					e);
		} catch (NoSuchMethodException e) {
			throw new ActivityTranslationException(
					"The processor does not have the method getInternalModel, and therefore does not conform to being a Workflow processor",
					e);
		} catch (IllegalArgumentException e) {
			throw new ActivityTranslationException(
					"The method getInternalModel on the Workflow processor had unexpected arguments",
					e);
		} catch (IllegalAccessException e) {
			throw new ActivityTranslationException(
					"Unable to access the method getInternalModel on the Workflow processor",
					e);
		} catch (InvocationTargetException e) {
			throw new ActivityTranslationException(
					"An error occurred invoking the method getInternalModel on the Workflow processor",
					e);
		}
	}

	public boolean canHandle(Processor processor) {
		return processor != null
				&& processor
						.getClass()
						.getName()
						.equals(
								"org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor");
	}

}
