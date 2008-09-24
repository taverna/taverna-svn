/*
 *
 * Copyright (C) 2003 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.JTextComponent;

import org.embl.ebi.escience.scuflui.shared.ShadedLabel;

import uk.ac.man.cs.img.fetaClient.resource.FetaResources;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;

/**
 * Panel showing a service description
 * 
 * @author Pinar Alper
 * @author Stian Soiland
 */
@SuppressWarnings("serial")
public class ServiceFormPanel extends JTabbedPane {

	private ServiceModelAdaptor serviceModelAdaptor;

	// private BasicServiceModel serviceModel;

	private JLabel typeLabel;

	private JTextField typeField;

	private JLabel organisationLabel;

	private JTextField organisationField;

	private JTextField locationField, interfaceField, descLocationField;

	private JLabel locationLabel, interfaceLabel, descLocationLabel;

	private JLabel descriptionLabel;

	private JTextComponent descriptionField;

	private JLabel nameLabel;

	private JTextField nameField;

	// private JLabel operationLabel;
	// private JTextField operationField;

	private JLabel operNameLabel;

	private JTextField operNameField;

	private JLabel operDescriptionLabel;

	private JTextComponent operDescriptionField;

	private JLabel operationMethodLabel;

	private JTextField operationMethodField;

	private JLabel operationResourceLabel;

	private JTextField operationResourceField;

	// private JLabel operationResourceContentLabel;
	// private JTextField operationResourceContentField;

	private JLabel operationTaskLabel;

	private JTextField operationTaskField;

	// private JLabel operationApplicationLabel;
	// private JTextField operationApplicationField;

	private JPanel operationPanel;

	private JPanel servicePanel;

	private JLabel parametersLabel;

	private JPanel parametersField;

    private JScrollPane parametersScroll;

	private JPanel inputParameters;

	private JPanel outputParameters;

	// private JPanel parameterPanel;

	/**
	 * 
	 */
	public ServiceFormPanel(ServiceModelAdaptor adaptor) {
		super();
		this.serviceModelAdaptor = adaptor;
		adaptor.setView(this);
		servicePanel = new JPanel();
		operationPanel = new JPanel();

		// parameterPanel = new JPanel();

		initialize();

	}

	/**
	 * @param arg0
	 */

