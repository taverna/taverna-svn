/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.registry.ComponentUtil.calculateComponent;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.component.ui.util.Utils.refreshComponentServiceProvider;
import static net.sf.taverna.t2.workflowmodel.utils.Tools.uniqueProcessorName;
import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.ui.panel.RegisteryAndFamilyChooserComponentEntryPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceProviderConfig;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OverwriteException;
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

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author alanrw
 * 
 */
public class ComponentServiceCreatorAction extends AbstractAction {
	private static final long serialVersionUID = -2611514696254112190L;
	private static Logger logger = getLogger(ComponentServiceCreatorAction.class);

	private final Processor p;

	private static FileManager fm = FileManager.getInstance();
	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();

	public ComponentServiceCreatorAction(final Processor p) {
		super("Create component...", getIcon());
		this.p = p;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Version.ID ident = getNewComponentIdentification(p.getLocalName());

		if (ident == null)
			return;

		Activity<?> a = p.getActivityList().get(0);
		Dataflow current = fm.getCurrentDataflow();
		Dataflow d = null;
		ComponentActivity ca = new ComponentActivity();
		ComponentActivityConfigurationBean cacb;
		Element processorElement;
		try {
			if (a instanceof NestedDataflow) {
				d = ((NestedDataflow) a).getNestedDataflow();
			} else {
				d = edits.createDataflow();

				// TODO: Keep the description

				// fm.setCurrentDataflow(current);

				processorElement = copyProcessor(p);

				Processor newProcessor = null;
				try {
					newProcessor = pasteProcessor(processorElement, d);
				} catch (IllegalArgumentException e) {
					logger.error(
							"failed to paste processor representing component",
							e);
				}

				List<Edit<?>> componentWorkflowEditList = new ArrayList<Edit<?>>();

				for (ProcessorInputPort pip : newProcessor.getInputPorts()) {
					DataflowInputPort dip = edits.createDataflowInputPort(
							pip.getName(), pip.getDepth(), pip.getDepth(), d);
					componentWorkflowEditList.add(edits
							.getAddDataflowInputPortEdit(d, dip));

					Datalink dl = edits.createDatalink(
							dip.getInternalOutputPort(), pip);
					componentWorkflowEditList.add(edits
							.getConnectDatalinkEdit(dl));
				}

				for (ProcessorOutputPort pop : newProcessor.getOutputPorts()) {
					DataflowOutputPort dop = edits.createDataflowOutputPort(
							pop.getName(), d);
					componentWorkflowEditList.add(edits
							.getAddDataflowOutputPortEdit(d, dop));

					Datalink dl = edits.createDatalink(pop,
							dop.getInternalInputPort());
					componentWorkflowEditList.add(edits
							.getConnectDatalinkEdit(dl));
				}
				em.doDataflowEdit(d,
						new CompoundEdit(componentWorkflowEditList));
			}

			cacb = saveWorkflowAsComponent(d, ident);

			ca.configure(cacb);

			List<Edit<?>> currentWorkflowEditList = new ArrayList<Edit<?>>();

			for (ProcessorInputPort pip : p.getInputPorts()) {
				currentWorkflowEditList.add(edits
						.getAddActivityInputPortMappingEdit(ca, pip.getName(),
								pip.getName()));
			}

			for (ProcessorOutputPort pop : p.getOutputPorts()) {
				currentWorkflowEditList.add(edits
						.getAddActivityOutputPortMappingEdit(ca, pop.getName(),
								pop.getName()));
			}

			currentWorkflowEditList.add(edits.getRemoveActivityEdit(p, a));
			currentWorkflowEditList.add(edits.getAddActivityEdit(p, ca));
			em.doDataflowEdit(current,
					new CompoundEdit(currentWorkflowEditList));
		} catch (Exception e) {
			logger.error("failed to instantiate component", e);
		}
	}

