/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import org.apache.bsf.ExtendedBSFManager;
import org.embl.ebi.escience.baclava.DataThing;
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
import org.syntax.jedit.TextAreaDefaults;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * A JPanel that can configure the beanshell processor type
 * 
 * Last edited by: $Author: phidias $
 * @author mfortner
 */
public class BSFConfigPanel extends JPanel implements ScuflUIComponent,
		ScuflModelEventListener
{
	private abstract class PortTableModel extends AbstractTableModel
	{
		protected abstract Port[] getPorts();

		public int getColumnCount()
		{
			return 3;
		}

		public int getRowCount()
		{
			return getPorts().length;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex > 0;
		}

		public Class getColumnClass(int columnIndex)
		{
			switch (columnIndex)
			{
			case 0:
				return Port.class;

			case 1:
				return Integer.class;

			case 2:
				return String.class;

			default:
				return null;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
			case 0:
				return getPorts()[rowIndex];

			case 1:
				return new Integer(getListDepth(getPorts()[rowIndex].getSyntacticType()));
				
			case 2:
				return getPrintableType(getPorts()[rowIndex].getSyntacticType());

			default:
				return null;
			}
		}

		/**
		 * @param syntacticType
		 * @return
		 */
		private int getListDepth(String syntacticType)
		{
			int index;
			int depth = 0;
			String temp = syntacticType;
			while ((index = temp.indexOf("l(")) > -1)
			{
				temp = temp.substring(2);
				depth++;
			}
			return depth;
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			if (columnIndex > 0)
			{
				int listCount = 0;
				String prettyMime = null;
				switch (columnIndex)
				{
				case 1:
					listCount = ((Integer) aValue).intValue();
					prettyMime = (String)getValueAt(rowIndex, 2);
					break;
					
				case 2:
					listCount = ((Integer)getValueAt(rowIndex, 1)).intValue();
					prettyMime = (String) aValue;
					break;
				}

				String mimeType = "bleh";
				Iterator entryIterator = DataThing.mimeTypes.entrySet().iterator();
				while(entryIterator.hasNext())
				{
					Map.Entry entry = (Map.Entry)entryIterator.next();
					if(entry.getValue().equals(prettyMime))
					{
						mimeType = (String)entry.getKey();
					}
				}
				
				getPorts()[rowIndex].setSyntacticType(getSyntacticType(listCount, mimeType));
			}
		}

		private String getSyntacticType(int listDepth, String mimeType)
		{
			if(listDepth == 0)
			{
				return "'" + mimeType + "'";
			}
			else
			{
				return "l(" + getSyntacticType(listDepth -1, mimeType) + ")";
			}
		}
		
		public String getColumnName(int columnIndex)
		{
			switch (columnIndex)
			{
			case 0:
				return "Name";

			case 1:
				return "List";

			case 2:
				return "Type";

			default:
				return "Eh? Shouldn't be here";
			}
		}

		private String getPrintableType(String syntacticType)
		{
			int start = syntacticType.indexOf('\'') + 1;
			int end = syntacticType.lastIndexOf('\'');

			String mimeType = syntacticType.substring(start, end);
			return (String) DataThing.mimeTypes.get(mimeType);
		}
	}

	private class ListDepthRenderer extends DefaultTableCellRenderer implements ChangeListener
	{
		public ListDepthRenderer()
		{
			super();
			setHorizontalAlignment(SwingConstants.RIGHT);
		}
		
		protected void setValue(Object value)
		{
			if(value instanceof Integer)
			{
				int depth = ((Integer)value).intValue();
				if (depth == 0)
				{
					setText("a");
				}
				else
				{
					String text = "a list of";
					for (int index = 1; index < depth; index++)
					{
						text = text + " lists of";
					}
					setText(text);
				}	
			}
			else
			{
				super.setValue(value);
			}
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		public void stateChanged(ChangeEvent e)
		{
			JSpinner mySpinner = (JSpinner)(e.getSource());
			setValue(mySpinner.getValue());
			mySpinner.revalidate();
			mySpinner.repaint();
		}
	}
	
	private class JSpinnerEditor extends AbstractCellEditor implements TableCellEditor
	{
		private JSpinner spinner = new JSpinner(new SpinnerNumberModel(0,0,10,1));
		private ListDepthRenderer renderer = new ListDepthRenderer();

		/**
		 * 
		 */
		public JSpinnerEditor()
		{
			super();
			spinner.setEditor(renderer);
			spinner.addChangeListener(renderer);			
		}		
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
		 *      java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column)
		{
			renderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
			spinner.setValue(value);			
			spinner.revalidate();
			spinner.repaint();			
			return spinner;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue()
		{
			return spinner.getValue();
		}

	}

	BSFProcessor processor = null;
	JEditTextArea scriptText;
	JTable inputTable;
	JTable outputTable;

	JButton addInputButton;
	JTextField addInputField;
	JButton addOutputButton;
	JTextField addOutputField;
	
	JComboBox scriptEngineSelect = new JComboBox();

	Action deletePortAction = new AbstractAction("Delete Port", ScuflIcons.deleteIcon)
	{
		public void actionPerformed(ActionEvent e)
		{
			JTable table = null;
			if (e.getSource() instanceof JMenuItem)
			{
				JMenuItem item = (JMenuItem) e.getSource();
				JPopupMenu menu = (JPopupMenu) item.getParent();
				Component component = menu.getInvoker();
				if (component instanceof JTable)
				{
					table = (JTable) component;
				}
			}
			else if (e.getSource() instanceof JTable)
			{
				table = (JTable) e.getSource();
			}

			if (table != null)
			{
				int[] rows = table.getSelectedRows();
				Port[] ports = new Port[rows.length];
				for (int index = 0; index < rows.length; index++)
				{
					ports[index] = (Port) table.getValueAt(rows[index], 0);
				}
				for (int index = 0; index < ports.length; index++)
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
	public BSFConfigPanel(BSFProcessor bp)
	{
		super(new BorderLayout());
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.processor = bp;
		
		scriptText = new JEditTextArea(new TextAreaDefaults());
		scriptText.setText(processor.getScript());
		scriptText.setTokenMarker(new JavaTokenMarker());
		scriptText.setCaretPosition(0);
		scriptText.setPreferredSize(new Dimension(0, 0));
		
		ExtendedBSFManager bsfMgr = new ExtendedBSFManager();
		Vector engineList = bsfMgr.getProcessorNameList();
		scriptEngineSelect.setModel(new DefaultComboBoxModel(engineList));

		JButton testScriptButton = new JButton("Test Script");
		testScriptButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					Interpreter interpreter = new Interpreter();
					interpreter.eval(scriptText.getText());
				}
				catch (EvalError e1)
				{
					e1.printStackTrace();
				}
			}
		});

		JButton scriptUpdateButton = new JButton("Save Script Changes", ScuflIcons.saveIcon);
		scriptUpdateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				processor.setScript(scriptText.getText());
			}
		});

		// create the button panel
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JLabel scriptLabel = new JLabel("Script Language");
		
		//buttonPanel.add(testScriptButton);
		buttonPanel.add(scriptLabel);
		buttonPanel.add(this.scriptEngineSelect);
		buttonPanel.add(scriptUpdateButton);

		JPanel scriptEditPanel = new JPanel(new BorderLayout());
		scriptEditPanel.add(scriptText, BorderLayout.CENTER);
		//this.add(buttonPanel, BorderLayout.SOUTH);
		scriptEditPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Panel to edit the input and output ports
		JPanel portEditPanel = new JPanel(new GridLayout(0, 2));

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
					JTable table = (JTable) me.getSource();
					int rowIndex = table.rowAtPoint(me.getPoint());
					table.setRowSelectionInterval(rowIndex, rowIndex);
					Port port = (Port) table.getValueAt(rowIndex, 0);

					if (port != null)
					{
						JPopupMenu menu = new JPopupMenu();
						menu.add(new JMenuItem(deletePortAction));
						menu.show(table, me.getX(), me.getY());
					}
				}
			}
		};

		deletePortAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0));

		JComboBox inputTypesCombo = new JComboBox(new Vector(DataThing.mimeTypes.values()));

		inputTable = new JTable(new PortTableModel()
		{
			protected Port[] getPorts()
			{
				return processor.getInputPorts();
			}
		});
		inputTable.setIntercellSpacing(new Dimension(0, 0));
		inputTable.setShowVerticalLines(false);
		inputTable.setShowHorizontalLines(false);