	@SuppressWarnings("serial")
	public void initialize() {
		/** Initialize Service Panel * */
		servicePanel.setLayout(new SpringLayout());
		/* FRDY this */
		nameLabel = new JLabel("Service name");
		nameField = new JTextField();
		nameField.setMaximumSize(new Dimension(300, 20));
		nameField.setEditable(false);

		descriptionLabel = new JLabel("Service description");
		descriptionField = multiLabel("");

		parametersLabel = new JLabel("Parameters");
		parametersField = new JPanel(new GridBagLayout());
        parametersScroll = new JScrollPane(parametersField, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


		inputParameters = new JPanel();
		outputParameters = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
		parametersField.add(inputParameters, c);
		parametersField.add(outputParameters, c);
		// filler
		c.weighty = 0.1;
		c.weightx = 0.1;
        c.fill = GridBagConstraints.VERTICAL;
		parametersField.add(new JPanel(), c);

		JScrollPane scrollingDescriptionArea =
			new JScrollPane(descriptionField,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// scrollingDescriptionArea.setBorder(BorderFactory.createEtchedBorder());
		scrollingDescriptionArea.setOpaque(false);

		locationLabel = new JLabel("Service endpoint location");
		locationField = new JTextField();
		locationField.setMaximumSize(new Dimension(300, 20));
		locationField.setEditable(false);

		interfaceLabel = new JLabel("Service interface location");
		interfaceField = new JTextField();
		interfaceField.setMaximumSize(new Dimension(300, 20));
		interfaceField.setEditable(false);

		organisationLabel = new JLabel("Organisation name");
		organisationField = new JTextField();
		organisationField.setMaximumSize(new Dimension(300, 20));
		organisationField.setEditable(false);

		typeLabel = new JLabel("Service type");

		typeField = new JTextField();
		typeField.setMaximumSize(new Dimension(300, 20));
		typeField.setEditable(false);

		descLocationLabel = new JLabel("Description File Location");
		descLocationField = new JTextField();
		descLocationField.setMaximumSize(new Dimension(300, 20));
		descLocationField.setEditable(false);

		servicePanel.setOpaque(true);

		servicePanel.add(nameLabel);
		servicePanel.add(nameField);

		servicePanel.add(descriptionLabel);
		servicePanel.add(scrollingDescriptionArea);

		servicePanel.add(parametersLabel);
		servicePanel.add(parametersScroll);

		servicePanel.add(descLocationLabel);
		servicePanel.add(descLocationField);

		servicePanel.add(locationLabel);
		servicePanel.add(locationField);

		servicePanel.add(interfaceLabel);
		servicePanel.add(interfaceField);

		servicePanel.add(organisationLabel);
		servicePanel.add(organisationField);

		servicePanel.add(typeLabel);
		servicePanel.add(typeField);

		final int COLS = 2;
		final int ROWS = 8;
		SpringUtilities.makeCompactGrid(servicePanel, ROWS, COLS, 6, 6, 6, 6);

		/** Initialize Operation Panel * */

		operationPanel.setLayout(new SpringLayout());

		operNameLabel = new JLabel("Operation name");
		operNameField = new JTextField();
		operNameField.setMaximumSize(new Dimension(300, 20));
		operNameField.setEnabled(false);

		operDescriptionLabel = new JLabel("Operation description");
		operDescriptionField = multiLabel("");

		JScrollPane scrollingOperDescriptionArea =
			new JScrollPane(operDescriptionField,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollingOperDescriptionArea.setBorder(BorderFactory.createEtchedBorder());

		operationMethodLabel = new JLabel("Operation Method");
		operationMethodLabel.setIcon(FetaResources.getIcon("selectedClass.gif"));
		operationMethodField = new JTextField();
		operationMethodField.setMaximumSize(new Dimension(300, 20));
		operationMethodField.setEnabled(false);

		operationResourceLabel = new JLabel("Operation Resource");
		operationResourceLabel.setIcon(FetaResources.getIcon("selectedClass.gif"));
		operationResourceField = new JTextField();
		operationResourceField.setMaximumSize(new Dimension(300, 20));
		operationResourceField.setEnabled(false);

		// operationResourceContentLabel = new JLabel("Operation Resource
		// Content");
		// operationResourceContentLabel.setIcon(FetaResources.getIcon("selectedClass.gif"));
		// operationResourceContentField = new JTextField();
		// operationResourceContentField.setMaximumSize(new Dimension(300,20));
		// operationResourceContentField.setEnabled(false);

		operationTaskLabel = new JLabel("Operation Task");
		operationTaskLabel.setIcon(FetaResources.getIcon("selectedClass.gif"));
		operationTaskField = new JTextField();
		operationTaskField.setMaximumSize(new Dimension(300, 20));
		operationTaskField.setEnabled(false);

		// operationApplicationLabel = new JLabel("Operation Application");
		// operationApplicationLabel.setIcon(FetaResources.getIcon("selectedClass.gif"));
		// operationApplicationField = new JTextField();
		// operationApplicationField.setMaximumSize(new Dimension(300,20));
		// operationApplicationField.setEnabled(false);

		operationPanel.setOpaque(true);

		operationPanel.add(operNameLabel);
		operationPanel.add(operNameField);

		operationPanel.add(operDescriptionLabel);
		operationPanel.add(operDescriptionField);

		operationPanel.add(operationMethodLabel);
		operationPanel.add(operationMethodField);

		operationPanel.add(operationResourceLabel);
		operationPanel.add(operationResourceField);

		// operationPanel.add(operationResourceContentLabel);
		// operationPanel.add(operationResourceContentField);

		operationPanel.add(operationTaskLabel);
		operationPanel.add(operationTaskField);

		// operationPanel.add(operationApplicationLabel);
		// operationPanel.add(operationApplicationField);

		SpringUtilities.makeCompactGrid(operationPanel, 5, 2, 6, 6, 6, 6);

		this.add("Brief Info", servicePanel);
		// this.add("Operation",operationPanel);
		// this.add("Parameters",parameterPanel);

		this.setBackgroundAt(0, ShadedLabel.TAVERNA_BLUE);
		// this.setBackgroundAt(1, ShadedLabel.TAVERNA_ORANGE);
		// this.setBackgroundAt(2, ShadedLabel.TAVERNA_GREEN);

	}

	/**
	 * @return
	 */
	public ServiceModelAdaptor getServiceModelAdaptor() {
		return serviceModelAdaptor;
	}

	/**
	 * @param adaptor
	 */
	public void setServiceModelAdaptor(ServiceModelAdaptor adaptor) {
		serviceModelAdaptor = adaptor;
	}

	public void setServiceName(String serviceName) {
		nameField.setText(serviceName);
	}

	public void setOperationName(String operationName) {
		// this.operationField.setText(operationName);
		this.operNameField.setText(operationName);
	}

	public void setOperationDescriptionText(String operationDesc) {

		this.operDescriptionField.setText(operationDesc);
	}

	public void setServiceDescriptionText(String serviceDescription) {
		descriptionField.setText(serviceDescription);
	}

	public void setLocationURL(String location) {
		locationField.setText(location);
	}

	public void setInterfaceLocationURL(String interfaceLocation) {
		interfaceField.setText(interfaceLocation);
	}

	public void setServiceType(ServiceType type) {
		typeField.setText(type.toString());
	}

	public void setOrganisationName(String organisation) {
		organisationField.setText(organisation);
	}

	public void setServiceDescriptionLocation(String descLocation) {
		descLocationField.setText(descLocation);
	}

	public void setOperationMethod(String method) {
		operationMethodField.setText(method);
	}

	public void setOperationTask(String task) {
		operationTaskField.setText(task);
	}

	public void setOperationResource(String resource) {
		operationResourceField.setText(resource);
	}

	public void setInputParameters(Map<String, String> parameters) {
		setParameters(inputParameters, "Inputs", ShadedLabel.TAVERNA_BLUE,
			parameters);
	}

	public void setOutputParameters(Map<String, String> parameters) {
		setParameters(outputParameters, "Outputs", ShadedLabel.TAVERNA_GREEN,
			parameters);
	}

	private void setParameters(JPanel panel, String header, Color color,
		Map<String, String> parameters) {
		panel.removeAll();
		if (parameters == null || parameters.size() == 0) {
			return; // Avoid showing empty list
		}
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;

		panel.add(new ShadedLabel(header, color), c);
		for (String parameter : parameters.keySet()) {
			JLabel label =
				new JLabel("<html><strong>" + parameter + "</strong></html>");
			panel.add(label, c);
			JTextComponent description = multiLabel(parameters.get(parameter));
			panel.add(description, c);
		}

		// filler
		c.weighty = 0.2;
		c.weightx = 0.2;
		c.fill = GridBagConstraints.VERTICAL;
		panel.add(new JPanel(), c);
		panel.revalidate();
		panel.repaint();
	}

	/**
	 * Make a multi-line label as a passive JEditorPane.
	 * 
	 * @param text
	 *            label to display
	 * @return JEditorPane instance showing label text
	 */
	public static JTextComponent multiLabel(String text) {
//		JEditorPane editor = new JEditorPane("text/plain", text);
		JTextArea editor = new JTextArea(text);
		editor.setEditable(false);
		editor.setOpaque(false);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);
		editor.setFont(Font.getFont("Dialog"));
		//editor.setAlignmentX(Component.LEFT_ALIGNMENT);
		// Avoid stealing all width
		editor.setMinimumSize(new Dimension(25, 10));
		return editor;
	}

	/*
	 * public void setOperationResourceContent(String resourceContent) {
	 * this.operationResourceContentField.setText(resourceContent); }
	 */
	/*
	 * public void setOperationApplication(String application) {
	 * this.operationApplicationField.setText(application); }
	 */
}
