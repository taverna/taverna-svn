/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.component.ComponentActivityConfigurationBean.ignorableNames;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.workflowmodel.utils.Tools.getActivityInputPort;
import static net.sf.taverna.t2.workflowmodel.utils.Tools.getActivityOutputPort;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentChooserPanel;
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
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author alanrw
 * 
 */
public class ReplaceByComponentAction extends AbstractAction {
	private static final long serialVersionUID = 7364648399658711574L;
	private static final FileManager fileManager = FileManager.getInstance();
	private static EditManager em = EditManager.getInstance();
	private static Edits edits = em.getEdits();

	public ReplaceByComponentAction() {
		super("Replace by component...", getIcon());
	}

	private Processor selection;

	@Override
	public void actionPerformed(ActionEvent e) {
		JPanel overallPanel = new JPanel(new BorderLayout());
		ComponentChooserPanel panel = new ComponentChooserPanel();
		overallPanel.add(panel, CENTER);
		JPanel checkBoxPanel = new JPanel(new FlowLayout());
		JCheckBox replaceAllCheckBox = new JCheckBox(
				"Replace all matching services");
		checkBoxPanel.add(replaceAllCheckBox);
		checkBoxPanel.add(new JSeparator());
		final JCheckBox renameServicesCheckBox = new JCheckBox("Rename service(s)");
		checkBoxPanel.add(renameServicesCheckBox);
		renameServicesCheckBox.setSelected(true);
		overallPanel.add(checkBoxPanel, SOUTH);
		int answer = showConfirmDialog(null, overallPanel, "Component choice",
				OK_CANCEL_OPTION);
		if (answer == OK_OPTION)
			doReplace(panel.getChosenRegistry(), panel.getChosenFamily(),
					replaceAllCheckBox.isSelected(), renameServicesCheckBox.isSelected(), panel.getChosenComponent());
	}

	private void doReplace(Registry chosenRegistry, Family chosenFamily,
			boolean replaceAll, boolean rename, Component chosenComponent) {
		Version chosenVersion = chosenComponent.getComponentVersionMap().get(
				chosenComponent.getComponentVersionMap().lastKey());
		Version.ID ident = new ComponentVersionIdentification(
				chosenRegistry.getRegistryBase(), chosenFamily.getName(),
				chosenComponent.getName(), chosenVersion.getVersionNumber());

		ComponentActivityConfigurationBean cacb = new ComponentActivityConfigurationBean(
				ident);

		try {
			if (replaceAll) {
				Activity<?> baseActivity = selection.getActivityList().get(0);
				Class<?> activityClass = baseActivity.getClass();
				String configString = getConfigString(baseActivity);

				replaceAllMatchingActivities(activityClass, cacb, configString, rename,
						fileManager.getCurrentDataflow());
			} else
				replaceActivity(cacb, selection, rename,
						fileManager.getCurrentDataflow());
		} catch (ActivityConfigurationException e) {
			showMessageDialog(
					null,
					"Failed to replace nested workflow with component: "
							+ e.getMessage(), "Component Problem",
					ERROR_MESSAGE);
		}
	}

	private String getConfigString(Activity<?> baseActivity) {
		XStream xstream = new XStream(new DomDriver());
		Object baseConfig = baseActivity.getConfiguration();
		xstream.setClassLoader(baseConfig.getClass().getClassLoader());
		return xstream.toXML(baseConfig);
	}

	private void replaceAllMatchingActivities(Class<?> activityClass,
			ComponentActivityConfigurationBean cacb, String configString,
			boolean rename, Dataflow d) throws ActivityConfigurationException {
		for (Processor p : d.getProcessors()) {
			Activity<?> a = p.getActivityList().get(0);
			if (a.getClass().equals(activityClass)
					&& getConfigString(a).equals(configString))
				replaceActivity(cacb, p, rename, d);
			else if (a instanceof NestedDataflow)
				replaceAllMatchingActivities(activityClass, cacb, configString, rename,
						((NestedDataflow) a).getNestedDataflow());

		}
	}

	private void replaceActivity(ComponentActivityConfigurationBean cacb,
			Processor p, boolean rename, Dataflow d) throws ActivityConfigurationException {
		final Activity<?> originalActivity = p.getActivityList().get(0);

		ComponentActivity replacementActivity = new ComponentActivity();
		try {
			replacementActivity.configure(cacb);
		} catch (ActivityConfigurationException e) {
			throw new ActivityConfigurationException(
					"Unable to configure component", e);
		}
		if (originalActivity.getInputPorts().size() != replacementActivity
				.getInputPorts().size())
			throw new ActivityConfigurationException(
					"Component does not have matching ports");

		int replacementOutputSize = replacementActivity.getOutputPorts().size();
		int originalOutputSize = originalActivity.getOutputPorts().size();
		for (String name : ignorableNames) {
			if (getActivityOutputPort(originalActivity, name) != null)
				originalOutputSize--;
			if (getActivityOutputPort(replacementActivity, name) != null)
				replacementOutputSize--;
		}

		int sizeDifference = replacementOutputSize - originalOutputSize;
		if (sizeDifference != 0)
			throw new ActivityConfigurationException(
					"Component does not have matching ports");

		for (ActivityInputPort aip : originalActivity.getInputPorts()) {
			String aipName = aip.getName();
			int aipDepth = aip.getDepth();
			ActivityInputPort caip = getActivityInputPort(replacementActivity,
					aipName);
			if ((caip == null) || (caip.getDepth() != aipDepth))
				throw new ActivityConfigurationException("Original input port "
						+ aipName + " is not matched");
		}
		for (OutputPort aop : originalActivity.getOutputPorts()) {
			String aopName = aop.getName();
			int aopDepth = aop.getDepth();
			OutputPort caop = getActivityOutputPort(replacementActivity,
					aopName);
			if (((caop == null) || (aopDepth != caop.getDepth()))
					&& !ignorableNames.contains(aopName))
				throw new ActivityConfigurationException(
						"Original output port " + aopName + " is not matched");
		}

		final List<Edit<?>> currentWorkflowEditList = new ArrayList<Edit<?>>();

		for (ProcessorInputPort pip : p.getInputPorts())
			currentWorkflowEditList.add(edits
					.getAddActivityInputPortMappingEdit(replacementActivity,
							pip.getName(), pip.getName()));

		for (ProcessorOutputPort pop : p.getOutputPorts())
			currentWorkflowEditList.add(edits
					.getAddActivityOutputPortMappingEdit(replacementActivity,
							pop.getName(), pop.getName()));

		currentWorkflowEditList.add(edits.getAddActivityEdit(p,
				replacementActivity));
		currentWorkflowEditList.add(edits.getRemoveActivityEdit(p,
				originalActivity));
		
		if (rename) {
			String possibleName =  replacementActivity.getConfiguration().getComponentName();
			String newName = Tools.uniqueProcessorName(Tools.sanitiseName(possibleName), d);
			currentWorkflowEditList.add(edits.getRenameProcessorEdit(p, newName));
		}
		try {
			em.doDataflowEdit(d, new CompoundEdit(currentWorkflowEditList));
		} catch (EditException e) {
			throw new ActivityConfigurationException(
					"Unable to replace with component", e);
		}
	}

	public void setSelection(Processor selection) {
		this.selection = selection;
	}

}
