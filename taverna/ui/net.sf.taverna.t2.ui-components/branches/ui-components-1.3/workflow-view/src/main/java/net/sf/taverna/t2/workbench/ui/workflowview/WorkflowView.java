package net.sf.taverna.t2.workbench.ui.workflowview;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.actions.PasteGraphComponentAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.workflowview.ShowExceptionRunnable;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.AddDataflowInputPortEdit;
import net.sf.taverna.t2.workflowmodel.impl.AddDataflowOutputPortEdit;
import net.sf.taverna.t2.workflowmodel.impl.AddProcessorEdit;
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
 * 
 * Super class for all UIComponentSPIs that display a Workflow
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public abstract class WorkflowView extends JPanel implements UIComponentSPI{
	
	private static Edits edits = EditsRegistry.getEdits();

	private static EditManager editManager = EditManager.getInstance();
	
	public static Element copyRepresentation = null;
	
	private static Logger logger = Logger
	.getLogger(WorkflowView.class);
	
	private static DataFlavor processorFlavor = null;
	private static DataFlavor serviceDescriptionDataFlavor = null;
	private static HashMap<String, Element> requiredSubworkflows = new HashMap<String, Element>();
	
	private static ChangeObserver observer = null;
	
	private static String UNABLE_TO_ADD_SERVICE = "Unable to add service";
	private static String UNABLE_TO_COPY_SERVICE = "Unable to copy service";

	/**
	 * Create a WorkflowView and set it up to receive services.
	 */
	public WorkflowView() {
		super();
		if (observer == null) {
			observer = new ChangeObserver();
		}
		setFocusable(true);
		if (serviceDescriptionDataFlavor == null) {
			try {
				serviceDescriptionDataFlavor = new DataFlavor(
						DataFlavor.javaJVMLocalObjectMimeType + ";class="
						+ ServiceDescription.class.getCanonicalName(),
						"ServiceDescription", ServiceDescription.class.getClassLoader());
		if (processorFlavor == null) {
			processorFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class="
					+ Processor.class.getCanonicalName(),
					"Processor", Processor.class.getClassLoader());
		}
			} catch (ClassNotFoundException e) {
				logger.error(e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public final static Processor importServiceDescription(ServiceDescription sd, boolean rename) {
		Processor p = null;
		Object bean = sd.getActivityConfiguration();
		Activity activity;
		Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
		try {
			activity = (Activity) sd.getActivityClass().newInstance();
			String name = sd.getName()
			.replace(' ', '_');
	name = Tools.uniqueProcessorName(name, currentDataflow);
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		editList.add(edits.getConfigureActivityEdit(activity, bean));
		p=edits.createProcessor(name);
		editList.add(edits.getDefaultDispatchStackEdit(p));
		editList.add(edits.getAddActivityEdit(p, activity));
//		editList.add(edits.getMapProcessorPortsForActivityEdit(p));
//		editList.add(edits.getRenameProcessorEdit(p, name));
		editList.add(edits.getAddProcessorEdit(currentDataflow, p));
		Edit insertionEdit = sd.getInsertionEdit(currentDataflow, p, activity);
		if (insertionEdit != null) {
			editList.add(insertionEdit);
		}
			editManager
					.doDataflowEdit(currentDataflow, new CompoundEdit(editList));
	} catch (EditException e) {
		showException(UNABLE_TO_ADD_SERVICE, e);
		logger.warn("Could not add processor : edit error", e);
		p = null;
	} catch (InstantiationException e) {
		showException(UNABLE_TO_ADD_SERVICE,e);
		logger.warn("Could not add processor : edit error", e);
		p = null;
	} catch (IllegalAccessException e) {
		showException(UNABLE_TO_ADD_SERVICE,e);
		logger.error(e);
	}
	
	if ((p != null) && rename) {
		RenameProcessorAction rpa = new RenameProcessorAction(currentDataflow, p, null);
		rpa.actionPerformed(new ActionEvent(sd, 0, ""));
	}
	
	if ((p != null) && sd.isTemplateService()) {
		Action action = getConfigureAction(p);
		if (action != null) {
			action.actionPerformed(new ActionEvent(sd, 0, ""));
		}

	}
	return p;
}
	
	public static Action getConfigureAction(Processor p) {
		Action result = null;
		JPopupMenu dummyMenu = MenuManager.getInstance().createContextMenu(null,
				p, null);
		for (Component c : dummyMenu.getComponents()) {
			logger.debug(c.getClass().getCanonicalName());
			if (c instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) c;
				Action action = menuItem.getAction();
				if ((action != null) && (action instanceof ActivityConfigurationAction) && action.isEnabled()){
					if (result != null) {
						// do not return anything if there are two matches
//						logger.info("Multiple actions " + action.getClass().getCanonicalName() + " " + result.getClass().getCanonicalName());
						return null;
					}
					result = action;
				}
			}
			}
		return result;
	}

	public static void pasteTransferable(Transferable t) {
		if (t.isDataFlavorSupported(processorFlavor)) {
			pasteProcessor(t);
		} else if (t.isDataFlavorSupported(serviceDescriptionDataFlavor)) {
			try {
				ServiceDescription data = (ServiceDescription) t.getTransferData(serviceDescriptionDataFlavor);
				importServiceDescription(data, false);
			} catch (UnsupportedFlavorException e) {
				showException(UNABLE_TO_ADD_SERVICE,e);
				logger.error(e);
			} catch (IOException e) {
				showException(UNABLE_TO_ADD_SERVICE,e);
				logger.error(e);
			}
			
		}
	}
	
	public static void pasteTransferable() {
		pasteTransferable(Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null));

	}
	
	public static void pasteProcessor(Transferable t) {
		try {
			Element e = (Element) t.getTransferData(processorFlavor);
			Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
			Processor p = ProcessorXMLDeserializer.getInstance().deserializeProcessor(e,requiredSubworkflows);
			if (p == null) {
				return;
			}
		String newName = Tools.uniqueProcessorName(p.getLocalName(), currentDataflow);
		List<Edit<?>> editList = new ArrayList<Edit<?>>();

		if (!newName.equals(p.getLocalName())) {
			Edit renameEdit = edits.getRenameProcessorEdit(p, newName);
			editList.add(renameEdit);
		}
		
		Activity activity = null;
		if (p.getActivityList().size() > 0) {
			activity = p.getActivityList().get(0);
		}

		List<ProcessorInputPort> processorInputPorts = new ArrayList<ProcessorInputPort>();
		processorInputPorts.addAll(p.getInputPorts());
		for (ProcessorInputPort port : processorInputPorts) {
			Edit removePortEdit = edits.getRemoveProcessorInputPortEdit(p, port);
			editList.add(removePortEdit);
			if (activity != null) {
				Edit removePortMapEdit = edits.getRemoveActivityInputPortMappingEdit(activity, port.getName());
				editList.add(removePortMapEdit);
			}
		}
		List<ProcessorOutputPort> processorOutputPorts = new ArrayList<ProcessorOutputPort>();
		processorOutputPorts.addAll(p.getOutputPorts());
		for (ProcessorOutputPort port : processorOutputPorts) {
			Edit removePortEdit = edits.getRemoveProcessorOutputPortEdit(p, port);
			editList.add(removePortEdit);
			if (activity != null) {
				Edit removePortMapEdit = edits.getRemoveActivityOutputPortMappingEdit(activity, port.getName());
				editList.add(removePortMapEdit);
			}
		}
		Edit edit = EditsRegistry.getEdits().getAddProcessorEdit(currentDataflow, p);
		editList.add(edit);
		EditManager.getInstance().doDataflowEdit(currentDataflow, new CompoundEdit(editList));
		} catch (ActivityConfigurationException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (EditException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (ClassNotFoundException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (InstantiationException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (IllegalAccessException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (DeserializationException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (UnsupportedFlavorException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		} catch (IOException e) {
			showException(UNABLE_TO_ADD_SERVICE,e);
			logger.error(e);
		}
	}
	
	public static void copyProcessor() {
		Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
		DataflowSelectionModel dataFlowSelectionModel = DataflowSelectionManager
		.getInstance().getDataflowSelectionModel(currentDataflow);
		// Get all selected components
		Set<Object> selectedWFComponents = dataFlowSelectionModel
				.getSelection();
		Processor p = null;
		for (Object selectedWFComponent : selectedWFComponents) {
			if (selectedWFComponent instanceof Processor) {
				p = (Processor) selectedWFComponent;
				break;
			}
		}
		if (p != null) {
			copyProcessor(p);
		}		
	}
	
	public static void copyProcessor(Processor p) {
		try {
			final Element e = ProcessorXMLSerializer.getInstance().processorToXML(p);
			requiredSubworkflows = new HashMap<String, Element>();
			rememberSubworkflows(p);
			Transferable t = new Transferable() {

				public Object getTransferData(DataFlavor flavor)
						throws UnsupportedFlavorException, IOException {
					return e;
				}

				public DataFlavor[] getTransferDataFlavors() {
					DataFlavor[] result = new DataFlavor[1];
					result[0] = processorFlavor;
					return result;
				}

				public boolean isDataFlavorSupported(DataFlavor flavor) {
					return flavor.equals(processorFlavor);
				}
				
			};
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(t, null);
			PasteGraphComponentAction.getInstance().setEnabled(true);
		} catch (IOException e1) {
			showException(UNABLE_TO_COPY_SERVICE, e1);
			logger.error(e1);
		} catch (JDOMException e1) {
			logger.error(UNABLE_TO_COPY_SERVICE, e1);
		} catch (SerializationException e) {
			logger.error(UNABLE_TO_COPY_SERVICE, e);
		}
	}
	
	private static void rememberSubworkflows(Processor p) throws SerializationException {
		for (Activity a : p.getActivityList()) {
			if (a instanceof NestedDataflow) {
				NestedDataflow da = (NestedDataflow) a;
				Dataflow df = da.getNestedDataflow();
				if (!requiredSubworkflows.containsKey(df.getIdentifier())) {
					requiredSubworkflows.put(df.getIdentifier(), DataflowXMLSerializer.getInstance().serializeDataflow(df));
					for (Processor sp : df.getProcessors()) {
						rememberSubworkflows(sp);
					}
				}
			}
		}
	}

	public static void cutProcessor() {
		Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
		DataflowSelectionModel dataFlowSelectionModel = DataflowSelectionManager
		.getInstance().getDataflowSelectionModel(currentDataflow);
		// Get all selected components
		Set<Object> selectedWFComponents = dataFlowSelectionModel
				.getSelection();
		Processor p = null;
		for (Object selectedWFComponent : selectedWFComponents) {
			if (selectedWFComponent instanceof Processor) {
				p = (Processor) selectedWFComponent;
				break;
			}
		}
		if (p != null) {
			cutProcessor(currentDataflow, p, null);
		}		
	}
	public static void cutProcessor(Dataflow dataflow, Processor processor, Component component) {
		copyProcessor(processor);
		new RemoveProcessorAction(dataflow, processor, component).actionPerformed(null);

	}
	
	private class ChangeObserver implements Observer<EditManagerEvent> {

		
		public ChangeObserver() {
			super();
			EditManager.getInstance().addObserver(this);
		}

		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			Dataflow currentDataflow = (Dataflow) ModelMap.getInstance().getModel(ModelMapConstants.CURRENT_DATAFLOW);
			Edit<?> edit = message.getEdit();
			considerEdit(edit, message instanceof DataFlowUndoEvent);
		}
		
		private void considerEdit(Edit<?> edit, boolean undoing) {
			if (edit instanceof CompoundEdit) {
				CompoundEdit compound = (CompoundEdit) edit;
				for (Edit e : compound.getChildEdits()) {
				    considerEdit(e, undoing);
				}
			} else {
				Object subject = edit.getSubject();
				if (subject instanceof Dataflow) {
					DataflowSelectionModel selectionModel = DataflowSelectionManager
					.getInstance().getDataflowSelectionModel(
							(Dataflow) edit.getSubject());
					Object objectOfEdit = null;
					if (edit instanceof AddProcessorEdit) {
						objectOfEdit = ((AddProcessorEdit) edit).getProcessor();
					} else if (edit instanceof AddDataflowInputPortEdit) {
						objectOfEdit = ((AddDataflowInputPortEdit) edit).getDataflowInputPort();
					} else if (edit instanceof AddDataflowOutputPortEdit) {
						objectOfEdit = ((AddDataflowOutputPortEdit) edit).getDataflowOutputPort();
					}
					if (objectOfEdit != null) {
					    if (undoing && selectionModel.getSelection().contains(objectOfEdit)) {
						selectionModel.clearSelection();
					    } else {
						HashSet selection = new HashSet();
						selection.add(objectOfEdit);
						selectionModel.setSelection(selection);		
					    }
					}
				}
			}
		}
		
	}
	
	private static void showException(String message, Exception e) {
		if (!GraphicsEnvironment.isHeadless()) {
			SwingUtilities.invokeLater(new ShowExceptionRunnable(message, e));
		}
	}
}
