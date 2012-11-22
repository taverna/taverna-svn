/**
 *
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentFamilyChooserPanel;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.License;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.ServerResponse;
import net.sf.taverna.t2.workbench.file.exceptions.SaveException;
import net.sf.taverna.t2.workbench.file.impl.T2DataflowSaver;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * @author alanrw
 *
 */
public class ComponentLocationChooserPanel extends ComponentFamilyChooserPanel {

	private static final String T2FLOW = ".t2flow";

	private JTextField componentNameField = new JTextField(20);

	public ComponentLocationChooserPanel(boolean editableFamily) {
		super(editableFamily);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		this.add(new JLabel("Component name"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentNameField, gbc);
	}

	public static void main(String[] args) {
		System.out.println("starting");
		JFrame frame = new JFrame();
		frame.add(new ComponentLocationChooserPanel(true));
		frame.setVisible(true);
		System.out.println("done");
	}

	public String getComponentName() {
		return componentNameField.getText();
	}

	public void setComponentName(String name) {
		componentNameField.setText(name);
	}

	public String saveComponent(Dataflow d) {
		String componentName = StringUtils.remove(getComponentName(), T2FLOW);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final T2DataflowSaver saver = new T2DataflowSaver();
		String componentWorkflowString = null;
		try {
			saver.saveDataflow(d, new T2FlowFileType(), baos);
			componentWorkflowString = baos.toString();
			Object familyChoice = getFamilyChoice();
		if (sourceChoiceIsLocal()) {
			if (familyChoice instanceof String) {
				File homeDir = ApplicationRuntime.getInstance().getApplicationHomeDir();
				File components = new File(homeDir, "components");
				familyChoice = new File(components, (String) familyChoice);
				((File)familyChoice).mkdir();
			}
			File componentFile = new File((File)familyChoice, componentName + T2FLOW);
			FileUtils.writeStringToFile(componentFile, componentWorkflowString, "utf-8");
		} else {
			Element packElement;
			if (familyChoice instanceof String) {
				String packToSend = "<pack><title>" + (String) familyChoice + "</title>" +
				 "</pack>";
				ServerResponse packResponse = myExperimentClient.doMyExperimentPOST(getSourceChoice() + "/pack.xml", packToSend);
				packElement = packResponse.getResponseBody().getRootElement();

				String taggingToSend = "<tagging><subject resource=\"" + packElement.getAttributeValue("resource") + "\"/><label>component family</label></tagging>";
				ServerResponse taggingResponse = myExperimentClient.doMyExperimentPOST(getSourceChoice() + "/tagging.xml", taggingToSend);
			}
			else {
				packElement = (Element) getFamilyChoice();
			}
			ServerResponse response = myExperimentClient.postWorkflow(componentWorkflowString, componentName, "", License.DEFAULT_LICENSE, "private");
			Document body = response.getResponseBody();
			Element root = body.getRootElement();
			String workflowResource = root.getAttributeValue("resource");
			String packResource = packElement.getAttributeValue("resource");

			String toPost = "<internal-pack-item><pack resource=\"" + packResource + "\"/><item resource=\"" + workflowResource + "\"/></internal-pack-item>";

			myExperimentClient.doMyExperimentPOST(getSourceChoice() + "/internal-pack-item.xml", toPost);
		}
		} catch (Exception e) {
			return null;
		}
		return componentWorkflowString;
	}
}
