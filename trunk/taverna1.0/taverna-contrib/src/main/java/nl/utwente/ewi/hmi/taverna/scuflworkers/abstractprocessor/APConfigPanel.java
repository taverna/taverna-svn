/**
 * CVS
 * $Author: mereden $
 * $Date: 2006-09-28 16:36:56 $
 * $Revision: 1.3 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * The configuration panel for abstract processors
 * 
 * @author Ingo Wassink
 */
public class APConfigPanel extends JPanel implements WorkflowModelViewSPI,
		ScuflModelEventListener {

	private static final long serialVersionUID = -1596783050410457435L;

	private APProcessor processor = null;

	// elements for description panel
	private JTextArea taskDescriptionTextArea;

	// elements for port edit panel
	private JTable inputPortsTable;

	private JButton addInputButton;

	// elements for output port panel
	private JTable outputPortsTable;

	private JButton addOutputButton;

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
	public APConfigPanel(APProcessor theProcessor) {
		super(new BorderLayout());
		this.processor = theProcessor;

		deletePortAction.putValue(Action.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_DELETE, 0));

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JTabbedPane tabbedPane = new JTabbedPane();

		/** **************************************************************** */
		/*
		 * description panel
		 * /******************************************************************
		 */
		JPanel taskDescriptionEditPanel = new JPanel(new BorderLayout());
		taskDescriptionEditPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Task description"));
		tabbedPane.addTab("Task description", taskDescriptionEditPanel);

		taskDescriptionTextArea = new JTextArea(1, 1);
		taskDescriptionTextArea.setText(processor.getTaskDescription());
		taskDescriptionTextArea.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				if (!processor.getTaskDescription().equals(
						taskDescriptionTextArea.getText())) {
					processor.setTaskDescription(taskDescriptionTextArea
							.getText());
				}
			}
		});
		JScrollPane taskDescriptionScrollPane = new JScrollPane(
				taskDescriptionTextArea);
		taskDescriptionEditPanel.add(taskDescriptionScrollPane,
				BorderLayout.CENTER);

		// control panel
		JPanel scriptControlPanel = new JPanel();
		scriptControlPanel.setLayout(new BoxLayout(scriptControlPanel,
				BoxLayout.X_AXIS));
		taskDescriptionEditPanel.add(scriptControlPanel, BorderLayout.SOUTH);

		JButton clearTaskDescriptionButton = new JButton("Clear description");
		clearTaskDescriptionButton.setIcon(TavernaIcons.deleteIcon);
		clearTaskDescriptionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearTaskDescription();
			}
		});
		scriptControlPanel.add(clearTaskDescriptionButton);

		scriptControlPanel.add(Box.createGlue());

		/** **************************************************************** */
		/* Panel to edit the input ports */
		/** **************************************************************** */
		JPanel inputPortsPanel = new JPanel(new BorderLayout());
		inputPortsPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Input ports"));
		tabbedPane.addTab("Input ports", inputPortsPanel);

		inputPortsTable = new JTable(new PortTableModel() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected Port[] getPorts() {
				return processor.getInputPorts();
			}
		});
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

		infoContentPanel.add(new JLabel("Abstract processor, 2006"),
				infoConstraints);
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

		add(tabbedPane);
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
	 * Method for clearing the task description
	 * 
	 */
	private void clearTaskDescription() {
		if (JOptionPane.showConfirmDialog(this,
				"Do you really want to clear the script?",
				"Clearing the script", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			processor.setTaskDescription("");
			taskDescriptionTextArea.setText("");
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
				InputPort inputPort = new InputPort(processor, portName);
				inputPort.setSyntacticType("'text/plain'");
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
				OutputPort outputPort = new OutputPort(processor, portName);
				outputPort.setSyntacticType("'text/plain'");
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
	 * Inner class for input and output port panel
	 */
	private abstract class PortTableModel extends AbstractTableModel {
		protected abstract Port[] getPorts();

		public int getColumnCount() {
			return 1;
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
			default:
				return null;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return getPorts()[rowIndex];
			default:
				return null;
			}
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "Name";

			default:
				return null;
			}
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
		detachFromModel();		
	};

}
