/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sadi.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.taverna.t2.activities.sadi.RestrictionNode;
import net.sf.taverna.t2.activities.sadi.SADIActivityOutputPort;
import net.sf.taverna.t2.activities.sadi.SADIActivityPort;
import net.sf.taverna.t2.activities.sadi.SADIRegistries;
import net.sf.taverna.t2.activities.sadi.SADIUtils;
import net.sf.taverna.t2.activities.sadi.SADIRegistries.RegistryDetails;
import net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIActivityIcon;
import net.sf.taverna.t2.activities.sadi.servicedescriptions.SADIServiceDescription;
import net.sf.taverna.t2.activities.sadi.utils.LabelUtils;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.lang.ui.icons.Icons;
import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.Service;
import ca.wilkinsonlab.sadi.common.SADIException;

/**
 * Dialog for users to select services found by the SADI service discovery.
 * 
 * @author David Withers
 */
public class SADIServiceDiscoveryDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static final String selectMessage = "Select SADI services to add to the workflow.";
	private static final String searchMessage = "Searching the SADI repository to find services...";
	private static final String errorMessage = "Error while accessing the SADI registry.";
	private static final String noServicesMessage = "No services were found.";
	private static final Icon listIcon = new ImageIcon(SADIServiceDiscoveryDialog.class
			.getResource("/sadi-logo32x32.png"));

	private SADIActivityPort sadiActivityPort;
	private JPanel titlePanel, buttonPanel;
	private JLabel titleLabel, titleIcon, searchingIcon;
	private DialogTextArea titleMessage;
	private JButton connectButton, cancelButton;

	private JList serviceList;
	private JScrollPane serviceListPane;
	private DefaultListModel listModel = new DefaultListModel();

	private boolean result = false;

	/**
	 * Constructs a new SDAIServiceDiscoveryDialog.
	 * 
	 * @param sadiActivityPort
	 */
	public SADIServiceDiscoveryDialog(SADIActivityPort sadiActivityPort) {
		this.sadiActivityPort = sadiActivityPort;
		initialize();
		findServices();
	}

	/**
	 * Initializes the dialog components.
	 */
	private void initialize() {
		setTitle("SADI Service Discovery");
		setLayout(new BorderLayout());
		setModal(true);

		// title
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		SADIViewUtils.addDivider(titlePanel, SwingConstants.BOTTOM, true);

		if (sadiActivityPort instanceof SADIActivityOutputPort) {
			titleLabel = new JLabel("SADI services that consume "
					+ LabelUtils.getLabel(sadiActivityPort.getOntClass()));
		} else {
			titleLabel = new JLabel("SADI services that produce "
					+ LabelUtils.getLabel(sadiActivityPort.getOntClass()));
		}
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea(selectMessage);
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);

		// content
		serviceList = new JList(listModel);
		serviceList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				connectButton.setEnabled(!serviceList.isSelectionEmpty());
			}
		});
		serviceList.setCellRenderer(new ServiceListCellRenderer());

		serviceListPane = new JScrollPane(serviceList);
		serviceListPane.setBorder(null);

		Icon icon = new ImageIcon(SADIActivityIcon.class.getResource("/SADI_spinner.gif"));
		searchingIcon = new JLabel(icon);

		// buttons
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		serviceList.setBackground(buttonPanel.getBackground());
		SADIViewUtils.addDivider(buttonPanel, SwingConstants.TOP, true);

		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = true;
				setVisible(false);
			}
		});
		connectButton.setEnabled(false);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		layoutDialog();
	}

	/**
	 * Sets the components layout within the dialog.
	 */
	private void layoutDialog() {
		setLayout(new BorderLayout());

		// title
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0,
				10)));
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);

		// buttons
		buttonPanel.add(cancelButton);
		buttonPanel.add(connectButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Show the dialog relative to the component. If the component is null then
	 * the dialog is shown in the centre of the screen.
	 * 
	 * Returns true if the user selects connect.
	 * 
	 * @param component
	 *            the component that the dialog is shown relative to
	 * @return true if the user selects connect
	 */
	public boolean show(Component component) {
		setLocationRelativeTo(component);
		setPreferredSize(new Dimension(450, 400));
		setVisible(true);
		dispose();
		return result;
	}

	/**
	 * Returns the selected services. If no services are selected an empty list
	 * is returned.
	 * 
	 * @return the selected services
	 */
	public List<SADIServiceDescription> getSelectedServices() {
		List<SADIServiceDescription> selectedServices = new ArrayList<SADIServiceDescription>();
		for (Object selection : serviceList.getSelectedValues()) {
			if (selection instanceof SADIServiceDescription) {
				SADIServiceDescription service = (SADIServiceDescription) selection;
				selectedServices.add(service);
			}
		}
		return selectedServices;
	}

	protected void findServices() {
		new Thread("SADI service discovery") {
			public void run() {
				try {
					setSearching(true);
					Collection<SADIServiceDescription> services = findServices(sadiActivityPort);
					setSearching(false);
					if (services.size() > 0) {
						for (SADIServiceDescription service : services) {
							listModel.addElement(service);
						}
					} else {
						titleMessage.setText(noServicesMessage);
						titleIcon.setIcon(Icons.warningIcon);
					}
				} catch (IOException e) {
					setSearching(false);
					titleMessage.setText(errorMessage);
					titleIcon.setIcon(Icons.severeIcon);
				} catch (SADIException e) {
					setSearching(false);
					titleMessage.setText(errorMessage);
					titleIcon.setIcon(Icons.severeIcon);
				}
			}

		}.start();
	}

	protected Collection<SADIServiceDescription> findServices(SADIActivityPort sadiActivityPort)
			throws IOException, SADIException {
		Collection<SADIServiceDescription> serviceDescriptions = new HashSet<SADIServiceDescription>();
		for (Entry<RegistryDetails, Registry> entry : SADIRegistries.getRegistryMap().entrySet()) {
			RegistryDetails registryDetails = entry.getKey();
			Registry registry = entry.getValue();
			Collection<? extends Service> services;
			if (sadiActivityPort instanceof SADIActivityOutputPort) {
				services = registry.findServicesByInputClass(sadiActivityPort.getOntClass());
			} else {
				services = registry.findServicesByConnectedClass(sadiActivityPort.getOntClass());
			}
			for (Service service : services) {
				serviceDescriptions.add(createServiceDescription(registryDetails, service));
			}
		}
		return serviceDescriptions;
	}

	/**
	 * Creates a {@link SADIServiceDescription} from registry and service
	 * details.
	 * 
	 * @param registryDetails
	 * @param service
	 * @return a new SADIServiceDescription
	 */
	private SADIServiceDescription createServiceDescription(RegistryDetails registryDetails,
			Service service) {
		SADIServiceDescription ssd = new SADIServiceDescription();
		ssd.setSparqlEndpoint(registryDetails.getSparqlEndpoint());
		ssd.setGraphName(registryDetails.getGraphName());
		ssd.setServiceURI(service.getURI());
		ssd.setName(service.getName());
		ssd.setDescription(service.getDescription());
		try {
			List<String> properties = new ArrayList<String>();
			RestrictionNode outputRestrictionTree = SADIUtils.buildOutputRestrictionTree(service.getInputClass(), service.getOutputClass());
			for (List<String> path : SADIUtils.getDefaultRestrictionPaths(outputRestrictionTree)) {
				RestrictionNode restriction = SADIUtils.getRestriction(outputRestrictionTree, path);
				if (restriction.getOntProperty() != null) {
					properties.add(LabelUtils.getLabel(restriction.getOntProperty()));
				}
			}
			ssd.setProperties(properties);
		} catch (SADIException e) {
		}
		return ssd;
	}

	protected void setSearching(boolean searching) {
		if (searching) {
			titleMessage.setText(searchMessage);
			remove(serviceList);
			add(searchingIcon, BorderLayout.CENTER);
			pack();
		} else {
			titleMessage.setText(selectMessage);
			remove(searchingIcon);
			add(serviceListPane, BorderLayout.CENTER);
			pack();
		}
	}

	private final class ServiceListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			Component renderer;
			if (value instanceof SADIServiceDescription) {
				renderer = super.getListCellRendererComponent(list,
						serviceToHtml((SADIServiceDescription) value), index, isSelected,
						cellHasFocus);
				
				/* prevent service description from appearing on a single line,
				 * no matter how big the dialog gets; if you can think of a
				 * better way to this (styling the HTML didn't work), please
				 * fix it...
				 * FYI, the increase to preferredSize.height may be because the
				 * automatic height calculation doesn't take wrapping into effect,
				 * in which case if the description is long enough to wrap twice,
				 * it will still look stupid; I'm hoping it's just that having
				 * a preferred height at all triggers the correct calculation...
				 */
				Dimension preferredSize = renderer.getPreferredSize();
				preferredSize.width = 400;
				preferredSize.height += 20;
				renderer.setPreferredSize(preferredSize);
			} else {
				renderer = super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
			}
			if (renderer instanceof JLabel) {
				JLabel rendererLabel = (JLabel) renderer;
				SADIViewUtils.addDivider(rendererLabel, SwingConstants.BOTTOM, true);
				rendererLabel.setIcon(listIcon);
			}
			return renderer;
		}

		/**
		 * Returns the service name and description as html.
		 * 
		 * @param service
		 */
		private String serviceToHtml(SADIServiceDescription service) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><dl>");
			sb.append("<dt><b>");
			sb.append(service.getName());
			sb.append("</b></dt><dd>");
			sb.append(service.getDescription());
			sb.append("</dd>");
			sb.append("<dt><b>Properties attached:</b></dt><dd>");
			boolean first = true;
			for (String property : service.getProperties()) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(property);
				first = false;
			}
			sb.append("</dd></dl>");
			return sb.toString();
		}

	}

}
