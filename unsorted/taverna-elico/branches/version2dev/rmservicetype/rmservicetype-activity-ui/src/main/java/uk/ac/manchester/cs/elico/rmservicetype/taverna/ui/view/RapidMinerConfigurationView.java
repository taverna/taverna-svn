package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import org.apache.commons.lang.WordUtils;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.*;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config.ParameterTableModel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class RapidMinerConfigurationView extends JPanel {

	private RapidMinerActivityConfigurationBean oldConfiguration;
	private RapidMinerActivityConfigurationBean newConfiguration;
	
	private JPanel titlePanel;
    private JPanel contentPanel;
    private JPanel buttonPanel;

    private boolean firstCardShown;
	
	private CardLayout cardLayout = new CardLayout();
	
	private JLabel titleLabel, titleIcon;
		
	private DialogTextArea titleMessage;
	
	private JTableParameters parameterTable;

	private ParameterTableModel tableModel = null;
	
	private JButton nextButton, finishButton;
	
	private String[] fillValues = new String[] { "true", "false"};
	
	private HashMap<Integer, String[]> choicesMap = new HashMap<Integer,String[]>();
			
    private List<PortField> inputFields;

    private List<PortField> outputFields;

	private RapidAnalyticsRepositoryBrowser browser = null;
	
	
	public RapidMinerConfigurationView(RapidMinerExampleActivity activity) {

		ActivityIconManager.getInstance().resetIcon(activity);
		
	
		oldConfiguration = activity.getConfiguration();
		newConfiguration = oldConfiguration;
		initialise();
		layoutPanel();
		
	}

//	public void setInputLocationField(String loc) {
//		inputLocationField.setText(loc);
//	}


	private void initialise() {
		
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);
		
		// title
		titleLabel = new JLabel("Configure Operator : " + newConfiguration.getOperatorName());
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));

		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("You can explicitly set the input and output files for this activity or leave the fields blank");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);

        RapidMinerIOODescription desc = getRMIOODescription(newConfiguration);

        this.inputFields = getInputFields(desc.getInputPort());


//        JPanel outputPanel = new JPanel(new BorderLayout());
        this.outputFields = getOutputFields(desc.getOutputPort());

//        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Ports"));
//
//        inputOutputBox.add(inputPanel);
//        inputOutputBox.add(Box.createVerticalStrut(6));
//        inputOutputBox.add(outputPanel);


		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);	
		
		nextButton = new JButton("Next");
		nextButton.setFocusable(false);
		nextButton.setVisible(true);
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//System.out.println("[DEBUG] --> RETURNED REPOSITORY LOCATION " + browser.getChosenRepositoryPath());
				//check if the configuration has changed
