package net.sf.taverna.t2.platform.taverna;

import java.util.Map;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.ProcessIdentifier;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * The enactor service constructs and configures workflow instance facades from
 * a workflow definition in the form of a Dataflow. The instances are facades
 * because they share an underlying Dataflow - this means that it is a very bad
 * idea to modify the Dataflow after you've used this service to obtain an
 * instance facade for it!
 * <p>
 * Methods on the returned facade allow for binding of workflow listeners and
 * for the push of workflow input values, or, in the case of workflows with no
 * input ports, triggering of instance invocation. The workflow does not run
 * immediately, you need to use the facade methods to start it once it's been
 * created by this service.
 * <p>
 * If you don't need access to the full facade API this interface also provides
 * some simple invocation functionality that wraps the more complex facade
 * methods.
 * 
 * @author Tom Oinn
 * 
 */
public interface Enactor {

	/**
	 * Build a workflow instance for the specified dataflow, using a default
	 * invocation context containing only the reference service from the spring
	 * context.
	 * 
	 * @param workflow
	 *            the workflow definition to run
	 * @return a workflow instance ready to enact *
	 * @throws EnactorException
	 *             if the dataflow passed fails validation, this will wrap an
	 *             InvalidDataflowException
	 */
	WorkflowInstanceFacade createFacade(Dataflow workflow)
			throws EnactorException;

	/**
	 * Build a workflow instance for the specified dataflow, using the specified
	 * invocation context. Use this form when you need to add facilities such as
	 * monitoring or provenance capture explicitly to this invocation.
	 * 
	 * @param workflow
	 *            the workflow definition to run
	 * @param context
	 *            a configured invocation context object. The invocation context
	 *            is not used until the instance is triggered through input data
	 *            or an explicit fire operation so entities can safely be added
	 *            to the context prior to this and after instance construction
	 *            if needed.
	 * @return a workflow instance ready to enact
	 * @throws EnactorException
	 *             if the dataflow passed fails validation, this will wrap an
	 *             InvalidDataflowException
	 */
	WorkflowInstanceFacade createFacade(Dataflow workflow,
			InvocationContext context) throws EnactorException;

	/**
	 * Build a workflow instance for the specified dataflow, using the specified
	 * invocation context. Use this form when you need to specify a parent process
	 * identifier.
	 * 
	 * @param workflow
	 *            the workflow definition to run
	 * @param context
	 *            a configured invocation context object. The invocation context
	 *            is not used until the instance is triggered through input data
	 *            or an explicit fire operation so entities can safely be added
	 *            to the context prior to this and after instance construction
	 *            if needed.
	 * @param parentProcess
	 *            the parent process identifier
	 * @return a workflow instance ready to enact
	 * @throws EnactorException
	 *             if the dataflow passed fails validation, this will wrap an
	 *             InvalidDataflowException
	 */
	WorkflowInstanceFacade createFacade(Dataflow workflow,
			InvocationContext context, ProcessIdentifier parentProcess) throws EnactorException;

	/**
	 * Push a T2 reference into the specified port of a workflow instance
	 * facade. This is equivalent to calling the 'pushData' method on the facade
	 * with an empty index array, blank parent process and wrapping the
	 * t2reference in a workflow data token with the same invocation context as
	 * the facade had at construction time. In general this is sensible, if you
	 * need token streaming (i.e. specifying the same input more than once) and
	 * similar you should use the more complex methods on WorkflowInstanceFacade
	 * directly.
	 * 
	 * @param facade
	 *            a previously constructed workflow instance
	 * @param inputPortName
	 *            the name of a workflow input port in the workflow definition
	 *            used to construct the workflow instance, note that this is
	 *            case sensitive!
	 * @param data
	 *            a T2 reference to the input data for that port
	 * @throws EnactorException
	 *             if the port can't be found or if the t2 reference isn't of
	 *             the correct depth for the input port.
	 */
	void pushData(WorkflowInstanceFacade facade, String inputPortName,
			T2Reference data) throws EnactorException;

	/**
	 * Blocking method to get a result from a workflow instance. This method
	 * blocks until the named output port on the workflow instance has emited a
	 * complete data structure and then returns it in the form of a T2Reference
	 * that can be resolved by the reference service
	 * 
	 * @param facade
	 *            the workflow instance to pull the result from
	 * @param outputPortName
	 *            the output port to read data from
	 * @return an internal reference that can be resolved by the associated
	 *         reference service
	 * @throws EnactorException
	 *             if the named output port doesn't exist in the dataflow used
	 *             by the facade, if an unhandled error occurs within the
	 *             workflow or if the workflow is cancelled
	 */
	T2Reference waitForResult(WorkflowInstanceFacade facade,
			String outputPortName) throws EnactorException;

	/**
	 * Blocking method, waits for the workflow instance to complete and returns
	 * a map of all results. This method can also be used for workflows with no
	 * outputs as it blocks on workflow completion and not on the more lenient
	 * constraint of all data being produced
	 * 
	 * @param facade
	 *            the workflow instance to wait for
	 * @return a map of port name to T2Reference
	 * @throws EnactorException
	 *             if the workflow instance fails
	 * @since p0.1b1
	 */
	Map<String, T2Reference> waitForCompletion(WorkflowInstanceFacade facade)
			throws EnactorException;
}
