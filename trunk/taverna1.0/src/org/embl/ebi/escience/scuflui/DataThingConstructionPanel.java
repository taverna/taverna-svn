/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright 2004 University of Nottingham
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.scufl.enactor.implementation.*;
import org.embl.ebi.escience.scuflui.workbench.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * COMMENT DataThingConstructionPanel
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1 $
 */
public class DataThingConstructionPanel extends JPanel implements ScuflUIComponent, ScuflModelEventListener
{
	private interface PanelTreeNode
	{
		public JComponent getPanel();

		public void fillMenu(JPopupMenu menu);
	}

	private class PortRootNode extends DefaultMutableTreeNode implements PanelTreeNode
	{
		private JComponent panel;
		private JTextArea editor;

		private ActionListener loadAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int returnVal = fileChooser.showOpenDialog(DataThingConstructionPanel.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						File file = fileChooser.getSelectedFile();
						Document inputDoc = new SAXBuilder(false).build(new FileReader(file));
						Map inputMap = DataThingXMLFactory.parseDataDocument(inputDoc);

						for (int index = 0; index < rootNode.getChildCount(); index++)
						{
							PortTreeNode portNode = (PortTreeNode) rootNode.getChildAt(index);
							DataThing thing = (DataThing) inputMap.get(portNode.toString());
							if (thing != null)
							{
								portNode.removeAllChildren();
								if (thing.getDataObject() instanceof Collection)
								{
									Iterator iterator = thing.childIterator();
									while (iterator.hasNext())
									{
										DataThing childThing = (DataThing) iterator.next();
										portNode.addInput(childThing.getDataObject());
									}
								}
								else
								{
									portNode.addInput(thing.getDataObject());
								}
							}
						}
						getPanel();
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null, "Problem opening content from web : \n"
							+ ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		private ActionListener importAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

			}
		};

		private ActionListener saveAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int returnVal = fileChooser.showSaveDialog(panel);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						File file = fileChooser.getSelectedFile();
						//FileFilter fileFilter =
						// fileChooser.getFileFilter();