//				if(!(inputLocationField.getText() == oldConfiguration.getInputLocation()) || (outputLocationField.getText() == oldConfiguration.getOutputLocation()) ) {
//
//					System.out.println("Configuration Changed");
//					newConfiguration.setInputLocation(inputLocationField.getText());
//					newConfiguration.setOutputLocation(outputLocationField.getText());
//
//				}
				
				if (firstCardShown) {	// move to next card
					
					nextButton.setText("Back");
					cardLayout.last(contentPanel);
					firstCardShown = false;
					finishButton.setEnabled(true);
					titleMessage.setText("Please choose which parameters you want to use and their corresponding configurations");
					
				} else {				// move to first card
					
					nextButton.setText("Next");
					cardLayout.first(contentPanel);
					firstCardShown =  true;
					finishButton.setEnabled(false);
					
				}
				
			}
		});
		
		finishButton = new JButton("Finish");
		finishButton.setFocusable(false);
		finishButton.setEnabled(false);
			
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);	
		
		// add table 
		
		// first find rows with combo boxes
		List<Integer> rowsWithCombobox = new ArrayList<Integer>();
		List<RapidMinerParameterDescription> ParameterDescriptions = oldConfiguration.getParameterDescriptions();

		Iterator parameterIterator = ParameterDescriptions.iterator();
		int i = 0;
		
		while (parameterIterator.hasNext()) {

            // get the parameter
            RapidMinerParameterDescription param = (RapidMinerParameterDescription) parameterIterator.next();
            if (param.getType() != null) {
                if (param.getType().equals("boolean") || param.getType().equals("choice")) {

                    rowsWithCombobox.add(i);
//                    System.out.println("BOOLEAN or CHOICE FOUND");

                    if (param.getType().equals("choice")) {

                        // it's a choice - add its choices to the hashmap
                        String [] theChoice;
                        List<String> tempChoices = param.getChoices();
                        theChoice = new String[tempChoices.size()];
                        Iterator choicesIterator = tempChoices.iterator();

                        int j = 0;

                        while (choicesIterator.hasNext()) {

                            theChoice[j] = (String) choicesIterator.next();
                            j++;

                        }

                        choicesMap.put(i, theChoice);

                    } else {

                        choicesMap.put(i, fillValues);

                    }

                }
			}

			i++;
			
		}	// rows found and set in rowsWithCombobox
		
		//ParameterTableModel tableModel = null;

		tableModel = new ParameterTableModel(ParameterDescriptions);

		parameterTable = new JTableParameters(tableModel);
		
		// add combo boxes to specific rows
		RowEditorModel rm = new RowEditorModel();
		parameterTable.setRowEditorModel(rm);

        for (Integer aRowsWithCombobox : rowsWithCombobox) {
            // add combo box to row column

            JComboBox cb = new JComboBox(choicesMap.get(aRowsWithCombobox));
            DefaultCellEditor ed = new DefaultCellEditor(cb);

            rm.addEditorForRow(aRowsWithCombobox, ed);

        }
		
		System.out.println(" HERE 3");

		parameterTable.setRowSelectionAllowed(false);
		parameterTable.getTableHeader().setReorderingAllowed(false);
		parameterTable.setGridColor(Color.LIGHT_GRAY);
		parameterTable.setSelectionBackground(Color.WHITE);
		parameterTable.setSelectionForeground(Color.WHITE);
		//parameterTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		parameterTable.setRowHeight(50);
		
		parameterTable.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(35);
		parameterTable.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(150);
		parameterTable.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(245);
		parameterTable.getTableHeader().getColumnModel().getColumn(3).setPreferredWidth(55);
		parameterTable.getTableHeader().getColumnModel().getColumn(4).setPreferredWidth(35);
		parameterTable.getTableHeader().getColumnModel().getColumn(5).setPreferredWidth(35);
		parameterTable.getTableHeader().getColumnModel().getColumn(6).setPreferredWidth(80);
		parameterTable.getTableHeader().getColumnModel().getColumn(7).setPreferredWidth(110);
		
		parameterTable.setEditingColumn(7);
		parameterTable.setEditingColumn(0);
		
		parameterTable.getColumnModel().getColumn(2).setCellRenderer(
		        new TextAreaRenderer());

		/*
		parameterTable.setColumnModel(new DefaultTableColumnModel() {
			public TableColumn getColumn(int columnIndex) {
				TableColumn column = super.getColumn(columnIndex);
				if (columnIndex == 0) {
					//column.setMaxWidth(100);
				}
				return column;
			}
		});
		 */
		
		firstCardShown = true;
	}


    private class PortField extends JPanel {

        public IOObjectPort getPort() {
            return port;
        }

        public String getFileLocation() {
            return location.getText();
        }

        IOObjectPort port;

//        final JLabel label;
        JTextField location;
        JButton upload;

        public PortField (IOObjectPort port) {
            this.port = port;

//            Box row = new Box(BoxLayout.X_AXIS);
//
//            String prettyLabel = port.getPortName();
//
//            label = new JLabel(WordUtils.capitalize(prettyLabel.replace("_", " ")));
//            Dimension d = label.getPreferredSize();
//            label.setPreferredSize(new Dimension(d.width+60,d.height));//<-----------
//            row.add(label);
//            row.add(Box.createHorizontalStrut(2));
//            row.add(location = new JTextField(20));
//            if (port.getFileLocation() != null) {
//                location.setText(port.getFileLocation());
//            }
//
//            row.add(Box.createHorizontalStrut(4));
//            upload = new JButton("Browse");
//            upload.addActionListener(new ActionListener() {
//                public void actionPerformed(ActionEvent arg0) {
//
//                    final JDialog frame = new JDialog((JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, RapidMinerConfigurationView.this), "Rapid Analytics Repository Browser");
//
//                    browser = new RapidAnalyticsRepositoryBrowser(){
//                        @Override
//                        public void fileSelectedButtonPress() {
//                            super.fileSelectedButtonPress();
//                            location.setText(browser.getChosenRepositoryPath());
//                            frame.dispose();
//                        }
//                    };
//
//                    browser.setOpaque(true);
//                    browser.initialiseTreeContents();
//
//
//                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//                    frame.setLocation(dim.width / 2 - getWidth() / 2, dim.height / 2 - getHeight() / 2);
//
//                    frame.setModal(true);
//                    frame.add(browser);
//                    frame.setPreferredSize(new Dimension(400,400));
//                    frame.pack();
//                    frame.setVisible(true);
//
//                }
//
//            });
//            row.add(upload);
//            row.add(Box.createHorizontalStrut(12));
//            this.add(row);

            JPanel pane = new JPanel();
            pane.setLayout(new GridBagLayout());

            FormUtility formUtility = new FormUtility();
            
            String prettyLabel = port.getPortName();

//            label = new JLabel();
//            Dimension d = label.getPreferredSize();
//            label.setPreferredSize(new Dimension(d.width+60,d.height));//<-----------

            formUtility.addLabel(WordUtils.capitalize(prettyLabel.replace("_", " ")), pane);
            formUtility.addMiddleField(location = new JTextField(20), pane);
            if (port.getFileLocation() != null) {
                location.setText(port.getFileLocation());
            }

            upload = new JButton("Browse");
            upload.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {

                    final JDialog frame = new JDialog((JDialog)SwingUtilities.getAncestorOfClass(JDialog.class, RapidMinerConfigurationView.this), "Rapid Analytics Repository Browser");

                    browser = new RapidAnalyticsRepositoryBrowser(){
                        @Override
                        public void fileSelectedButtonPress() {
                            super.fileSelectedButtonPress();
                            location.setText(browser.getChosenRepositoryPath());
                            frame.dispose();
                        }
                    };

                    browser.setOpaque(true);
                    browser.initialiseTreeContents();


                    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                    frame.setLocation(dim.width / 2 - getWidth() / 2, dim.height / 2 - getHeight() / 2);

                    frame.setModal(true);
                    frame.add(browser);
                    frame.setPreferredSize(new Dimension(400,400));
                    frame.pack();
                    frame.setVisible(true);

                }

            });

            formUtility.addLastField(upload, pane);
            this.add(pane);


        }
    }

  /**
 * Simple application for demonstrating the use of FormUtility
 * to hide the details of creating a form layout with
 * GridBagLayout.
 * <P>
 * Philip Isenhour - 060628 - http://javatechniques.com/
 */