//		inputTable.setTableHeader(null);
		inputTable.getInputMap().put((KeyStroke) deletePortAction.getValue(Action.ACCELERATOR_KEY),
				"DELETE_PORT");
		inputTable.getActionMap().put("DELETE_PORT", deletePortAction);
		inputTable.addMouseListener(tableMouseListener);
		inputTable.setPreferredSize(new Dimension(0,0));
		inputTable.getTableHeader().setPreferredSize(new Dimension(0,0));
		inputTable.setRowHeight(inputTable.getRowHeight() + 2);
		inputTable.getColumnModel().getColumn(0).setMinWidth(10);
		inputTable.getColumnModel().getColumn(1).setMinWidth(10);
		inputTable.getColumnModel().getColumn(2).setMinWidth(10);		
		inputTable.setDefaultEditor(Integer.class, new JSpinnerEditor());
		inputTable.setDefaultRenderer(Integer.class, new ListDepthRenderer());
		inputTable.setDefaultEditor(String.class, new DefaultCellEditor(inputTypesCombo));
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
		inputFieldPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
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

		JComboBox outputTypesCombo = new JComboBox(new Vector(DataThing.mimeTypes.values()));		
		
		outputTable = new JTable(new PortTableModel()
		{
			protected Port[] getPorts()
			{
				return processor.getOutputPorts();
			}
		});
		outputTable.setIntercellSpacing(new Dimension(0, 0));
		outputTable.setShowVerticalLines(false);
		outputTable.setShowHorizontalLines(false);
		outputTable.addMouseListener(tableMouseListener);
		outputTable.getColumnModel().getColumn(0).setMinWidth(10);
		outputTable.getColumnModel().getColumn(1).setMinWidth(10);
		outputTable.getColumnModel().getColumn(2).setMinWidth(10);
		outputTable.setPreferredSize(new Dimension(0,0));
		outputTable.getInputMap().put(
				(KeyStroke) deletePortAction.getValue(Action.ACCELERATOR_KEY), "DELETE_PORT");
		outputTable.getActionMap().put("DELETE_PORT", deletePortAction);
		outputTable.setDefaultEditor(Integer.class, new JSpinnerEditor());
		outputTable.setDefaultRenderer(Integer.class, new ListDepthRenderer());
		outputTable.setDefaultEditor(String.class, new DefaultCellEditor(outputTypesCombo));
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
					addOutputField.setText("");
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
		outputFieldPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
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
		return "Configuring script for " + processor.getName();
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