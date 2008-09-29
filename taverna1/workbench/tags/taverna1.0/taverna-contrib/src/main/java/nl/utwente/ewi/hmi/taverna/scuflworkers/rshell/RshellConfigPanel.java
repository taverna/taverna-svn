/*
 * CVS
 * $Author: mereden $
 * $Date: 2006-09-28 16:36:57 $
 * $Revision: 1.3 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import nl.utwente.ewi.hmi.taverna.scuflworkers.rshell.RshellPortTypes.SymanticTypes;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * A JPanel that can configure the Rserv processor type. Very much inspired by
 * BeanshellConfigPanel, but without the output ports.
 * 
 * @author Stian Soiland, Tom Oinn, Chris Greenhalgh, Kevin Glover, Ingo Wassink
 */
public class RshellConfigPanel extends JPanel implements WorkflowModelViewSPI,
		ScuflModelEventListener {

	private static final long serialVersionUID = -1596783050410457435L;

	private RshellProcessor processor = null;

	private RshellConnectionSettings connectionSettings = null;

	private File currentDirectory = null;

	// elements for script edit panel
	private JTextArea scriptTextArea;

	// elements for port edit panel
	private JTable inputPortsTable;

	private JButton addInputButton;

	// elements for output port panel
	private JTable outputPortsTable;

	private JButton addOutputButton;

	// elements for connection edit panel
	private JTextField hostField;

	private JTextField portField;

	private JTextField usernameField;

	private JPasswordField passwordField;

	private JCheckBox keepSessionAliveField;

	@SuppressWarnings("serial")
	private Action deletePortAction = new AbstractAction("Delete Port",
			TavernaIcons.deleteIcon) {
		public void actionPerformed(ActionEvent e) {
			JTable table = null;
			if (e.getSource() instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) e.getSource();
				JPopupMenu menu = (JPopupMenu) item.getParent();
				Component component = menu.getInvoker();
				if (component instanceof JTable) {
					table = (JTable) component;
				}
			} else if (e.getSource() instanceof JTable) {
				table = (JTable) e.getSource();
			}

			if (table != null) {
				int[] rows = table.getSelectedRows();
				Port[] ports = new Port[rows.length];
				for (int index = 0; index < rows.length; index++) {
					ports[index] = (Port) table.getValueAt(rows[index], 0);
				}
				for (int index = 0; index < ports.length; index++) {
					processor.removePort(ports[index]);
				}
			}
		}
	};

	/**
	 * Create a new rserv configuration panel applying to the processor
	 * specified in the constructor
	 * 
	 * @param theProcessor
	 *            the processor
	 */
	@SuppressWarnings( { "unchecked", "serial" })
	public RshellConfigPanel(RshellProcessor theProcessor) {
		super(new BorderLayout());
		this.processor = theProcessor;
		this.connectionSettings = processor.getConnectionSettings();

		deletePortAction.putValue(Action.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_DELETE, 0));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JTabbedPane tabbedPane = new JTabbedPane();
		add(tabbedPane);

		/** **************************************************************** */
		/*
		 * Script panel
		 * /******************************************************************
		 */
		JPanel scriptEditPanel = new JPanel(new BorderLayout());
		scriptEditPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "R Script"));
		tabbedPane.addTab("Script", scriptEditPanel);

		scriptTextArea = new JTextArea();
		scriptTextArea.setText(processor.getScript());
		scriptTextArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (!processor.getScript().equals(scriptTextArea.getText())) {
					processor.setScript(scriptTextArea.getText());
				}
			}
		});

		JScrollPane scriptScrollPane = new JScrollPane(scriptTextArea);
		scriptEditPanel.add(scriptScrollPane, BorderLayout.CENTER);

		// control panel
		JPanel scriptControlPanel = new JPanel();
		scriptControlPanel.setLayout(new BoxLayout(scriptControlPanel,
				BoxLayout.X_AXIS));
		scriptEditPanel.add(scriptControlPanel, BorderLayout.SOUTH);

		JButton clearScriptButton = new JButton("Clear script");
		clearScriptButton.setIcon(TavernaIcons.deleteIcon);
		clearScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearScript();
			}
		});
		scriptControlPanel.add(clearScriptButton);

		scriptControlPanel.add(Box.createGlue());

		JButton scriptBrowseButton = new JButton("Browse...");
		scriptBrowseButton.setIcon(TavernaIcons.folderOpenIcon);
		scriptBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readScriptFile();
			}
		});
		scriptControlPanel.add(scriptBrowseButton);

		JComboBox portTypesCombo = new JComboBox(RshellPortTypes.SymanticTypes
				.values());
		portTypesCombo.setRenderer(new PortTypesListCellRenderer());

		/** **************************************************************** */
		/* Panel to edit the input ports */
		/** **************************************************************** */
		JPanel inputPortsPanel = new JPanel(new BorderLayout());
		inputPortsPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Input ports"));
		tabbedPane.addTab("Input ports", inputPortsPanel);

		inputPortsTable = new JTable(new PortTableModel() {
			protected Port[] getPorts() {
				return processor.getInputPorts();
			}
		});
		TableColumn inputPortsColumn = inputPortsTable.getColumnModel()
				.getColumn(1);
		inputPortsColumn.setCellRenderer(new PortTableCellRenderer());
		inputPortsTable.setIntercellSpacing(new Dimension(0, 0));
		inputPortsTable.getInputMap().put(
				(KeyStroke) deletePortAction.getValue(Action.ACCELERATOR_KEY),
				"DELETE_PORT");
		inputPortsTable.getActionMap().put("DELETE_PORT", deletePortAction);
		inputPortsTable.addMouseListener(new TableMouseListener());
		inputPortsTable.setPreferredSize(new Dimension(0, 0));
		inputPortsTable.getTableHeader().setPreferredSize(new Dimension(0, 0));
		inputPortsTable.setRowHeight(inputPortsTable.getRowHeight() + 2);
		inputPortsTable.getColumnModel().getColumn(0).setMinWidth(10);
		inputPortsTable.getColumnModel().getColumn(1).setMinWidth(10);
		inputPortsTable.setDefaultEditor(String.class, new DefaultCellEditor(
				portTypesCombo));
		inputPortsTable.setDefaultRenderer(Port.class,
				new DefaultTableCellRenderer() {
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						setIcon(TavernaIcons.inputPortIcon);
						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}
				});

		JPanel inputPortsTablePanel = new JPanel(new BorderLayout());
		inputPortsTablePanel.add(inputPortsTable, BorderLayout.CENTER);
		JScrollPane inputPortsPane = new JScrollPane(inputPortsTablePanel);
		inputPortsPane.setPreferredSize(new Dimension(0, 0));
		inputPortsPanel.add(inputPortsPane, BorderLayout.CENTER);
		inputPortsPane.setPreferredSize(new Dimension(0, 0));
		inputPortsPanel.add(inputPortsPane, BorderLayout.CENTER);

		JPanel inputPortControlPanel = new JPanel(new FlowLayout(
				FlowLayout.RIGHT));
		inputPortsPanel.add(inputPortControlPanel, BorderLayout.SOUTH);

		addInputButton = new JButton("Create input port",
				TavernaIcons.inputPortIcon);
		addInputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createInputPort();
			}
		});
		inputPortControlPanel.add(addInputButton);

		/** **************************************************************** */
		/* Panel to edit the output ports */
		/** **************************************************************** */
		JPanel outputPortsPanel = new JPanel(new BorderLayout());
		outputPortsPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Output ports"));
		tabbedPane.addTab("Output ports", outputPortsPanel);

		outputPortsTable = new JTable(new PortTableModel() {
			protected Port[] getPorts() {
				return processor.getOutputPorts();
			}
		});
		TableColumn outputPortsColumn = outputPortsTable.getColumnModel()
				.getColumn(1);
		outputPortsColumn.setCellRenderer(new PortTableCellRenderer());
		outputPortsTable.setIntercellSpacing(new Dimension(0, 0));
		outputPortsTable.getInputMap().put(
				(KeyStroke) deletePortAction.getValue(Action.ACCELERATOR_KEY),
				"DELETE_PORT");
		outputPortsTable.getActionMap().put("DELETE_PORT", deletePortAction);
		outputPortsTable.addMouseListener(new TableMouseListener());
		outputPortsTable.setPreferredSize(new Dimension(0, 0));
		outputPortsTable.getTableHeader().setPreferredSize(new Dimension(0, 0));
		outputPortsTable.setRowHeight(outputPortsTable.getRowHeight() + 2);
		outputPortsTable.getColumnModel().getColumn(0).setMinWidth(10);
		outputPortsTable.getColumnModel().getColumn(1).setMinWidth(10);
		outputPortsTable.setDefaultEditor(String.class, new DefaultCellEditor(
				portTypesCombo));
		outputPortsTable.setDefaultRenderer(Port.class,
				new DefaultTableCellRenderer() {
					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						setIcon(TavernaIcons.outputPortIcon);
						return super.getTableCellRendererComponent(table,
								value, isSelected, hasFocus, row, column);
					}
				});

		JPanel outputPortsTablePanel = new JPanel(new BorderLayout());
		outputPortsTablePanel.add(outputPortsTable, BorderLayout.CENTER);
		JScrollPane outputPortsPane = new JScrollPane(outputPortsTablePanel);
		outputPortsPane.setPreferredSize(new Dimension(0, 0));
		outputPortsPanel.add(outputPortsPane, BorderLayout.CENTER);
		outputPortsPane.setPreferredSize(new Dimension(0, 0));
		outputPortsPanel.add(outputPortsPane, BorderLayout.CENTER);

		JPanel outputPortControlPanel = new JPanel(new FlowLayout(
				FlowLayout.RIGHT));
		outputPortsPanel.add(outputPortControlPanel, BorderLayout.SOUTH);

		addOutputButton = new JButton("Create output port",
				TavernaIcons.outputPortIcon);
		addOutputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createOutputPort();
			}
		});
		outputPortControlPanel.add(addOutputButton);

		/** **************************************************************** */
		/* Panel for editing the connection settings */
		/** **************************************************************** */
		JPanel connectionPanel = new JPanel(new BorderLayout());
		tabbedPane.addTab("Connection settings", connectionPanel);

		JPanel connectionConfigPanel = new JPanel(new GridBagLayout());
		connectionConfigPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Connection settings"));
		connectionPanel.add(connectionConfigPanel, BorderLayout.NORTH);

		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.weightx = 0.0;
		labelConstraints.gridx = 0;
		labelConstraints.gridy = 0;
		labelConstraints.fill = GridBagConstraints.NONE;

		GridBagConstraints fieldConstraints = new GridBagConstraints();
		fieldConstraints.weightx = 1.0;
		fieldConstraints.gridx = 1;
		fieldConstraints.gridy = 0;
		fieldConstraints.fill = GridBagConstraints.HORIZONTAL;

		connectionConfigPanel.add(new JLabel("Hostname"), labelConstraints);
		labelConstraints.gridy++;
		hostField = new JTextField(connectionSettings.getHost());
		hostField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				connectionSettings.setHost(hostField.getText());
			}
		});
		connectionConfigPanel.add(hostField, fieldConstraints);
		fieldConstraints.gridy++;

		connectionConfigPanel.add(new JLabel("Port"), labelConstraints);
		labelConstraints.gridy++;
		portField = new JTextField(Integer.toString(connectionSettings
				.getPort()));
		portField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				try {
					connectionSettings.setPort(Integer.parseInt(portField
							.getText()));
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "The value '"
							+ portField.getText()
							+ "' is not a valid port number",
							"Wrong port number", JOptionPane.ERROR_MESSAGE,
							null);
				}
			}
		});
		connectionConfigPanel.add(portField, fieldConstraints);
		fieldConstraints.gridy++;

		connectionConfigPanel.add(new JLabel("Username"), labelConstraints);
		labelConstraints.gridy++;
		usernameField = new JTextField(connectionSettings.getUsername());
		usernameField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				connectionSettings.setUsername(usernameField.getText());
			}
		});
		connectionConfigPanel.add(usernameField, fieldConstraints);
		fieldConstraints.gridy++;

		connectionConfigPanel.add(new JLabel("Password"), labelConstraints);
		labelConstraints.gridy++;
		passwordField = new JPasswordField(connectionSettings.getPassword());
		passwordField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				connectionSettings.setPassword(new String(passwordField
						.getPassword()));
			}
		});
		connectionConfigPanel.add(passwordField, fieldConstraints);
		fieldConstraints.gridy++;

		connectionConfigPanel.add(new JLabel("Keep session alive"),
				labelConstraints);
		labelConstraints.gridy++;
		keepSessionAliveField = new JCheckBox();
		keepSessionAliveField.setSelected(connectionSettings
				.isKeepSessionAlive());
		keepSessionAliveField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connectionSettings.setKeepSessionAlive(keepSessionAliveField
						.isSelected());
			}
		});
		connectionConfigPanel.add(keepSessionAliveField, fieldConstraints);
		fieldConstraints.gridy++;

		/** **************************************************************** */
		/*
		 * Info panel
		 * /******************************************************************
		 */
		JPanel infoPanel = new JPanel(new BorderLayout());
		tabbedPane.addTab("Info", infoPanel);

		JPanel infoContentPanel = new JPanel(new GridBagLayout());
		infoContentPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Info"));
		infoPanel.add(infoContentPanel, BorderLayout.NORTH);

		GridBagConstraints infoConstraints = new GridBagConstraints();
		infoConstraints.weightx = 0.0;
		infoConstraints.gridx = 0;
		infoConstraints.gridy = 0;
		infoConstraints.fill = GridBagConstraints.NONE;

		infoContentPanel.add(new JLabel("Rshell, 2006"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("Ingo Wassink"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("Human Media Interaction"),
				infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("University of Twente"),
				infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel.add(new JLabel("BioRange"), infoConstraints);
		infoConstraints.gridy++;
		infoContentPanel
				.add(
						new JLabel(
								"<html><a href='http://www.ewi.utwente.nl/~biorange'>www.ewi.utwente.nl/~biorange</a></html>"),
						infoConstraints);
		infoConstraints.gridy++;

		setVisible(true);
	}

	public void attachToModel(ScuflModel theModel) {
		if (theModel != null) {
			theModel.addListener(this);
		}
	}

	public void detachFromModel() {
		processor.getModel().removeListener(this);
	}

	/**
	 * Method for getting the name of this configuration panel
	 * 
	 * @return the name
	 */
	public String getName() {
		if (processor == null) {
			return "Rserv config panel for unknown processor";
		} else {
			return "Configuring Rserv for " + processor.getName();
		}
	}

	/**
	 * Method for getting the icon for this configuration panel
	 * 
	 * @return the icon
	 */
	public ImageIcon getIcon() {
		return ProcessorHelper.getPreferredIcon(processor);
	}

	/**
	 * Method for receiveing a model event
	 */
	public void receiveModelEvent(ScuflModelEvent event) {
		inputPortsTable.tableChanged(new TableModelEvent(inputPortsTable
				.getModel()));
		outputPortsTable.tableChanged(new TableModelEvent(outputPortsTable
				.getModel()));
	}

	/**
	 * Method for reading a script file
	 */
	private void readScriptFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new RshellFileFilter());
		fileChooser.setAcceptAllFileFilterUsed(true);
		if (currentDirectory != null) {
			fileChooser.setCurrentDirectory(currentDirectory);
		}
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentDirectory = fileChooser.getCurrentDirectory();
			File selectedFile = fileChooser.getSelectedFile();

			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						selectedFile));

				String line;
				StringBuffer buffer = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
					buffer.append("\n");
				}
				reader.close();

				processor.setScript(buffer.toString());
				scriptTextArea.setText(buffer.toString());

			} catch (FileNotFoundException ffe) {
				JOptionPane.showMessageDialog(this, "File '"
						+ selectedFile.getName() + "' not found",
						"File not found", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(this, "Can not read file '"
						+ selectedFile.getName() + "'", "Can not read file",
						JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	/**
	 * Method for clearing the script
	 * 
	 */
	private void clearScript() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to clear the script?",
				"Clearing the script", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			processor.setScript("");
			scriptTextArea.setText("");
		}

	}

	/**
	 * Method for creating a new input port
	 */
	private void createInputPort() {
		String portName = JOptionPane.showInputDialog(this,
				"Enter the name of the input port to be created",
				"Create a new input port", JOptionPane.QUESTION_MESSAGE);
		if (portName != null) {
			try {
				RshellInputPort inputPort = new RshellInputPort(processor,
						portName);
				processor.addPort(inputPort);
			} catch (PortCreationException pce) {
				JOptionPane.showMessageDialog(null, "Can not create port: "
						+ pce.getMessage(), "Error creating port",
						JOptionPane.ERROR_MESSAGE);
			} catch (DuplicatePortNameException dpne) {
				JOptionPane.showMessageDialog(null,
						"A port with the name '" + portName
								+ "' already exists, specify different name",
						"Error creating port", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Method for creating a new input port
	 */
	private void createOutputPort() {
		String portName = JOptionPane.showInputDialog(this,
				"Enter the name of the output port to be created",
				"Create a new output port", JOptionPane.QUESTION_MESSAGE);
		if (portName != null) {
			try {
				RshellOutputPort outputPort = new RshellOutputPort(processor,
						portName);
				processor.addPort(outputPort);
			} catch (PortCreationException pce) {
				JOptionPane.showMessageDialog(null, "Can not create port: "
						+ pce.getMessage(), "Error creating port",
						JOptionPane.ERROR_MESSAGE);
			} catch (DuplicatePortNameException dpne) {
				JOptionPane.showMessageDialog(null,
						"A port with the name '" + portName
								+ "' already exists, specify different name",
						"Error creating port", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Class which calls the 'getDescription' method instead of the toString
	 */
	class PortTypesListCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 9013505290926408000L;

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			this.setText(((RshellPortTypes.SymanticTypes) value).description);
			return this;
		}

	}

	/**
	 * Inner class for input and output port panel
	 */
	private abstract class PortTableModel extends AbstractTableModel {
		protected abstract Port[] getPorts();

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return getPorts().length;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex > 0;
		}

		@SuppressWarnings("unchecked")
		public Class getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Port.class;

			case 1:
				return String.class;

			default:
				return null;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return getPorts()[rowIndex];

			case 1:
				Port port = getPorts()[rowIndex];
				if (port instanceof RshellInputPort)
					return ((RshellInputPort) port).getSymanticType();
				else
					return ((RshellOutputPort) port).getSymanticType();
			default:
				return null;
			}
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				break;
			case 1:
				Port port = getPorts()[rowIndex];
				SymanticTypes symanticType = (RshellPortTypes.SymanticTypes) value;

				if (port instanceof RshellInputPort)
					((RshellInputPort) port).setSymanticType(symanticType);
				else
					((RshellOutputPort) port).setSymanticType(symanticType);
				break;
			}
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "Name";

			case 1:
				return "Semantic port type";

			default:
				return "Eh? Shouldn't be here";
			}
		}
	}

	/**
	 * Renderer which shows the port type description for the port type instead
	 * of calling the toString method
	 */
	private class PortTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 2687130380275907870L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component component = super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);
			((DefaultTableCellRenderer) component)
					.setText(((RshellPortTypes.SymanticTypes) (value)).description);
			return component;
		}
	}

	/**
	 * Table listener, shows popup menu when mouse is pressed or released
	 */
	private class TableMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent me) {
			popup(me);
		}

		public void mouseReleased(MouseEvent me) {
			popup(me);
		}

		protected void popup(MouseEvent me) {
			if (me.isPopupTrigger()) {
				JTable table = (JTable) me.getSource();
				int rowIndex = table.rowAtPoint(me.getPoint());
				table.setRowSelectionInterval(rowIndex, rowIndex);
				Port port = (Port) table.getValueAt(rowIndex, 0);

				if (port != null) {
					JPopupMenu menu = new JPopupMenu();
					menu.add(new JMenuItem(deletePortAction));
					menu.show(table, me.getX(), me.getY());
				}
			}
		}
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		if (processor != null) {
			detachFromModel();
		}
	};

}