// todo Jupp - need to do this properly, nicked this code for a quick and dirty solution...
    public class FormUtility {
    /**
     * Grid bag constraints for fields and labels
     */
    private GridBagConstraints lastConstraints = null;
    private GridBagConstraints middleConstraints = null;
    private GridBagConstraints labelConstraints = null;

    public FormUtility() {
        // Set up the constraints for the "last" field in each
        // row first, then copy and modify those constraints.

        // weightx is 1.0 for fields, 0.0 for labels
        // gridwidth is REMAINDER for fields, 1 for labels
        lastConstraints = new GridBagConstraints();

        // Stretch components horizontally (but not vertically)
        lastConstraints.fill = GridBagConstraints.HORIZONTAL;

        // Components that are too short or narrow for their space
        // Should be pinned to the northwest (upper left) corner
        lastConstraints.anchor = GridBagConstraints.NORTHWEST;

        // Give the "last" component as much space as possible
        lastConstraints.weightx = 1.0;

        // Give the "last" component the remainder of the row
        lastConstraints.gridwidth = GridBagConstraints.REMAINDER;

        // Add a little padding
        lastConstraints.insets = new Insets(1, 1, 1, 1);

        // Now for the "middle" field components
        middleConstraints =
             (GridBagConstraints) lastConstraints.clone();

        // These still get as much space as possible, but do
        // not close out a row
        middleConstraints.gridwidth = GridBagConstraints.RELATIVE;

        // And finally the "label" constrains, typically to be
        // used for the first component on each row
        labelConstraints =
            (GridBagConstraints) lastConstraints.clone();

        // Give these as little space as necessary
        labelConstraints.weightx = 0.0;
        labelConstraints.gridwidth = 1;
    }

    /**
     * Adds a field component. Any component may be used. The
     * component will be stretched to take the remainder of
     * the current row.
     */
    public void addLastField(Component c, Container parent) {
        GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, lastConstraints);
        parent.add(c);
    }

    /**
     * Adds an arbitrary label component, starting a new row
     * if appropriate. The width of the component will be set
     * to the minimum width of the widest component on the
     * form.
     */
    public void addLabel(Component c, Container parent) {
        GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, labelConstraints);
        parent.add(c);
    }

    /**
     * Adds a JLabel with the given string to the label column
     */
    public JLabel addLabel(String s, Container parent) {
        JLabel c = new JLabel(s);
        addLabel(c, parent);
        return c;
    }

    /**
     * Adds a "middle" field component. Any component may be
     * used. The component will be stretched to take all of
     * the space between the label and the "last" field. All
     * "middle" fields in the layout will be the same width.
     */
    public void addMiddleField(Component c, Container parent) {
        GridBagLayout gbl = (GridBagLayout) parent.getLayout();
        gbl.setConstraints(c, middleConstraints);
        parent.add(c);
    }

}

    private List<PortField> getInputFields(LinkedHashMap<String, IOInputPort> inputPort) {

        LinkedHashMap<String, IOInputPort> currentPorts =  newConfiguration.getInputPorts();

        List<PortField> ports = new ArrayList<PortField>();
        for (String key : inputPort.keySet()) {
            if (currentPorts.containsKey(key)) {
                System.err.println("looked up key " + key + " and intput loc is " + currentPorts.get(key).getFileLocation());
                ports.add(new PortField(currentPorts.get(key)));
            }
            else {
                ports.add(new PortField(inputPort.get(key)));
            }
        }
        return ports;
    }

    private List<PortField> getOutputFields(LinkedHashMap<String, IOOutputPort> outputPort) {

        LinkedHashMap<String, IOOutputPort> currentPorts =  newConfiguration.getOutputPorts();

        List<PortField> ports = new ArrayList<PortField>();
        for (String key : outputPort.keySet()) {
            if (currentPorts.containsKey(key)) {
                System.err.println("looked up key " + key + " and output loc is " + currentPorts.get(key).getFileLocation());
                ports.add(new PortField(currentPorts.get(key)));

            }
            else {
                ports.add(new PortField(outputPort.get(key)));
            }
        }
        return ports;
    }


    private void layoutPanel() {

		setPreferredSize(new Dimension(745, 400));
		setLayout(new BorderLayout());

        JPanel page1 = new JPanel(new BorderLayout());
        JPanel page2 = new JPanel(new GridBagLayout());
		
		contentPanel = new JPanel(cardLayout);
		contentPanel.add(page1, "page1");
		contentPanel.add(page2, "page2");
		add(contentPanel, BorderLayout.CENTER);
		
		// title
		// title
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0, 10)));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);

