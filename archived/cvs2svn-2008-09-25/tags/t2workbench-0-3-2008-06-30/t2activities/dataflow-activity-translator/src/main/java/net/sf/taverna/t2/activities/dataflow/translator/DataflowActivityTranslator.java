package net.sf.taverna.t2.activities.dataflow.translator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.compatibility.WorkflowModelTranslator;
import net.sf.taverna.t2.compatibility.WorkflowTranslationException;
import net.sf.taverna.t2.compatibility.activity.AbstractActivityTranslator;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslator;
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
		AbstractActivityTranslator<Dataflow> {

	@Override
	protected DataflowActivity createUnconfiguredActivity() {
		return new DataflowActivity();
	}

	@Override
	protected Dataflow createConfigType(
			Processor processor) throws ActivityTranslationException {
		Dataflow dataflow=null;
		try {
			dataflow = WorkflowModelTranslator
					.doTranslation(getScuflModel(processor));
			DataflowValidationReport report = dataflow.checkValidity();
			if (!report.isValid()) {
				throw new ActivityTranslationException(
						"Error validating nested workflow");
			}
		} catch (WorkflowTranslationException e) {
			throw new ActivityTranslationException(
					"Error translating nested workflow", e);
		}
		return dataflow;
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
