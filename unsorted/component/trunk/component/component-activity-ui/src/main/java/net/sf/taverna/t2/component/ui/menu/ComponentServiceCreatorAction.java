/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.DataflowXMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ProcessorXMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.ProcessorXMLSerializer;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author alanrw
 *
 */
public class ComponentServiceCreatorAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(ComponentServiceCreatorAction.class);

	
	private final Processor p;
	
	private static FileManager fm = FileManager.getInstance();
	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();
	
	public ComponentServiceCreatorAction(final Processor p) {
		super("Create component...", ComponentServiceIcon.getIcon());
		this.p = p;		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		final Activity<?> a = p.getActivityList().get(0);
		
		final Dataflow current = fm.getCurrentDataflow();
		
		Dataflow d = null;

		
		final ComponentActivity ca = new ComponentActivity();
		final ComponentActivityConfigurationBean cacb;
		
		
		Element processorElement;
		try {

			if (a instanceof NestedDataflow) {
				d = ((NestedDataflow) a).getNestedDataflow();
			} else {
			d = edits.createDataflow();
			
			// TODO: Keep the description
			
//			fm.setCurrentDataflow(current);
			
				processorElement = copyProcessor(p);
		
				Processor newProcessor = null;
			try {
				newProcessor = pasteProcessor (processorElement, d);
			} catch (IllegalArgumentException e1) {
				logger.error(e1);
			}
		
		final List<Edit<?>> componentWorkflowEditList = new ArrayList<Edit<?>>();
		
		
		for (final ProcessorInputPort pip : newProcessor.getInputPorts()) {
			final DataflowInputPort dip = edits.createDataflowInputPort(pip.getName(), pip.getDepth(), pip.getDepth(), d);
			componentWorkflowEditList.add(edits.getAddDataflowInputPortEdit(d, dip));
			
			final Datalink dl = edits.createDatalink(dip.getInternalOutputPort(), pip);
			componentWorkflowEditList.add(edits.getConnectDatalinkEdit(dl));
		}
		
		for (final ProcessorOutputPort pop : newProcessor.getOutputPorts()) {
			final DataflowOutputPort dop = edits.createDataflowOutputPort(pop.getName(), d);
			componentWorkflowEditList.add(edits.getAddDataflowOutputPortEdit(d, dop));
			
			final Datalink dl = edits.createDatalink(pop, dop.getInternalInputPort());
			componentWorkflowEditList.add(edits.getConnectDatalinkEdit(dl));
		}
			em.doDataflowEdit(d, new CompoundEdit(componentWorkflowEditList));
			}
		
			
		cacb = saveWorkflowAsComponent(d, p.getLocalName());
		
		
		ca.configure(cacb);

		final List<Edit<?>> currentWorkflowEditList = new ArrayList<Edit<?>>();

		for (final ProcessorInputPort pip : p.getInputPorts()) {
			currentWorkflowEditList.add(edits.getAddActivityInputPortMappingEdit(ca, pip.getName(), pip.getName()));
		}
		
		for (final ProcessorOutputPort pop : p.getOutputPorts()) {
			currentWorkflowEditList.add(edits.getAddActivityOutputPortMappingEdit(ca, pop.getName(), pop.getName()));
		}

		currentWorkflowEditList.add(edits.getRemoveActivityEdit(p, a));
		currentWorkflowEditList.add(edits.getAddActivityEdit(p, ca));
		em.doDataflowEdit(current, new CompoundEdit(currentWorkflowEditList));

		} catch (Exception e1) {
			logger.error(e1);
		} 

	}

	public static ComponentActivityConfigurationBean saveWorkflowAsComponent(Dataflow d, String defaultName) throws SaveException,
			IOException, ConfigurationException, ComponentRegistryException {
		ComponentLocationChooserPanel panel = new ComponentLocationChooserPanel(true);
		panel.setComponentName(defaultName);
		int result = JOptionPane.showConfirmDialog(null, panel, "Component location", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			return null;
		}
				
		JOptionPane.showMessageDialog(null, "Here will be the assurance that the component meets the profile\nThis may just be checking that it is the same URI");
		
		ComponentActivityConfigurationBean componentBean = panel.saveComponent(d);
		
		ServiceDescriptionRegistry registry = ServiceDescriptionRegistryImpl.getInstance();
		ComponentServiceProvider provider = new ComponentServiceProvider();
		provider.configure(panel.getConfig());
		registry.removeServiceDescriptionProvider(provider);
		registry.addServiceDescriptionProvider(provider);
		return componentBean;
	}
	
	private static HashMap<String, Element> requiredSubworkflows = new HashMap<String, Element>();
	
	public static Element copyProcessor(final Processor p) throws IOException, JDOMException, SerializationException {
		 final Element result = ProcessorXMLSerializer.getInstance().processorToXML(p);
			requiredSubworkflows = new HashMap<String, Element>();
			rememberSubworkflows(p);
			return result;

	}

	private static void rememberSubworkflows(final Processor p) throws SerializationException {
		for (final Activity<?> a : p.getActivityList()) {
			if (a instanceof NestedDataflow) {
				final NestedDataflow da = (NestedDataflow) a;
				final Dataflow df = da.getNestedDataflow();
				if (!requiredSubworkflows.containsKey(df.getIdentifier())) {
					requiredSubworkflows.put(df.getIdentifier(), DataflowXMLSerializer.getInstance().serializeDataflow(df));
					for (final Processor sp : df.getProcessors()) {
						rememberSubworkflows(sp);
					}
				}
			}
		}
	}
	
	public static Processor pasteProcessor(final Element e, final Dataflow d) throws ActivityConfigurationException, Exception, ClassNotFoundException, InstantiationException, IllegalAccessException, DeserializationException {
			final Processor result = ProcessorXMLDeserializer.getInstance().deserializeProcessor(e,requiredSubworkflows);
			if (result == null) {
				return null;
			}
		final String newName = Tools.uniqueProcessorName(result.getLocalName(), d);
		final List<Edit<?>> editList = new ArrayList<Edit<?>>();

		if (!newName.equals(result.getLocalName())) {
			final Edit<?> renameEdit = edits.getRenameProcessorEdit(result, newName);
			editList.add(renameEdit);
		}
		
		Activity<?> activity = null;
		if (result.getActivityList().size() > 0) {
			activity = result.getActivityList().get(0);
		}

		final Edit<?> edit = edits.getAddProcessorEdit(d, result);
		editList.add(edit);
		em.doDataflowEdit(d, new CompoundEdit(editList));
		
		return result;
	}


}