//		GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.weightx = 0.5;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.gridx = 0;
//		c.gridy = 0;

        Box inputOutputBox = new Box(BoxLayout.Y_AXIS);

        //
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Ports"));
        Box inputRows = new Box (BoxLayout.Y_AXIS);
        int x = 2;
        for (PortField p : inputFields) {
            inputRows.add(p);
            inputRows.add(Box.createVerticalStrut(x));
            x = x+2;
        }
        inputPanel.add(inputRows, BorderLayout.WEST);
        inputOutputBox.add(new JScrollPane(inputPanel));

        //
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Ports"));
        Box outputRows = new Box (BoxLayout.Y_AXIS);
        int y = 2;
        for (PortField p : outputFields) {
            outputRows.add(p);
            outputRows.add(Box.createVerticalStrut(y));
            y = y+2;
        }
        outputPanel.add(outputRows, BorderLayout.WEST);

        inputOutputBox.add(Box.createVerticalStrut(4));

        inputOutputBox.add(new JScrollPane(outputPanel));

        page1.add(inputOutputBox, BorderLayout.CENTER);

//		page1.add(explicitButton, c);
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.weightx = 0.5;
//		c.gridx = 1;
//		c.gridy = 0;
//
//		page1.add(implicitButton, c);
//
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.ipady = 20;      //make this component tall
//		c.weightx = 0.0;
//		c.gridwidth = 3;
//		c.gridx = 0;
//		c.gridy = 1;
//		page1.add(choiceDescription, c);
//
//		// input fields + label
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.ipady = 20;
//		c.gridwidth = 2;
//		c.gridx = 0;
//		c.gridy = 3;
//		page1.add(inputLocationLabel, c);
//
//		c.gridx = 1;
//		page1.add(inputLocationField, c);
//
//		c.gridx = 2;
//		page1.add(uploadButton, c);
//
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.ipady = 20;
//		c.gridwidth = 2;
//		c.gridx = 0;
//		c.gridy = 4;
//
//
//
//		page1.add(outputLocationLabel, c);
//
//		c.gridx = 1;
//
//		page1.add(outputLocationField, c);
		
		
		
		//buttons
		buttonPanel.add(nextButton);
		buttonPanel.add(finishButton);
		
		// page 2
				
		page2.setLayout(new BorderLayout());
		page2.add(new JScrollPane(parameterTable));
		
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public RapidMinerConfigurationView(LayoutManager arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public RapidMinerConfigurationView(boolean arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public RapidMinerConfigurationView(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Adds a light gray or etched border to the top or bottom of a JComponent.
	 * 
	 * @param component
     * @param position
     * @param etched
	 */
	protected void addDivider(JComponent component, final int position, final boolean etched) {
		component.setBorder(new Border() {
			private final Color borderColor = new Color(.6f, .6f, .6f);
			
			public Insets getBorderInsets(Component c) {
				if (position == SwingConstants.TOP) {
					return new Insets(5, 0, 0, 0);
				} else {
					return new Insets(0, 0, 5, 0);
				}
			}

			public boolean isBorderOpaque() {
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if (position == SwingConstants.TOP) {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y, x + width, y);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + 1, x + width, y + 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y, x + width, y);
					}
				} else {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y + height - 2, x + width, y + height - 2);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					}
				}
			}

		});
	}

    public RapidMinerIOODescription getRMIOODescription(RapidMinerActivityConfigurationBean config) {
        return new RapidMinerIOODescription(config.getCallName());
    }

