package net.sf.taverna.t2.platform.taverna;

import org.springframework.context.ApplicationContext;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.spring.profile.AbstractContextProfile;
import net.sf.taverna.t2.platform.spring.profile.ContextProfileException;
import net.sf.taverna.t2.platform.util.reflect.ReflectionHelper;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2ReferenceGeneratorFactory;

/**
 * Provides a minimal platform environment required to access the basic
 * enactment facilities from the Taverna 2 codebase. It exposes the following
 * beans from the underlying Spring context :
 * 
 * <pre>
 * t2.enactor.workflowParser=net.sf.taverna.t2.platform.taverna.WorkflowParser
 * t2.enactor.workflowRenderer=net.sf.taverna.t2.platform.taverna.WorkflowXMLRenderer
 * t2.enactor.invocationContextFactory=net.sf.taverna.t2.platform.taverna.InvocationContextFactory
 * t2.enactor.monitorFactory=net.sf.taverna.t2.platform.taverna.MonitorFactory
 * t2.enactor.enactor=net.sf.taverna.t2.platform.taverna.Enactor
 * t2.data.referenceService=net.sf.taverna.t2.reference.ReferenceService
 * t2.data.referenceGeneratorFactory=net.sf.taverna.t2.reference.T2ReferenceGeneratorFactory
 * t2.workflow.editkit=net.sf.taverna.t2.platform.taverna.WorkflowEditKit
 * platform.pluginManager=net.sf.taverna.t2.platform.plugin.PluginManager
 * platform.reflectionHelper=net.sf.taverna.t2.platform.util.reflect.ReflectionHelper
 * </pre>
 * 
 * @author Tom Oinn
 * 
 */
public class TavernaBaseProfile extends AbstractContextProfile {

	/**
	 * Supply the profile with an application context, the profile then provides
	 * type-safe methods exposing beans from that context and in addition checks
	 * for the necessary beans on construction
	 * 
	 * @param context
	 *            an ApplicationContext to fetch beans from
	 * @throws ContextProfileException
	 *             if the context does not contain the correct beans for this
	 *             profile
	 */
	public TavernaBaseProfile(ApplicationContext context)
			throws ContextProfileException {
		super(context);
	}

	/**
	 * The edit kit provides method and edit objects to create and manipulate
	 * workflow models.
	 */
	public WorkflowEditKit getWorkflowEditKit() {
		return (WorkflowEditKit) getBean("t2.workflow.editkit");
	}

	/**
	 * The WorkflowParser is used to assemble Dataflow instances from references
	 * to serialized XML workflow files
	 */
	public final WorkflowParser getWorkflowParser() {
		return (WorkflowParser) getBean("t2.enactor.workflowParser");
	}

	/**
	 * The WorkflowXMLRenderer is used to build XML containing a serialization
	 * of a dataflow
	 */
	public final WorkflowXMLRenderer getWorkflowXMLRenderer() {
		return (WorkflowXMLRenderer) getBean("t2.enactor.workflowRenderer");
	}

	/**
	 * The InvocationContextFactory is used to obtain InvocationContext
	 * instances which are then used by the Enactor when constructing workflow
	 * instances
	 */
	public final InvocationContextFactory getInvocationContextFactory() {
		return (InvocationContextFactory) getBean("t2.enactor.invocationContextFactory");
	}

	/**
	 * The MonitorFactory is used to construct Monitor instances which, when
	 * added to an InvocationContext, provide monitoring facilities within a
	 * workflow enactment
	 */
	public final MonitorFactory getMonitorFactory() {
		return (MonitorFactory) getBean("t2.enactor.monitorFactory");
	}

	/**
	 * The Enactor is used to create WorkflowInstanceFacade instances from
	 * Dataflow definition objects. The facade provides methods to start an
	 * enactment instance.
	 */
	public final Enactor getEnactor() {
		return (Enactor) getBean("t2.enactor.enactor");
	}

	/**
	 * The ReferenceService is used to register objects, files, urls and similar
	 * with the reference management system, returning T2Identifier instances
	 * which can be used as inputs to workflow instances created by the Enactor.
	 * It is also used to de-reference these internal identifiers, rendering
	 * them to value graphs when requested.
	 */
	public final ReferenceService getReferenceService() {
		return (ReferenceService) getBean("t2.data.referenceService");
	}

	/**
	 * The PluginManager is used to install, enable and disable plug-in
	 * packages. These are used to specify types of activity, dispatch layer and
	 * external reference within the enactment system. It is important to ensure
	 * that the appropriate plug-ins are installed and active before attempting
	 * to load and enact workflows.
	 */
	public final PluginManager getPluginManager() {
		return (PluginManager) getBean("platform.pluginManager");
	}

	/**
	 * The ReflectionHelper provides access to classes managed by the plug-in
	 * manager. These classes aren't directly accessible from your code so you
	 * need to use java reflection to get at them - methods in the reflection
	 * helper simplify this process and reduce the pain involved.
	 */
	public final ReflectionHelper getReflectionHelper() {
		return (ReflectionHelper) getBean("platform.reflectionHelper");
	}

	/**
	 * The reference generator can be optionally injected into the invocation
	 * context to provide control over the namespace of references produced
	 * during workflow enactment. A default reference generator defined by the
	 * platform is used if this is not supplied in the invocation context. This
	 * factory produces reference generators for this purpose.
	 * 
	 * @since p0.1b1
	 */
	public final T2ReferenceGeneratorFactory getT2ReferenceGeneratorFactory() {
		return (T2ReferenceGeneratorFactory) getBean("t2.data.referenceGeneratorFactory");
	}

}