	public static ComponentActivityConfigurationBean saveWorkflowAsComponent(
			Dataflow d, Version.ID ident) throws SaveException, IOException,
			ConfigurationException, RegistryException {
		if (ident == null) {
			return null;
		}

		createInitialComponent(d, ident);

		refreshComponentServiceProvider(new ComponentServiceProviderConfig(
				ident));
		return new ComponentActivityConfigurationBean(ident);
	}

	static Version.ID getNewComponentIdentification(String defaultName) {
		RegisteryAndFamilyChooserComponentEntryPanel panel = new RegisteryAndFamilyChooserComponentEntryPanel();
		panel.setComponentName(defaultName);
		int result = showConfirmDialog(null, panel, "Component location",
				OK_CANCEL_OPTION);
		if (result != OK_OPTION)
			return null;

		Version.ID ident = panel.getComponentVersionIdentification();
		if (ident == null) {
			showMessageDialog(null,
					"Not enough information to create component",
					"Component creation problem", ERROR_MESSAGE);
			return null;
		}

		try {
			Component existingComponent = calculateComponent(ident);
			if (existingComponent != null) {
				showMessageDialog(null,
						"Component with this name already exists",
						"Component creation problem", ERROR_MESSAGE);
				return null;
			}
		} catch (RegistryException e) {
			logger.error("failed to search registry", e);
			showMessageDialog(null,
					"Problem searching registry: " + e.getMessage(),
					"Component creation problem", ERROR_MESSAGE);
			return null;
		}
		return ident;
	}

	private static HashMap<String, Element> requiredSubworkflows = new HashMap<String, Element>();

	public static Element copyProcessor(final Processor p) throws IOException,
			JDOMException, SerializationException {
		final Element result = ProcessorXMLSerializer.getInstance()
				.processorToXML(p);
		requiredSubworkflows = new HashMap<String, Element>();
		rememberSubworkflows(p);
		return result;
	}

	private static void rememberSubworkflows(final Processor p)
			throws SerializationException {
		for (final Activity<?> a : p.getActivityList())
			if (a instanceof NestedDataflow) {
				NestedDataflow da = (NestedDataflow) a;
				Dataflow df = da.getNestedDataflow();
				if (!requiredSubworkflows.containsKey(df.getIdentifier())) {
					requiredSubworkflows.put(df.getIdentifier(),
							DataflowXMLSerializer.getInstance()
									.serializeDataflow(df));
					for (Processor sp : df.getProcessors())
						rememberSubworkflows(sp);
				}
			}
	}

	public static Processor pasteProcessor(final Element e, final Dataflow d)
			throws ActivityConfigurationException, Exception,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, DeserializationException {
		final Processor result = ProcessorXMLDeserializer.getInstance()
				.deserializeProcessor(e, requiredSubworkflows);
		if (result == null) {
			return null;
		}
		String newName = uniqueProcessorName(result.getLocalName(), d);
		List<Edit<?>> editList = new ArrayList<Edit<?>>();

		if (!newName.equals(result.getLocalName())) {
			Edit<?> renameEdit = edits.getRenameProcessorEdit(result, newName);
			editList.add(renameEdit);
		}

		Edit<?> edit = edits.getAddProcessorEdit(d, result);
		editList.add(edit);
		em.doDataflowEdit(d, new CompoundEdit(editList));

		return result;
	}

	public static Version.ID createInitialComponent(Dataflow d, Version.ID ident)
			throws RegistryException {
		try {
			fm.saveDataflow(d, ComponentFileType.instance, ident, false);

			Edit<?> dummyEdit = edits.getUpdateDataflowNameEdit(d,
					d.getLocalName());
			em.doDataflowEdit(d, dummyEdit);
		} catch (OverwriteException e) {
			throw new RegistryException(e);
		} catch (SaveException e) {
			throw new RegistryException(e);
		} catch (IllegalStateException e) {
			throw new RegistryException(e);
		} catch (EditException e) {
			throw new RegistryException(e);
		}
		return ident;
	}

}
