/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.Component;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

/**
 * @author alanrw
 *
 */
public class ReplaceByComponentAction extends AbstractAction {

	private static final FileManager fileManager = FileManager.getInstance();
	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();

	public ReplaceByComponentAction() {
		super("Replace by component...", ComponentServiceIcon.getIcon());
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 7364648399658711574L;
	private Processor selection;

	@Override
	public void actionPerformed(ActionEvent e) {
		ComponentChooserPanel panel = new ComponentChooserPanel();
		int answer = JOptionPane.showConfirmDialog(null, panel, "Component choice", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			final Activity<?> a = selection.getActivityList().get(0);
			Dataflow current = fileManager.getCurrentDataflow();
			
			Component chosenComponent = panel.getChosenComponent();
			ComponentVersion chosenVersion = chosenComponent.getComponentVersionMap().get(chosenComponent.getComponentVersionMap().lastKey());
			ComponentVersionIdentification ident = new ComponentVersionIdentification(panel.getChosenRegistry().getRegistryBase(), panel.getChosenFamily().getName(), chosenComponent.getName(), chosenVersion.getVersionNumber());
			
			ComponentActivityConfigurationBean cacb = new ComponentActivityConfigurationBean(ident);
			ComponentActivity ca = new ComponentActivity();
			try {
				ca.configure(cacb);
			} catch (ActivityConfigurationException e1) {
				JOptionPane.showMessageDialog(null, "Unable to configure component", "Component Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (a.getInputPorts().size() != ca.getInputPorts().size()) {
				JOptionPane.showMessageDialog(null, "Component does not have matching ports", "Component Replacement Problem", JOptionPane.ERROR_MESSAGE);
				return;			
			}
			if (a.getOutputPorts().size() != ca.getOutputPorts().size()) {
				JOptionPane.showMessageDialog(null, "Component does not have matching ports", "Component Replacement Problem", JOptionPane.ERROR_MESSAGE);
				return;			
			}
			for (ActivityInputPort aip : a.getInputPorts()) {
				String aipName = aip.getName();
				int aipDepth = aip.getDepth();
				boolean found = false;
				for (ActivityInputPort caip : ca.getInputPorts()) {
					if (caip.getName().equals(aipName) && (caip.getDepth() == aipDepth)) {
						found = true;
						break;
					}
				}
				if (!found) {
					JOptionPane.showMessageDialog(null, "Original input port " + aipName + " is not matched", "Component Replacement Problem", JOptionPane.ERROR_MESSAGE);
					return;							
				}
			}
			for (OutputPort aop : a.getOutputPorts()) {
				String aopName = aop.getName();
				int aopDepth = aop.getDepth();
				boolean found = false;
				for (OutputPort caop : ca.getOutputPorts()) {
					if (caop.getName().equals(aopName) && (caop.getDepth() == aopDepth)) {
						found = true;
						break;
					}
				}
				if (!found) {
					JOptionPane.showMessageDialog(null, "Original output port " + aopName + " is not matched", "Component Replacement Problem", JOptionPane.ERROR_MESSAGE);
					return;							
				}
			}
			
			final List<Edit<?>> currentWorkflowEditList = new ArrayList<Edit<?>>();

			for (final ProcessorInputPort pip : selection.getInputPorts()) {
				currentWorkflowEditList.add(edits.getAddActivityInputPortMappingEdit(ca, pip.getName(), pip.getName()));
			}
			
			for (final ProcessorOutputPort pop : selection.getOutputPorts()) {
				currentWorkflowEditList.add(edits.getAddActivityOutputPortMappingEdit(ca, pop.getName(), pop.getName()));
			}

			currentWorkflowEditList.add(edits.getAddActivityEdit(selection, ca));
			currentWorkflowEditList.add(edits.getRemoveActivityEdit(selection, a));
			try {
				em.doDataflowEdit(current, new CompoundEdit(currentWorkflowEditList));
			} catch (EditException e1) {
				JOptionPane.showMessageDialog(null, "Unable to replace with component", "Component Replacement Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}

	public void setSelection(Processor selection) {
		this.selection = selection;
	}

}
