/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

/**
 * A JPanel that can configure the beanshell processor type
 * 
 * @author Tom Oinn, Chris Greenhalgh, Kevin Glover
 */
public class BeanshellConfigPanel extends JPanel implements ScuflUIComponent,
		ScuflModelEventListener
{
	BeanshellProcessor processor = null;
	JEditTextArea scriptText;
	JTable inputTable;
	JTable outputTable;

	JButton addInputButton;
	JTextField addInputField;
	JButton addOutputButton;
	JTextField addOutputField;
	
	Action deletePortAction = new AbstractAction("Delete Port", ScuflIcons.deleteIcon)
	{
		public void actionPerformed(ActionEvent e)
		{
			JTable table = null;			
			if(e.getSource() instanceof JMenuItem)
			{
				JMenuItem item = (JMenuItem)e.getSource();
				JPopupMenu menu = (JPopupMenu)item.getParent();
				Component component = menu.getInvoker();
				if(component instanceof JTable)
				{
					table = (JTable)component;
				}
			}
			else if(e.getSource() instanceof JTable)
			{
				table = (JTable)e.getSource();
			}
			
			if(table != null)
			{
				int[] rows = table.getSelectedRows();
				Port[] ports = new Port[ rows.length ];
				for(int index = 0; index < rows.length; index++)
				{
					ports[index] = (Port)table.getValueAt(rows[index], 0);
				}
				for(int index = 0; index < ports.length; index++)
				{
					processor.removePort(ports[index]);
				}				
			}
		}
	};	

	/**
	 * Create a new beanshell configuration panel applying to the processor
	 * specified in the constructor
	 */
	public BeanshellConfigPanel(BeanshellProcessor bp)
	{
		super(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.processor = bp;

		scriptText = new JEditTextArea();
		scriptText.setText(processor.getScript());
		scriptText.setTokenMarker(new JavaTokenMarker());
		scriptText.setCaretPosition(0);
		scriptText.setPreferredSize(new Dimension(0, 0));

		JButton scriptUpdateButton = new JButton("Save Script Changes", ScuflIcons.saveIcon);
		scriptUpdateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				processor.setScript(scriptText.getText());
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(scriptUpdateButton);

		JPanel scriptEditPanel = new JPanel(new BorderLayout());
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);
		scriptEditPanel.add(buttonPanel, BorderLayout.PAGE_END);

		// Panel to edit the input and output ports
		JPanel portEditPanel = new JPanel(new GridLayout(0, 2));

		// ...and for the outputs
		final DefaultListModel outputModel = new DefaultListModel();

		MouseListener tableMouseListener = new MouseAdapter()
		{
			public void mousePressed(MouseEvent me)
			{
				popup(me);
			}

			public void mouseReleased(MouseEvent me)
			{
				popup(me);
			}

			protected void popup(MouseEvent me)
			{
				if (me.isPopupTrigger())
				{
					JTable table = (JTable)me.getSource();
					int rowIndex = table.rowAtPoint(me.getPoint());
					table.setRowSelectionInterval(rowIndex, rowIndex);
					Port port = (Port)table.getValueAt(rowIndex, 0);

					if (port != null)
					{
						JPopupMenu menu = new JPopupMenu();
						menu.add(new JMenuItem(deletePortAction));
						menu.show(table, me.getX(), me.getY());
					}
				}
			}
		};
		
		deletePortAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		
		inputTable = new JTable(new AbstractTableModel()
		{
			public int getColumnCount()
			{
				return 2;
			}

			public int getRowCount()
			{
				return processor.getInputPorts().length;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return columnIndex == 1;
			}

			public Class getColumnClass(int columnIndex)
			{
				if(columnIndex == 0)
				{
					return Port.class;
				}
				return String.class;
			}

			public Object getValueAt(int rowIndex, int columnIndex)
			{
				if (columnIndex == 0)
				{
					return processor.getInputPorts()[rowIndex];
				}
				else
				{
					return processor.getInputPorts()[rowIndex].getSyntacticType();
				}
			}

			public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
				if (columnIndex == 1)
				{
					processor.getInputPorts()[rowIndex].setSyntacticType((String) aValue);
				}
			}

			public String getColumnName(int columnIndex)
			{
				if (columnIndex == 0)
				{
					return "Name";
				}
				else
				{
					return "Type";
				}
			}
		});
		inputTable.setIntercellSpacing(new Dimension(0, 0));
		inputTable.setShowVerticalLines(false);
		inputTable.setShowHorizontalLines(false);
		inputTable.setTableHeader(null);
		inputTable.getInputMap().put((KeyStroke)deletePortAction.getValue(Action.ACCELERATOR_KEY), "DELETE_PORT");
		inputTable.getActionMap().put("DELETE_PORT", deletePortAction);
		inputTable.addMouseListener(tableMouseListener);
		inputTable.setDefaultRenderer(Port.class, new DefaultTableCellRenderer()
		{
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column)
			{
				setIcon(ScuflIcons.inputPortIcon);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
			}
		});

		JPanel inputTablePanel = new JPanel(new BorderLayout());
		inputTablePanel.add(inputTable, BorderLayout.CENTER);
		JScrollPane inputPane = new JScrollPane(inputTablePanel);
		inputPane.setPreferredSize(new Dimension(0, 0));

		ActionListener addInputAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Add a port to the input model!
				try
				{
					InputPort ip = new InputPort(processor, addInputField.getText());
					ip.setSyntacticType("'text/plain'");
					processor.addPort(ip);
					addInputField.setText("");
				}
				catch (PortCreationException pce)
				{
					//
				}
				catch (DuplicatePortNameException dpne)
				{
					//
				}
			}
		};

		addInputButton = new JButton("Add Input", ScuflIcons.inputPortIcon);
		addInputButton.addActionListener(addInputAction);
		addInputButton.setEnabled(false);

		addInputField = new JTextField();
		addInputField.addActionListener(addInputAction);
		addInputField.getDocument().addDocumentListener(new DocumentListener()
		{
			private void checkName()
			{
				addInputButton.setEnabled(isValidName(addInputField.getText()));
			}

			public void changedUpdate(DocumentEvent e)
			{
				checkName();
			}

			public void insertUpdate(DocumentEvent e)
			{
				checkName();
			}

			public void removeUpdate(DocumentEvent e)
			{
				checkName();
			}
		});

		JPanel inputFieldPanel = new JPanel(new BorderLayout());
		inputFieldPanel.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));		
		inputFieldPanel.add(addInputButton, BorderLayout.LINE_START);
		inputFieldPanel.add(addInputField, BorderLayout.CENTER);

		// Input edit panel
		JPanel inputEditPanel = new JPanel(new BorderLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Inputs"));
		inputEditPanel.add(inputPane, BorderLayout.CENTER);
		inputEditPanel.add(inputFieldPanel, BorderLayout.SOUTH);
		portEditPanel.add(inputEditPanel);

		// Output edit panel
		JPanel outputEditPanel = new JPanel(new BorderLayout());
		outputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Outputs"));

		outputTable = new JTable(new AbstractTableModel()
		{
			public int getColumnCount()
			{
				return 2;
			}

			public int getRowCount()
			{
				return processor.getOutputPorts().length;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return columnIndex == 1;
			}

			public Class getColumnClass(int columnIndex)
			{
				if(columnIndex == 0)
				{
					return Port.class;
				}
				return String.class;
			}

			public Object getValueAt(int rowIndex, int columnIndex)
			{
				if (columnIndex == 0)
				{
					return processor.getOutputPorts()[rowIndex];
				}
				else
				{
					return processor.getOutputPorts()[rowIndex].getSyntacticType();
				}
			}

			public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
				if (columnIndex == 1)
				{
					processor.getOutputPorts()[rowIndex].setSyntacticType((String) aValue);
				}
			}

			public String getColumnName(int columnIndex)
			{
				if (columnIndex == 0)
				{
					return "Name";
				}
				else
				{
					return "Type";
				}
			}
		});
		outputTable.setIntercellSpacing(new Dimension(0, 0));
		outputTable.setShowVerticalLines(false);
		outputTable.setShowHorizontalLines(false);
		outputTable.addMouseListener(tableMouseListener);		
		outputTable.getInputMap().put((KeyStroke)deletePortAction.getValue(Action.ACCELERATOR_KEY), "DELETE_PORT");
		outputTable.getActionMap().put("DELETE_PORT", deletePortAction);				
		outputTable.setTableHeader(null);
		outputTable.setDefaultRenderer(Port.class, new DefaultTableCellRenderer()
		{
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column)
			{
				setIcon(ScuflIcons.outputPortIcon);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
			}
		});

		JPanel outputTablePanel = new JPanel(new BorderLayout());
		outputTablePanel.add(outputTable, BorderLayout.CENTER);
		JScrollPane outputPane = new JScrollPane(outputTablePanel);
		inputPane.setPreferredSize(new Dimension(0, 0));

		outputEditPanel.add(outputPane, BorderLayout.CENTER);
		// Add a text button to create a new input

		ActionListener addOutputAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				// Add a port to the input model!
				try
				{
					OutputPort op = new OutputPort(processor, addOutputField.getText());
					op.setSyntacticType("'text/plain'");
					processor.addPort(op);
				}
				catch (PortCreationException pce)
				{
					//
				}
				catch (DuplicatePortNameException dpne)
				{
					//
				}
			}
		};

		addOutputField = new JTextField();
		addOutputField.addActionListener(addOutputAction);
		addOutputField.getDocument().addDocumentListener(new DocumentListener()
		{
			private void checkName()
			{
				addOutputButton.setEnabled(isValidName(addOutputField.getText()));
			}

			public void changedUpdate(DocumentEvent e)
			{
				checkName();
			}

			public void insertUpdate(DocumentEvent e)
			{
				checkName();
			}

			public void removeUpdate(DocumentEvent e)
			{
				checkName();
			}
		});

		addOutputButton = new JButton("Add Output", ScuflIcons.outputPortIcon);
		addOutputButton.addActionListener(addOutputAction);
		addOutputButton.setEnabled(false);

		JPanel outputFieldPanel = new JPanel(new BorderLayout());
		outputFieldPanel.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
		outputFieldPanel.add(addOutputButton, BorderLayout.LINE_START);
		outputFieldPanel.add(addOutputField, BorderLayout.CENTER);

		outputEditPanel.add(outputFieldPanel, BorderLayout.SOUTH);
		portEditPanel.add(outputEditPanel);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Script", scriptEditPanel);
		tabbedPane.addTab("Ports", portEditPanel);
		add(tabbedPane);
		setVisible(true);
	}

	protected class PortListMouseListener extends MouseAdapter
	{
		PortListMouseListener(JList list, DefaultListModel listModel, boolean inputFlag)
		{
			this.list = list;
			this.listModel = listModel;
			this.inputFlag = inputFlag;
			list.addMouseListener(this);
		}

		protected JList list;
		protected DefaultListModel listModel;
		protected boolean inputFlag;

		public void mousePressed(MouseEvent me)
		{
			popup(me);
		}

		public void mouseReleased(MouseEvent me)
		{
			popup(me);
		}

		protected void popup(MouseEvent me)
		{
			if (me.isPopupTrigger())
			{
				int index = list.locationToIndex(new Point(me.getX(), me.getY()));
				if (index < 0 || index >= list.getModel().getSize())
					return;
				list.setSelectedIndex(index);
				final Port p = (Port) list.getModel().getElementAt(index);
				if (p != null)
				{
					JPopupMenu menu = new JPopupMenu();
					menu.add(new JMenuItem(new AbstractAction("Remove port")
					{
						public void actionPerformed(ActionEvent ae)
						{
							processor.removePort(p);
						}
					}));
					menu.add(new JMenuItem(new AbstractAction("Edit syntactic type")
					{
						public void actionPerformed(ActionEvent ae)
						{
							// System.out.println("Edit syntactic type of
							// "+p.getName()+" ("+p.getSyntacticType()+")");
							final JTextField field = new JTextField(40);
							if (p.getSyntacticType() != null)
							{
								field.setText(p.getSyntacticType());
							}
							final JDialog dialog = new JDialog();
							dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
							dialog.setTitle("Edit syntactic type of " + p.getName());
							dialog.getContentPane().setLayout(new BorderLayout());
							dialog.getContentPane().add(field);
							field.addActionListener(new ActionListener()
							{
								public void actionPerformed(ActionEvent ae)
								{
									p.setSyntacticType(field.getText());
									dialog.dispose();
								}
							});
							dialog.pack();
							dialog.setVisible(true);
						}
					}));
					menu.show(list, me.getX(), me.getY());
				}
			}
		}
	}

	public void attachToModel(ScuflModel theModel)
	{
		theModel.addListener(this);
	}

	public void detachFromModel()
	{
		//
	}

	public String getName()
	{
		return "Configuring beanshell for " + processor.getName();
	}

	public ImageIcon getIcon()
	{
		return ProcessorHelper.getPreferredIcon(processor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scufl.ScuflModelEventListener#receiveModelEvent(org.embl.ebi.escience.scufl.ScuflModelEvent)
	 */
	public void receiveModelEvent(ScuflModelEvent event)
	{
		inputTable.tableChanged(new TableModelEvent(inputTable.getModel()));
		outputTable.tableChanged(new TableModelEvent(outputTable.getModel()));
	}

	boolean isValidName(String name)
	{
		if (name.matches("\\w+"))
		{
			Port[] ports = processor.getPorts();
			for (int index = 0; index < ports.length; index++)
			{
				if (ports[index].getName().equals(name))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
}