						FileWriter fileWriter = new FileWriter(file);
						BufferedWriter writer = new BufferedWriter(fileWriter);
						getPanel();
						BufferedReader reader = new BufferedReader(new StringReader(editor
								.getText()));
						String line = null;
						while ((line = reader.readLine()) != null)
						{
							writer.write(line);
							writer.newLine();
						}
						writer.flush();
						fileWriter.flush();
					}
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null, "Problem opening content from web : \n"
							+ ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		/*
		 * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
		 */
		public JComponent getPanel()
		{
			if (panel == null)
			{
				panel = new JPanel(new BorderLayout());
				
				editor = new JTextArea();
				editor.setEditable(false);
				editor.setPreferredSize(new Dimension(100, 100));
				editor.setToolTipText("Input Document");

				JScrollPane scrollPane = new JScrollPane(editor);
				JPanel buttonPanel = new JPanel();

				JButton loadInputDocButton = new JButton(ScuflIcons.openIcon);
				JButton importButton = new JButton(new ImageIcon(ClassLoader
						.getSystemResource("org/embl/ebi/escience/scuflui/workbench/import.gif")));
				JButton saveInputDocButton = new JButton(new ImageIcon(ClassLoader
						.getSystemResource("org/embl/ebi/escience/scuflui/workbench/save.gif")));

				loadInputDocButton.setToolTipText("Load Input Document");
				loadInputDocButton.setPreferredSize(new Dimension(32, 32));
				loadInputDocButton.addActionListener(loadAction);

				importButton.setToolTipText("Import Input Values");
				importButton.setPreferredSize(new Dimension(32, 32));
				importButton.addActionListener(importAction);

				saveInputDocButton.setToolTipText("Save Input Document");
				saveInputDocButton.setPreferredSize(new Dimension(32, 32));
				saveInputDocButton.addActionListener(saveAction);

				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
				buttonPanel.add(loadInputDocButton);
				//buttonPanel.add(importButton);
				buttonPanel.add(saveInputDocButton);

				panel.add(scrollPane, BorderLayout.CENTER);
				panel.add(buttonPanel, BorderLayout.EAST);
			}
			XMLOutputter outputter = new XMLOutputter("   ", true);
			editor.setText(outputter.outputString(DataThingXMLFactory
					.getDataDocument(bakeInputMap())));
			return panel;
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
		 */
		public void fillMenu(JPopupMenu menu)
		{
			JMenuItem loadItem = new JMenuItem("Load Input Document", ScuflIcons.openIcon);
			loadItem.addActionListener(loadAction);

			JMenuItem importItem = new JMenuItem("Import Input Values");
			importItem.addActionListener(importAction);

			JMenuItem saveItem = new JMenuItem("Save Input Document", new ImageIcon(ClassLoader
					.getSystemResource("org/embl/ebi/escience/scuflui/workbench/save.gif")));
			saveItem.addActionListener(saveAction);

			menu.add(loadItem);
			//menu.add(importItem);
			menu.add(saveItem);
		}

		public String toString()
		{
			return "Input Document";
		}
	}

	private class PortTreeNode extends DefaultMutableTreeNode implements PanelTreeNode
	{
		private Port port;
		private JPanel portPanel;

		private ActionListener createAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				InputTreeNode newNode = addInput(new String("Enter input value here"));
				portTree.setSelectionPath(new TreePath(newNode.getPath()));
			}
		};

		public PortTreeNode(Port port)
		{
			super(port);
			this.port = port;
		}

		public Port getPort()
		{
			return port;
		}

		public DataThing getDataThing()
		{
			ArrayList inputList = new ArrayList();
			for (int index = 0; index < getChildCount(); index++)
			{
				InputTreeNode inputNode = (InputTreeNode) getChildAt(index);
				inputList.add(inputNode.getUserObject());
			}
			if (inputList.size() != 1)
			{
				return DataThingFactory.bake(inputList);
			}
			else
			{
				return DataThingFactory.bake(inputList.get(0));
			}
		}

		public InputTreeNode addInput(Object inputValue)
		{
			InputTreeNode inputNode = new InputTreeNode(inputValue, port.getSyntacticType());
			add(inputNode);
			treeModel.nodeStructureChanged(this);
			return inputNode;
		}

		public void removeInput(InputTreeNode input)
		{
			remove(input);
			treeModel.nodeStructureChanged(this);
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
		 */
		public JComponent getPanel()
		{
			if (portPanel == null)
			{
				portPanel = new JPanel(new BorderLayout(3, 3));
				portPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
				
				JPanel buttonPanel = new JPanel();
				JLabel description = new JLabel();
				JPanel descriptionPanel = new JPanel();

				JButton addButton = new JButton("Create New Input Value");
				JButton addFileListButton = new JButton("Load List of Inputs", ScuflIcons.openIcon);

				addButton.setAlignmentX(CENTER_ALIGNMENT);
				addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton
						.getPreferredSize().height));
				addButton.addActionListener(createAction);

				addFileListButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addFileListButton
						.getPreferredSize().height));
				addFileListButton.setAlignmentX(CENTER_ALIGNMENT);
				addFileListButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							int returnVal = fileChooser.showOpenDialog(DataThingConstructionPanel.this);
							if (returnVal == JFileChooser.APPROVE_OPTION)
							{
								File file = fileChooser.getSelectedFile();
								//FileFilter fileFilter =
								// fileChooser.getFileFilter();

								BufferedReader reader = new BufferedReader(new FileReader(file));
								String s = null;
								while ((s = reader.readLine()) != null)
								{
									addInput(s);
								}
							}
						}
						catch (Exception ex)
						{
							JOptionPane.showMessageDialog(null,
									"Problem opening content from web : \n" + ex.getMessage(),
									"Exception!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});

				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
				buttonPanel.add(addButton);
				buttonPanel.add(Box.createVerticalStrut(3));
				buttonPanel.add(addFileListButton);

				description.setFont(portTree.getFont());
				description.setText("<html><b>" + port.getName() + "</b><br>"
						+ port.getSyntacticType() + "<br>" + port.getMetadata().getDescription()
						+ "<br>" + port.getMetadata().getSemanticType() + "</html>");

				descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
				descriptionPanel.add(description);

				portPanel.add(descriptionPanel, BorderLayout.CENTER);
				portPanel.add(buttonPanel, BorderLayout.SOUTH);
			}
			return portPanel;
		}

		/*
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return port.toString();
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
		 */
		public void fillMenu(JPopupMenu menu)
		{
			// TODO Add implementation to PortTreeNode.fillMenu
			JMenuItem createItem = new JMenuItem("Create New Input Value");
			createItem.addActionListener(createAction);

			menu.add(createItem);
		}
	}

	private class InputTreeNode extends DefaultMutableTreeNode implements PanelTreeNode
	{
		private JComponent panel;
		private JTextArea editor;
		private String syntacticType;

		private ActionListener loadURLAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String name = (String) JOptionPane.showInputDialog(null,
							"URL to open?", "URL Required", JOptionPane.QUESTION_MESSAGE,
							null, null, "http://");
					if (name != null)
					{
						InputStream is = new URL(name).openStream();
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(is));
						StringBuffer sb = new StringBuffer();
						String s = null;
						while ((s = reader.readLine()) != null)
						{
							sb.append(s);
							sb.append("\n");
						}
						editor.setText(sb.toString());
					}
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null,
							"Problem opening content from web : \n" + ex.getMessage(),
							"Exception!", JOptionPane.ERROR_MESSAGE);
				}

			}
		};
		
		private ActionListener loadFileAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int returnVal = fileChooser.showOpenDialog(DataThingConstructionPanel.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						File file = fileChooser.getSelectedFile();
						//FileFilter fileFilter =
						// fileChooser.getFileFilter();

						BufferedReader reader = new BufferedReader(new FileReader(file));
						StringBuffer sb = new StringBuffer();
						String s = null;
						while ((s = reader.readLine()) != null)
						{
							sb.append(s);
							sb.append("\n");
						}
						editor.setText(sb.toString());
					}
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null,
							"Problem opening content from web : \n" + ex.getMessage(),
							"Exception!", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		
		private ActionListener removeAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				PortTreeNode parent = (PortTreeNode) InputTreeNode.this.getParent();
				portTree.setSelectionPath(new TreePath(parent.getPath()));
				parent.removeInput(InputTreeNode.this);
			}
		};

		public InputTreeNode(Object inputValue, String syntacticType)
		{
			super(inputValue);
			this.syntacticType = syntacticType;
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#getPanel()
		 */
		public JComponent getPanel()
		{
			if (panel == null)
			{
				editor = new JTextArea();
				editor.setText((String) getUserObject());
				editor.getDocument().addDocumentListener(new DocumentListener()
				{
					public void insertUpdate(DocumentEvent e)
					{
						setUserObject(editor.getText());
						treeModel.nodeChanged(InputTreeNode.this);
					}

					public void removeUpdate(DocumentEvent e)
					{
						setUserObject(editor.getText());
						treeModel.nodeChanged(InputTreeNode.this);
					}

					public void changedUpdate(DocumentEvent e)
					{
						setUserObject(editor.getText());
						treeModel.nodeChanged(InputTreeNode.this);
					}
				});
				JPanel buttonPanel = new JPanel();

				JButton removeButton = new JButton(ScuflIcons.deleteIcon);
				JButton loadFileButton = new JButton(ScuflIcons.openIcon);
				JButton loadURLButton = new JButton(ScuflIcons.webIcon);

				removeButton.setToolTipText("Remove this Input");
				removeButton.setPreferredSize(new Dimension(32, 32));
				removeButton.addActionListener(removeAction);

				loadFileButton.setToolTipText("Load from File");
				loadFileButton.setPreferredSize(new Dimension(32, 32));
				loadFileButton.addActionListener(loadFileAction);

				loadURLButton.setToolTipText("Load from URL");
				loadURLButton.setPreferredSize(new Dimension(32, 32));
				loadURLButton.addActionListener(loadURLAction);

				buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
				buttonPanel.add(removeButton);
				buttonPanel.add(loadFileButton);
				buttonPanel.add(loadURLButton);

				panel = new JPanel(new BorderLayout());
				panel.add(new JScrollPane(editor), BorderLayout.CENTER);
				panel.add(buttonPanel, BorderLayout.EAST);
			}
			return panel;
		}

		/*
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return getUserObject().toString();
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.DataThingConstructionPanel.PanelTreeNode#fillMenu(javax.swing.JPopupMenu)
		 */
		public void fillMenu(JPopupMenu menu)
		{
			JMenuItem removeItem = new JMenuItem("Remove Input Value", ScuflIcons.deleteIcon);
			removeItem.addActionListener(removeAction);

			JMenuItem loadFileItem = new JMenuItem("Load Input Value from File", ScuflIcons.openIcon);
			loadFileItem.addActionListener(loadFileAction);

			JMenuItem loadURLItem = new JMenuItem("Load Input Value from URL", ScuflIcons.openIcon);
			loadFileItem.addActionListener(loadURLAction);
			
			menu.add(removeItem);
			menu.add(loadFileItem);
			menu.add(loadURLItem);
		}
	}

	private class InputRenderer extends DefaultTreeCellRenderer
	{
		/*
		 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
		 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
				boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (value instanceof PortTreeNode)
			{
				setIcon(ScuflIcons.inputIcon);
			}
			if (value instanceof InputTreeNode)
			{
				setIcon(new ImageIcon(ClassLoader
						.getSystemResource("org/embl/ebi/escience/baclava/icons/text.png")));
				Object userObject = ((InputTreeNode) value).getUserObject();
				if (userObject instanceof String)
				{
					String summaryText = (String) userObject;
					if (summaryText.length() > 25)
					{
						summaryText = "<em>Click to edit...</em>";
					}
					setText("<html><font color=\"#666666\">"
							+ ((InputTreeNode) value).syntacticType + "</font><br>" + summaryText
							+ "</html>");
				}
			}
			return this;
		}
	}

	private static EnactorProxy defaultEnactor = new FreefluoEnactorProxy();
	private static JFileChooser fileChooser = new JFileChooser();

	private ScuflModel model = null;
	private PortRootNode rootNode = new PortRootNode();
	private DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

	private JSplitPane splitter;
	private JTree portTree;

	/**
	 * COMMENT Constructs a new <code>DataThingConstructionPanel</code>.
	 */
	public DataThingConstructionPanel()
	{
		super(new BorderLayout());

		portTree = new JTree(treeModel);
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton runButton = new JButton("Run Workflow", ScuflIcons.runIcon);

		runButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				Map inputObject = bakeInputMap();
				try
				{
					if (Workbench.workbench != null)
					{
						GenericUIComponentFrame thing = new GenericUIComponentFrame(
								Workbench.workbench.model, new EnactorInvocation(defaultEnactor,
										model, inputObject));
						thing.setSize(600, 400);
						thing.setLocation(100, 100);
						Workbench.workbench.desktop.add(thing);
						thing.moveToFront();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		portTree.setCellRenderer(new InputRenderer());
		portTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		portTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			public void valueChanged(TreeSelectionEvent event)
			{
				if (event.getPath().getLastPathComponent() instanceof PanelTreeNode)
				{
					PanelTreeNode node = (PanelTreeNode) event.getPath().getLastPathComponent();
					splitter.setRightComponent(node.getPanel());
				}
				else
				{
					if (splitter.getRightComponent() != null)
					{
						splitter.remove(splitter.getRightComponent());
					}
				}
				splitter.validate();
			}
		});

		portTree.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				showPopup(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				showPopup(e);
			}

			private void showPopup(MouseEvent event)
			{
				if (event.isPopupTrigger())
				{
					try
					{
						PanelTreeNode node = (PanelTreeNode) portTree.getPathForLocation(
								event.getX(), event.getY()).getLastPathComponent();
						JPopupMenu popup = new JPopupMenu();
						node.fillMenu(popup);
						popup.show(event.getComponent(), event.getX(), event.getY());
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		buttonPanel.add(runButton);

		splitter.setContinuousLayout(true);
		splitter.setLeftComponent(portTree);

		//add(portTree, BorderLayout.WEST);
		add(splitter, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		setVisible(true);
	}

	public Map bakeInputMap()
	{
		HashMap inputMap = new HashMap();
		Enumeration children = rootNode.children();
		while (children.hasMoreElements())
		{
			PortTreeNode portNode = (PortTreeNode) children.nextElement();
			inputMap.put(portNode.getPort().getName(), portNode.getDataThing());
		}
		return inputMap;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
	public void attachToModel(ScuflModel model)
	{
		if (this.model == null)
		{
			this.model = model;
			model.addListener(this);
			updateModel();
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
	 */
	public void detachFromModel()
	{
		if (this.model != null)
		{
			model.removeListener(this);
			this.model = null;
			rootNode.removeAllChildren();
			splitter.remove(splitter.getRightComponent());
		}
	}

	public void loadInputDocument()
	{
		try
		{
			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fileChooser.getSelectedFile();
				//FileFilter fileFilter = fileChooser.getFileFilter();

				BufferedReader reader = new BufferedReader(new FileReader(file));
				StringBuffer sb = new StringBuffer();
				String s = null;
				while ((s = reader.readLine()) != null)
				{
					sb.append(s);
					sb.append("\n");
				}
			}
		}
		catch (Exception e)
		{
			// TODO Handle HeadlessException
			e.printStackTrace();
		}
	}

	/**
	 * COMMENT Method DataThingConstructionPanel.updateModel
	 */
	private void updateModel()
	{
		Port[] inputs = model.getWorkflowSourcePorts();
		for (int index = 0; index < inputs.length; index++)
		{
			PortTreeNode portNode = null;
			for (int nodeIndex = index; nodeIndex < rootNode.getChildCount(); nodeIndex++)
			{
				PortTreeNode tempNode = (PortTreeNode) rootNode.getChildAt(nodeIndex);
				if (inputs[index].equals(tempNode.getPort()))
				{
					rootNode.remove(nodeIndex);
					portNode = tempNode;
				}
			}
			if (portNode == null)
			{
				portNode = new PortTreeNode(inputs[index]);
			}
			rootNode.insert(portNode, index);
		}
		while (rootNode.getChildCount() > inputs.length)
		{
			rootNode.remove(inputs.length);
		}
		treeModel.nodeStructureChanged(rootNode);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getName()
	 */
	public String getName()
	{
		return "Enactor Launch";
	}

	/*
	 * @see org.embl.ebi.escience.scufl.ScuflModelEventListener#receiveModelEvent(org.embl.ebi.escience.scufl.ScuflModelEvent)
	 */
	public void receiveModelEvent(ScuflModelEvent event)
	{
		updateModel();
	}
}