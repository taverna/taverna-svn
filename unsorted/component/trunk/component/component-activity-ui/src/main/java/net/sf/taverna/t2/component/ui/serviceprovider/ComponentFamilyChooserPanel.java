/**
 * 
 */
package net.sf.taverna.t2.component.ui.serviceprovider;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.MyExperimentClient;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author alanrw
 *
 */
public class ComponentFamilyChooserPanel extends JPanel {
	
	private static Logger logger = Logger.getLogger(ComponentFamilyChooserPanel.class);
	

	
	public static final String LOCAL = "Local machine";
	public static final String MYEXPERIMENT = "http://www.myexperiment.org";
	
	private static List<String> knownSources = Arrays.asList (new String[] {LOCAL, MYEXPERIMENT});
	
	private String currentSourceChoice;
	
	private DefaultComboBoxModel familyModel = new DefaultComboBoxModel();
	
	private static DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	
	protected MyExperimentClient myExperimentClient = new MyExperimentClient(logger);

	
	public ComponentFamilyChooserPanel(boolean editableFamily) {
		super();
		this.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		final JComboBox sourceChoice = new JComboBox(knownSources.toArray());
//		sourceChoice.setEditable(true);
		
		final JComboBox familyChoice = new JComboBox();
		familyChoice.setEditable(editableFamily);
		familyChoice.setEditor(new BasicComboBoxEditor());
		
		sourceChoice.setSelectedItem(MYEXPERIMENT);
		currentSourceChoice = MYEXPERIMENT;
		familyChoice.setModel(familyModel);
		familyChoice.setRenderer(new ListCellRenderer() {

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return defaultRenderer.getListCellRendererComponent(list, convertValueToString(value), index, isSelected, cellHasFocus);
			}});
		familyChoice.setEditor(new FamilyChoiceEditor());
		updateFamilyModel(currentSourceChoice);
		sourceChoice.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String v = (String) sourceChoice.getSelectedItem();
				if (!v.equals(currentSourceChoice)) {
					updateFamilyModel(v);
					currentSourceChoice = v;
				}
			}});
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		this.add(new JLabel("Component family site"), gbc);
		gbc.gridx++;
		this.add(sourceChoice, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		this.add(new JLabel("Component family"), gbc);
		gbc.gridx++;
		this.add(familyChoice, gbc);

	}
	
	public static String convertValueToString(Object value) {
		if (value instanceof Element) {
			return ((Element) value).getChildText("title");
		}
		if (value instanceof File) {
			return ((File) value).getName();					
		}
		if (value instanceof String) {
			return (String) value;
		}
		return null;
	}
	
	private void updateFamilyModel (String source) {
		
		if (!source.equals(LOCAL)) {
			myExperimentClient.setBaseURL(source);
		
			if (!myExperimentClient.isLoggedIn()) {
				myExperimentClient.doLogin();
			}

			Document families = null;
				try {
					families = myExperimentClient.doMyExperimentGET(source + "/packs.xml?tag=component%20family&elements=description,title").getResponseBody();
				} catch (Exception e) {
					logger.error(e);
					familyModel.removeAllElements();
				}
				Element root = families.getRootElement();
				for (Object packObject : root.getChildren()) {
					Element packElement = (Element) packObject;
					familyModel.addElement(packElement);
				}
		} else {
			File homeDir = ApplicationRuntime.getInstance().getApplicationHomeDir();
			File components = new File(homeDir, "components");
			familyModel.removeAllElements();
			if (!components.exists()) {
				components.mkdir();
			}
			if (!components.isDirectory()) {
				return;
			}
			for (File family : components.listFiles()) {
				if (family.isDirectory()) {
					familyModel.addElement(family);
				}
			}
		}
	}

	public boolean sourceChoiceIsLocal() {
		return currentSourceChoice.equals(LOCAL);
	}

	public Object getFamilyChoice() {
		return familyModel.getSelectedItem();
	}

	public String getSourceChoice() {
		return currentSourceChoice;
	}

	public ComponentServiceProviderConfig getConfig() {
		ComponentServiceProviderConfig newConfig = new ComponentServiceProviderConfig();
		try {
			if (sourceChoiceIsLocal()) {
				newConfig.setSource(null);
				File choice = (File) getFamilyChoice();
				newConfig.setFamilySource(choice.toURI().toURL());
			} else {
				Element choice = (Element) getFamilyChoice();
				newConfig.setSource(new URL(getSourceChoice()));
				newConfig.setFamilySource(new URL(choice
						.getAttributeValue("uri")));
			}
		} catch (MalformedURLException e) {
			logger.error(e);
			return null;
		}
		return newConfig;
	}
}
