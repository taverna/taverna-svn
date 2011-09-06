package uk.ac.manchester.cs.elico.utilities.csvimporter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.ui.DialogTextArea;

import com.csvreader.CsvReader;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Rishi Ramgolam<br>
 * Date: Jul 13, 2011<br>
 * The University of Manchester<br>
 **/

public class CSVImporter extends JPanel
						implements ActionListener {
	
	private JPanel titlePanel;
	private JPanel contentPanel;
	private JPanel columnSeparatorPanel;
	
	private JLabel titleLabel;
	private JLabel titleIcon;
	
	private DialogTextArea titleMessage;

	private String COMMA_DELIMITER = "comma";
	private String SEMICOLON_DELIMITER = "semicolon";
	private String SPACE_DELIMITER = "space";
	private String TAB_DELIMITER = "tab";
	private char chosenDelimiter;
	
	private ButtonGroup group;
	private JPanel radioPanel;
	private JTable previewTable;
	
	private List<String> headerNames;
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton uploadButton;
	private String filePath;
	private JTextField commentSpecifier;
	
	private CsvImporterTableModel tableModel;
	private JLabel escapeCharacterLabel;
	private JTextField escapeCharacterSpecifier;
	private JCheckBox useQuotesCheckBox;
	private JTextField useQuotesTextField;
	private JCheckBox skipCommentsCheckBox;

	public CSVImporter(String fPath) {
		
		String[] temp = { "a","b","c","d","e","f","g","h","i","j"};
		tableModel = new CsvImporterTableModel(6, temp);
		previewTable = new JTable(tableModel);
		
		filePath = fPath;
		initialise();
		
		fetchTableData(fPath, ',');		// default is comma 
		setChosenDelimiter(',');
		
		layoutPanel();
		
		//tester();
		
	}
	
	private void initialise() {
		
		// title panel
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);
		
		// title
		titleLabel = new JLabel("CSV Importer");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("This wizard will allow you to preview and upload your data.");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		
		columnSeparatorPanel = new JPanel(new BorderLayout());
		columnSeparatorPanel.setBorder(BorderFactory.createTitledBorder("Column Separation"));
		
		// radio buttons to be placed within the panel
		JRadioButton commaButton = new JRadioButton("Comma");
		commaButton.setActionCommand(COMMA_DELIMITER);
		commaButton.setSelected(true);
		
		JRadioButton semicolonButton = new JRadioButton("Semi-colon");
		semicolonButton.setActionCommand(SEMICOLON_DELIMITER);

		JRadioButton spaceButton = new JRadioButton("Space");
		spaceButton.setActionCommand(SPACE_DELIMITER);
		
		JRadioButton tabButton = new JRadioButton("Tab");
		tabButton.setActionCommand(TAB_DELIMITER);
		
		skipCommentsCheckBox = new JCheckBox("Skip Comments");
		skipCommentsCheckBox.setSelected(true);
		
		commentSpecifier = new JTextField("#");
		
		escapeCharacterLabel = new JLabel("		Escape Character for Seperator");
						
		escapeCharacterSpecifier = new JTextField("\\");
		
		useQuotesCheckBox = new JCheckBox("Use Quotes");
		useQuotesCheckBox.setSelected(true);
		
		useQuotesTextField = new JTextField("\"");
		
		//Group the radio buttons.
	    group = new ButtonGroup();
	    group.add(commaButton);
	    group.add(semicolonButton);
	    group.add(spaceButton);
	    group.add(tabButton);
	  
	    //Register a listener for the radio buttons.
	    commaButton.addActionListener(this);
	    semicolonButton.addActionListener(this);
	    spaceButton.addActionListener(this);
	    tabButton.addActionListener(this);
	
	    //Add buttons to panel
	    radioPanel = new JPanel(new GridLayout(0, 2));
	    radioPanel.setPreferredSize(new Dimension(700, 160));
        radioPanel.add(commaButton);
        radioPanel.add(semicolonButton);
        radioPanel.add(spaceButton);
        radioPanel.add(tabButton);
        radioPanel.add(skipCommentsCheckBox);
	    radioPanel.add(commentSpecifier);
	    radioPanel.add(escapeCharacterLabel);
	    radioPanel.add(escapeCharacterSpecifier);
	    radioPanel.add(useQuotesCheckBox);
	    radioPanel.add(useQuotesTextField);
	    
        // table - temporary data structure
        String[] columnNames = {"First Name",
                "Last Name",
                "Sport",
                "# of Years",
                "Vegetarian"};
        
        Object[][] data = {
        	    {"Kathy", "Smith",
        	     "Snowboarding", new Integer(5), new Boolean(false)},
        	    {"John", "Doe",
        	     "Rowing", new Integer(3), new Boolean(true)},
        	    {"Sue", "Black",
        	     "Knitting", new Integer(2), new Boolean(false)},
        	    {"Jane", "White",
        	     "Speed reading", new Integer(20), new Boolean(true)},
        	    {"Joe", "Brown",
        	     "Pool", new Integer(10), new Boolean(false)}
        	};
        
        //previewTable = new JTable();
       		
		// bottom of GUI buttons
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);	
		
		cancelButton = new JButton("Cancel");
		cancelButton.setFocusable(false);
		cancelButton.setVisible(true);
		//	<< needs listener >>
		cancelButton = new JButton(new AbstractAction("Cancel") {

			public void actionPerformed(ActionEvent arg0) {

				closeImporter();
				
			}
						
		});
		
		
		uploadButton = new JButton("Upload");
		uploadButton =  new JButton(new AbstractAction("Upload") {

			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				getChosenFileDelimiter();
			}
			
		});
		uploadButton.setFocusable(false);
		uploadButton.setEnabled(true);
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);	
        
	}	
		
	private void layoutPanel() {
		
		// set this panels preferences
		setPreferredSize(new Dimension(745, 500));
		setLayout(new BorderLayout());
		
		previewTable.setPreferredSize(new Dimension(700, 100));
	        
		previewTable.getTableHeader().setPreferredSize(new Dimension(700, 25));
		
		contentPanel = new JPanel();
		add(contentPanel, BorderLayout.CENTER);
		
		// title panel
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0, 10)));
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);
		add(titlePanel, BorderLayout.NORTH);
								
		columnSeparatorPanel.add(radioPanel, BorderLayout.NORTH);
				
		contentPanel.add(columnSeparatorPanel);
		contentPanel.add(previewTable.getTableHeader());
		contentPanel.add(previewTable, BorderLayout.SOUTH);
		
		//buttons
		buttonPanel.add(cancelButton);
		buttonPanel.add(uploadButton);
		
		add(buttonPanel, BorderLayout.SOUTH);

	}
	
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
	
	public void getChosenFileDelimiter() {
		
		
		
	}
	
	public void fetchTableData(String filePath, char delimiter) {

		// 1. Read Headers - get the first 10
		CsvReader csvReader = null;
				
		try {
			
			csvReader = new CsvReader(filePath);
			
			// set parameters
			csvReader.setDelimiter(delimiter);
			csvReader.setComment(commentSpecifier.getText().charAt(0));
			
				if (escapeCharacterSpecifier.getText().equals("\\")) {
					
					csvReader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
					
				} else {
					
					csvReader.setEscapeMode(CsvReader.ESCAPE_MODE_DOUBLED);
	
				}
			
			csvReader.setTextQualifier(useQuotesTextField.getText().charAt(0));
			csvReader.setUseTextQualifier(useQuotesCheckBox.isSelected());
			csvReader.setSafetySwitch(false);		// lots of columns
			
			// [debug] System.out.println(" the delimited set is " + csvReader.getDelimiter());
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
				
		// Read its headers
		try {
			
			csvReader.readHeaders();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
		String[] allHeaders = null;
		String [] limitedHeaders = new String[10];
		int numberOfColumns = 0;
		try {
			
			allHeaders = csvReader.getHeaders();
			numberOfColumns = allHeaders.length;
			
			//[debug]System.out.println(" the number of headers returned are : " + allHeaders.length);
			int limiter = 10;		
			if (numberOfColumns < 10) {
				
				limiter = numberOfColumns;
				
			}
			for (int i = 0; i < limiter; i++) {
				
				limitedHeaders[i] = allHeaders[i];
				//[debug]System.out.println(" limited headers " + limitedHeaders[i]);
			}
			
			tableModel.setColumnHeaders(limitedHeaders);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
			
		//[debug]tableModel = new CsvImporterTableModel(6, limitedHeaders);
	
		// populate table model with values
		List<String> tableData = new ArrayList<String>();
		int iRow = 0;
		
		try {
						
			while (csvReader.readRecord() && iRow < 6) {	// while there are more records
				
				// for this record (row), get the first 10 column values
				tableData.add( csvReader.get( limitedHeaders[0] ) );
				tableData.add( csvReader.get( limitedHeaders[1] ) );
				tableData.add( csvReader.get( limitedHeaders[2] ) );
				tableData.add( csvReader.get( limitedHeaders[3] ) );
				tableData.add( csvReader.get( limitedHeaders[4] ) );
				tableData.add( csvReader.get( limitedHeaders[5] ) );
				tableData.add( csvReader.get( limitedHeaders[6] ) );
				tableData.add( csvReader.get( limitedHeaders[7] ) );
				tableData.add( csvReader.get( limitedHeaders[8] ) );
				tableData.add( csvReader.get( limitedHeaders[9] ) );
							
				for (int i = 0; i < 10; i++) {
					
					tableModel.setValueAt(tableData.get(i), iRow, i);
					
				}
				
				tableData.clear();
				iRow++;
			}
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
		//previewTable = new JTable(tableModel);
	
		tableModel.fireTableDataChanged();
		tableModel.fireTableStructureChanged();
		// 2. Read first six rows
	
	}
	
	// TESTER FUNCTION
	public static void tester() 
	{
		
		CsvReader csvReader = null;
		
		try {
			
			csvReader = new CsvReader("/Users/Rishi/Desktop/e-LICO_Development/geo-data.csv");
			
			System.out.println(" the delimited set is " + csvReader.getDelimiter());
			//csvReader.setDelimiter(',');
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}

		try {
			
			csvReader.readHeaders();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		System.out.println(" The number headers that have been read are -> " + csvReader.getHeaderCount());
		
		try {
			
			while (csvReader.readRecord()) {
				
				String productID = csvReader.get("assayID");
				System.out.println("	header :" + productID);

			}
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
		//tester();
		CSVImporter importer = new CSVImporter("/Users/rishi/Desktop/geo-data.csv");
		
		final JFrame frame = new JFrame();
		frame.add(importer);
		frame.pack();
		frame.setVisible(true);
		
	}

	public void actionPerformed(ActionEvent arg0) {

		// Radio Buttons
		String selectedButton = arg0.getActionCommand();
		//[debug]System.out.println(" Action Command Received : " + selectedButton);

		if (selectedButton.equals("comma")) {
		
			// change the delimiter to comma
			setChosenDelimiter(',');
			fetchTableData(filePath, ',');
			
		}
		
		if (selectedButton.equals("space")) {
			
			// change the delimiter to space
			setChosenDelimiter(' ');
			fetchTableData(filePath, ' ');
			
		}
		
		if (selectedButton.equals("tab")) {
			
			// change the delimiter to tab
			setChosenDelimiter('	');
			fetchTableData(filePath, '	');
			
		}
		
		if (selectedButton.equals("semicolon")) {
			
			// change the delimiter to semicolon
			setChosenDelimiter(';');
			fetchTableData(filePath, ';');
			
		}
	
	}

	public void setChosenDelimiter(char chosenDelimiter) {
		this.chosenDelimiter = chosenDelimiter;
	}

	public char getChosenDelimiter() {
		return chosenDelimiter;
	}
	
	public void closeImporter() {
		
		
	}

}
