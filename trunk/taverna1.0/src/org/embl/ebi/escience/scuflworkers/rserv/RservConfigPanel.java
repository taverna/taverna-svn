/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.TextAreaDefaults;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

/**
 * A JPanel that can configure the Rserv processor type. Very much inspired by
 * BeanshellConfigPanel, but without the output ports.
 * 
 * @author Stian Soiland, Tom Oinn, Chris Greenhalgh, Kevin Glover
 */
public class RservConfigPanel extends JPanel implements ScuflUIComponent,
		ScuflModelEventListener {
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
				if (port instanceof RservInputPort) {
					return ((RservInputPort) port).getJavaType();
				}
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
				if (port instanceof RservInputPort) {
					((RservInputPort) port).setJavaType((String) value);
				}
				break;
			}
		}

		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return "Name";

			case 1:
				return "Java Type";

			default:
				return "Eh? Shouldn't be here";
			}
		}
	}

	RservProcessor processor = null;

	JEditTextArea scriptText;

	JTable inputTable;

	JTable outputTable;

	JButton addInputButton;

	JTextField addInputField;

	JButton addOutputButton;

	JTextField addOutputField;

	Action deletePortAction = new AbstractAction("Delete Port",
			ScuflIcons.deleteIcon) {
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
	 */
	public RservConfigPanel(RservProcessor bp) {
		super(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.processor = bp;

		scriptText = new JEditTextArea(new TextAreaDefaults());
		scriptText.setText(processor.getScript());
		scriptText.setTokenMarker(new JavaTokenMarker());
		scriptText.setCaretPosition(0);
		scriptText.setPreferredSize(new Dimension(0, 0));
		scriptText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				// Maybe a bit overkill to save every time focus is lost..
				// but I couldn't find any simple way to listen for
				// "on change" either.. This is anyway better than forcing
				// people to press a "Save" button --Stian
				if (!processor.getScript().equals(scriptText.getText())) {
					processor.setScript(scriptText.getText());
				}
			}
		});

		JPanel scriptEditPanel = new JPanel(new BorderLayout());
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);

		// Panel to edit the input ports
		JPanel portEditPanel = new JPanel(new GridLayout(0, 1));

		MouseListener tableMouseListener = new MouseAdapter() {
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
		};

		deletePortAction.putValue(Action.ACCELERATOR_KEY, KeyStroke
				.getKeyStroke(KeyEvent.VK_DELETE, 0));

		JComboBox inputTypesCombo = new JComboBox(new Vector(
				RservInputPort.javaTypes.keySet()));

		inputTable = new JTable(new PortTableModel() {
			protected Port[] getPorts() {
				return processor.getInputPorts();
			}
		});
		inputTable.setIntercellSpacing(new Dimension(0, 0));
		inputTable.setShowVerticalLines(false);
		inputTable.setShowHorizontalLines(false);
		inputTable.getInputMap().put(
			(KeyStroke) deletePortAction.getValue(Action.ACCELERATOR_KEY),
			"DELETE_PORT");
		inputTable.getActionMap().put("DELETE_PORT", deletePortAction);
		inputTable.addMouseListener(tableMouseListener);
		inputTable.setPreferredSize(new Dimension(0, 0));
		inputTable.getTableHeader().setPreferredSize(new Dimension(0, 0));
		inputTable.setRowHeight(inputTable.getRowHeight() + 2);
		inputTable.getColumnModel().getColumn(0).setMinWidth(10);
		inputTable.getColumnModel().getColumn(1).setMinWidth(10);
		inputTable.setDefaultEditor(String.class, new DefaultCellEditor(
				inputTypesCombo));
		inputTable.setDefaultRenderer(Port.class,
			new DefaultTableCellRenderer() {
				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {
					setIcon(ScuflIcons.inputPortIcon);
					return super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
				}
			});

		JPanel inputTablePanel = new JPanel(new BorderLayout());
		inputTablePanel.add(inputTable, BorderLayout.CENTER);
		JScrollPane inputPane = new JScrollPane(inputTablePanel);
		inputPane.setPreferredSize(new Dimension(0, 0));

		ActionListener addInputAction = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Add a port to the input model!
				try {
					RservInputPort ip = new RservInputPort(processor,
							addInputField.getText());
					processor.addPort(ip);
					addInputField.setText("");
				} catch (PortCreationException pce) {
					// FIXME: Should warn about creation error
				} catch (DuplicatePortNameException dpne) {
					// FIXME: Should warn about duplicate port
				}
			}
		};

		addInputButton = new JButton("Add Input", ScuflIcons.inputPortIcon);
		addInputButton.addActionListener(addInputAction);
		addInputButton.setEnabled(false);

		addInputField = new JTextField();
		addInputField.addActionListener(addInputAction);
		addInputField.getDocument().addDocumentListener(new DocumentListener() {
			private void checkName() {
				addInputButton.setEnabled(isValidName(addInputField.getText()));
			}

			public void changedUpdate(DocumentEvent e) {
				checkName();
			}

			public void insertUpdate(DocumentEvent e) {
				checkName();
			}

			public void removeUpdate(DocumentEvent e) {
				checkName();
			}
		});

		JPanel inputFieldPanel = new JPanel(new BorderLayout());
		inputFieldPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		inputFieldPanel.add(addInputButton, BorderLayout.LINE_START);
		inputFieldPanel.add(addInputField, BorderLayout.CENTER);

		// Input edit panel
		JPanel inputEditPanel = new JPanel(new BorderLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(), "Inputs"));
		inputEditPanel.add(inputPane, BorderLayout.CENTER);
		inputEditPanel.add(inputFieldPanel, BorderLayout.SOUTH);
		portEditPanel.add(inputEditPanel);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Ports", portEditPanel);
		add(tabbedPane);

		setVisible(true);
	}

	public void attachToModel(ScuflModel theModel) {
		if (theModel != null) {
			theModel.addListener(this);
		}
	}

	public void detachFromModel() {
		//
	}

	public String getName() {
		if (processor == null) {
			return "Rserv config panel for unknown processor";
		} else {
			return "Configuring Rserv for " + processor.getName();
		}
	}

	public ImageIcon getIcon() {
		return ProcessorHelper.getPreferredIcon(processor);
	}

	public void receiveModelEvent(ScuflModelEvent event) {
		inputTable.tableChanged(new TableModelEvent(inputTable.getModel()));
	}

	boolean isValidName(String name) {
		if (name.matches("\\w+")) {
			Port[] ports = processor.getPorts();
			for (int index = 0; index < ports.length; index++) {
				if (ports[index].getName().equals(name)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
