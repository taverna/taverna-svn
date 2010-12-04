package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.taverna.t2.lang.ui.DialogTextArea;



import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;

public class RapidMinerConfigurationView extends JPanel {

	private RapidMinerActivityConfigurationBean oldConfiguration;
	private RapidMinerActivityConfigurationBean newConfiguration;

	
	private JPanel titlePanel, contentPanel, buttonPanel, page1, page2;
	
	private boolean firstCardShown;
	
	private CardLayout cardLayout = new CardLayout();
	
	private JLabel titleLabel, titleIcon, inputLocationLabel, outputLocationLabel;
		
	private DialogTextArea titleMessage, choiceDescription;
	
	private JTextField inputLocationField, outputLocationField;
	
	private JRadioButton implicitButton;
	private JRadioButton explicitButton;
	
	private JButton nextButton, finishButton;
	
	private String first = new String("Explicit");
	private String second = new String("Implicit");
	
	public RapidMinerConfigurationView(RapidMinerExampleActivity activity) {
		
		oldConfiguration = activity.getConfiguration();
		newConfiguration = oldConfiguration;
		initialise();
		layoutPanel();
	}

	private void initialise() {
		
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);
		
		// title
		titleLabel = new JLabel(" Operator : " + newConfiguration.getOperatorName());
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("Please chose whether you want to explicitly set the operator output location");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		
		// buttons
		explicitButton = new JRadioButton(first);
		explicitButton.setActionCommand(first);
		explicitButton.setSelected(oldConfiguration.getIsExplicit());
		
		implicitButton = new JRadioButton(second);
		implicitButton.setActionCommand(second);
		implicitButton.setSelected(!oldConfiguration.getIsExplicit());

		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(explicitButton);
		buttonGroup.add(implicitButton);
		
		RadioButtonListener radioListener = new RadioButtonListener();
		explicitButton.addActionListener(radioListener);
		explicitButton.addChangeListener(radioListener);
		explicitButton.addItemListener(radioListener);
		implicitButton.addActionListener(radioListener);
		implicitButton.addChangeListener(radioListener);
		implicitButton.addItemListener(radioListener);
		
		choiceDescription = new DialogTextArea("\n  Choosing Explicit will allow you to specify your own output location,\n    Implicit will automatically generate an outputlocation");;
		choiceDescription.setBackground(getBackground());
		
		// input fields
		inputLocationLabel = new JLabel("			Input Location");
		outputLocationLabel = new JLabel("			OutputLocation");
		
		inputLocationField = new JTextField();
		outputLocationField = new JTextField();
		
		inputLocationField.setText(oldConfiguration.getInputLocation());
		outputLocationField.setText(oldConfiguration.getOutputLocation());

		// buttons
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);	
		
		nextButton = new JButton("Next");
		nextButton.setFocusable(false);
		nextButton.setVisible(true);
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//check if the configuration has changed
				if(!(inputLocationField.getText() == oldConfiguration.getInputLocation()) || (outputLocationField.getText() == oldConfiguration.getOutputLocation()) ) {
					
					System.out.println("Configuration Changed");
					newConfiguration.setInputLocation(inputLocationField.getText());
					newConfiguration.setOutputLocation(outputLocationField.getText());

				}
				
				if (firstCardShown) {	// move to next card
					
					nextButton.setText("Back");
					cardLayout.last(contentPanel);
					firstCardShown = false;
					
				} else {				// move to first card
					
					nextButton.setText("Next");
;					cardLayout.first(contentPanel);
					firstCardShown =  true;
					
				}
				
			}
		});
		
		finishButton = new JButton("Finish");
		finishButton.setFocusable(false);
		finishButton.setEnabled(false);
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);	
		
		firstCardShown = true;
	}
	
	private void layoutPanel() {
		// TODO Auto-generated method stub
		setPreferredSize(new Dimension(450, 400));
		setLayout(new BorderLayout());
		
		page1 = new JPanel(new GridBagLayout());
		page2 = new JPanel(new GridBagLayout());
		
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

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		
		page1.add(explicitButton, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		
		page1.add(implicitButton, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 20;      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		page1.add(choiceDescription, c);
		
		// input fields + label
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 20;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 3;
		page1.add(inputLocationLabel, c);
		
		c.gridx = 1;
		page1.add(inputLocationField, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 20;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 4;
		
		page1.add(outputLocationLabel, c);
		
		c.gridx = 1;
		
		page1.add(outputLocationField, c);
		
		
		//buttons
		buttonPanel.add(nextButton);
		buttonPanel.add(finishButton);

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
	
	class RadioButtonListener implements ActionListener, ChangeListener, ItemListener {  
		public void actionPerformed(ActionEvent e) {
			//String factoryName = null;

			System.out.print("ActionEvent received: ");
			if (e.getActionCommand() == first) {
				System.out.println(first + " pressed.");	// the user chose explicit
				
				outputLocationLabel.setEnabled(true);
				outputLocationField.setEnabled(true);
				
				// set in config bean
				newConfiguration.setIsExplicit(true);

			} else {
				System.out.println(second + " pressed.");	//	the user chose implicit
				
				// gray out output field + label
				outputLocationLabel.setEnabled(false);
				outputLocationField.setEnabled(false);
				
				// set in config bean
				newConfiguration.setIsExplicit(false);
			}
		}

		public void itemStateChanged(ItemEvent e) {
			//System.out.println("ItemEvent received: " 
			//	+ e.getItem()
			//	+ " is now "
	  	 	//	+ ((e.getStateChange() == ItemEvent.SELECTED)?
	  	 	//			"selected.":"unselected"));
		}

		public void stateChanged(ChangeEvent e) {
			//System.out.println("ChangeEvent received from: "
			//	+ e.getSource());
			
		}
	}


}
