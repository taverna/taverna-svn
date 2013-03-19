/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

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
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

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
		
		JPanel overallPanel = new JPanel(new BorderLayout());
		ComponentChooserPanel panel = new ComponentChooserPanel();
		overallPanel.add(panel, BorderLayout.CENTER);
		JCheckBox replaceAllCheckBox = new JCheckBox("Replace all matching services");
		overallPanel.add(replaceAllCheckBox, BorderLayout.SOUTH);
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Component choice", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			
			Component chosenComponent = panel.getChosenComponent();
			ComponentVersion chosenVersion = chosenComponent.getComponentVersionMap().get(chosenComponent.getComponentVersionMap().lastKey());
			ComponentVersionIdentification ident = new ComponentVersionIdentification(panel.getChosenRegistry().getRegistryBase(), panel.getChosenFamily().getName(), chosenComponent.getName(), chosenVersion.getVersionNumber());
			
			ComponentActivityConfigurationBean cacb = new ComponentActivityConfigurationBean(ident);

			try {
				if (replaceAllCheckBox.isSelected()) {
					Activity<?> baseActivity = selection.getActivityList().get(0);
					Class<?> activityClass = baseActivity.getClass();
					String configString = getConfigString(baseActivity);
					
					replaceAllMatchingActivities(activityClass, cacb, configString, fileManager.getCurrentDataflow());

				} else {
					replaceActivity(cacb, selection, fileManager.getCurrentDataflow());
				}
			} catch (ActivityConfigurationException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Component Problem", JOptionPane.ERROR_MESSAGE);;
			}
		}
	}

	private String getConfigString(Activity<?> baseActivity) {
		XStream xstream = new XStream(new DomDriver());
		Object baseConfig = baseActivity.getConfiguration();
		xstream.setClassLoader(baseConfig.getClass().getClassLoader());
		return xstream.toXML(baseConfig);
	}

	private void replaceAllMatchingActivities(
			Class<?> activityClass, ComponentActivityConfigurationBean cacb, String configString,
			Dataflow d) throws ActivityConfigurationException {
		for (Processor p : d.getProcessors()) {
			Activity<?> a = p.getActivityList().get(0);
			if (a.getClass().equals(activityClass) && getConfigString(a).equals(configString)) {
				replaceActivity(cacb, p, d);
			} else if (a instanceof NestedDataflow) {
				replaceAllMatchingActivities(activityClass, cacb, configString, ((NestedDataflow) a).getNestedDataflow());
			}
		}
	}

	private void replaceActivity(ComponentActivityConfigurationBean cacb, Processor p, Dataflow d)
		throws ActivityConfigurationException {
		final Activity<?> a = p.getActivityList().get(0);

		ComponentActivity ca = new ComponentActivity();
		try {
			ca.configure(cacb);
		} catch (ActivityConfigurationException e1) {
			throw new ActivityConfigurationException("Unable to configure component");
		}
		if (a.getInputPorts().size() != ca.getInputPorts().size()) {
			throw new ActivityConfigurationException("Component does not have matching ports");			
		}
		if (a.getOutputPorts().size() != ca.getOutputPorts().size()) {
			throw new ActivityConfigurationException("Component does not have matching ports");		
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
				throw new ActivityConfigurationException("Original input port " + aipName + " is not matched");						
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
				throw new ActivityConfigurationException("Original output port " + aopName + " is not matched");						
			}
		}
		
		final List<Edit<?>> currentWorkflowEditList = new ArrayList<Edit<?>>();

		for (final ProcessorInputPort pip : p.getInputPorts()) {
			currentWorkflowEditList.add(edits.getAddActivityInputPortMappingEdit(ca, pip.getName(), pip.getName()));
		}
		
		for (final ProcessorOutputPort pop : p.getOutputPorts()) {
			currentWorkflowEditList.add(edits.getAddActivityOutputPortMappingEdit(ca, pop.getName(), pop.getName()));
		}

		currentWorkflowEditList.add(edits.getAddActivityEdit(p, ca));
		currentWorkflowEditList.add(edits.getRemoveActivityEdit(p, a));
		try {
			em.doDataflowEdit(d, new CompoundEdit(currentWorkflowEditList));
		} catch (EditException e1) {
			throw new ActivityConfigurationException("Unable to replace with component");
		}
	}

	public void setSelection(Processor selection) {
		this.selection = selection;
	}

}