//    class RadioButtonListener implements ActionListener, ChangeListener, ItemListener {
//		public void actionPerformed(ActionEvent e) {
//			//String factoryName = null;
//
//			System.out.print("ActionEvent received: ");
//			if (e.getActionCommand() == first) {
//				System.out.println(first + " pressed.");	// the user chose explicit
//
//				outputLocationLabel.setEnabled(true);
//				outputLocationField.setEnabled(true);
//
//				// set in config bean
//				newConfiguration.setIsExplicit(true);
//
//			} else {
//				System.out.println(second + " pressed.");	//	the user chose implicit
//
//				// gray out output field + label
//				outputLocationLabel.setEnabled(false);
//				outputLocationField.setEnabled(false);
//
//				// set in config bean
//				newConfiguration.setIsExplicit(false);
//			}
//		}
//
//		public void itemStateChanged(ItemEvent e) {
//			//System.out.println("ItemEvent received: "
//			//	+ e.getItem()
//			//	+ " is now "
//	  	 	//	+ ((e.getStateChange() == ItemEvent.SELECTED)?
//	  	 	//			"selected.":"unselected"));
//		}
//
//		public void stateChanged(ChangeEvent e) {
//			//System.out.println("ChangeEvent received from: "
//			//	+ e.getSource());
//
//		}
//	}

	public RapidMinerActivityConfigurationBean getConfiguration() {
		
		System.out.println("[DEBUG] the parameter count is " + tableModel.getColumnCount());

        LinkedHashMap<String, IOInputPort> inputs = new LinkedHashMap<String, IOInputPort>();
        for (PortField p : inputFields) {
            IOInputPort port = (IOInputPort) p.getPort();
            if (p.getFileLocation() != null) {
                port.setFileLocation(p.getFileLocation());
            }
            inputs.put(port.getPortName().replace(" ", "_"), port);

        }
        newConfiguration.setInputPorts(inputs);

        LinkedHashMap<String, IOOutputPort> outputs = new LinkedHashMap<String, IOOutputPort>();
        for (PortField p : outputFields) {
            IOOutputPort port = (IOOutputPort) p.getPort();
            if (p.getFileLocation() != null) {
                port.setFileLocation(p.getFileLocation());
            }
            outputs.put(port.getPortName().replace(" ", "_"), port);

        }
        newConfiguration.setOutputPorts(outputs);


		try {
			//System.out.println("[DEBUG] INSIDE TABLE MODEL " + tableModel.getUpdatedParameters().get(0).getExecutionValue());
			newConfiguration.setParameterDescriptions(tableModel.getUpdatedParameters());

            for (RapidMinerParameterDescription rapidMinerParameterDescription : newConfiguration.getParameterDescriptions()) {

                System.out.println(" new parameters to set " + rapidMinerParameterDescription.getUseParameter() + " " + rapidMinerParameterDescription.getExecutionValue());

            }



			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		newConfiguration.setIsParametersConfigured(true);
		tableModel = new ParameterTableModel(newConfiguration.getParameterDescriptions());
		return newConfiguration;
		
	}

	public void setOkAction(Action okAction) {
		
		finishButton.setAction(okAction);
		finishButton.setEnabled(false);
	}
	
	public class TextAreaRenderer extends JTextArea
    implements TableCellRenderer {

	public TextAreaRenderer() {
		
	    setLineWrap(true);
	    setWrapStyleWord(true);
	    setAutoscrolls(true);

	}

	public Component getTableCellRendererComponent(JTable jTable,
	      Object obj, boolean isSelected, boolean hasFocus, int row,
	      int column) {
		
		  setText((String)obj);
		  return this;
		  
	  }
	}



}
