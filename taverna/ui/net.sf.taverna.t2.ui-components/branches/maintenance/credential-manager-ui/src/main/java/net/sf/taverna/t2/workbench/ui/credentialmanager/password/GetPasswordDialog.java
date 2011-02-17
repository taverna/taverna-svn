/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.credentialmanager.password;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog for entering user's username and password.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class GetPasswordDialog extends NonBlockedHelpEnabledDialog {
	
	// Whether we should ask user to save their username and password using Credential Manager
	private boolean shouldAskUserToSave;

    // Username field 
    private JTextField usernameField;

    // Password field 
    private JPasswordField passwordField;
    
    // Whether user wished to save the username and password using Credential Manager
    private JCheckBox saveCheckBox;
   
    // The entered username
    private String username;
    
    // The entered password
    private String password;

    // Instructions to the user
	private String instructions;

    public GetPasswordDialog(String instructions, boolean shouldAskUserToSave)
    {
        super((Frame)null, "Enter username and password", true);
        this.instructions = instructions;
        this.shouldAskUserToSave = shouldAskUserToSave;
        initComponents();
    } 
    
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel instructionsLabel = new JLabel(instructions);
        instructionsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel jpInstructions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jpInstructions.add(instructionsLabel);
        
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
              
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                okPressed();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                cancelPressed();
            }
        });

        // Central panel with username/password fields and a "Do you want to Save?" checkbox
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel passwordPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        passwordPanel.add(usernameLabel);
        passwordPanel.add(usernameField);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        mainPanel.add(passwordPanel, BorderLayout.CENTER);
        
        // If user wants to save this username and password
        saveCheckBox = new JCheckBox();
        saveCheckBox.setBorder(new EmptyBorder(5,5,5,5));
        saveCheckBox.setSelected(true);
        saveCheckBox.setText("Use Credential Manager to save this username and password");  
        if (shouldAskUserToSave){
        	JPanel jpSaveCheckBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        	jpSaveCheckBox.add(saveCheckBox);
        	mainPanel.add(jpSaveCheckBox, BorderLayout.SOUTH);
        }
        
        passwordPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        passwordPanel.setMinimumSize(new Dimension(300,100));

        getContentPane().add(jpInstructions, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(okButton);

        pack();
    }
    
    public String getUsername()
    {
        return username;
    }
    
    public String getPassword()
    {
    	return password;
    }
    
    /**
     * Check if user wishes to save username and pasword
     * using the Credential Manager.
     */
    public boolean shouldSaveUsernameAndPassword(){
    	return saveCheckBox.isSelected();
    }

    private boolean checkControls()
    {    	
    	username = usernameField.getText();
    	if (username.length() == 0){
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty", 
                "Warning",
                JOptionPane.WARNING_MESSAGE);            
            return false;
    	}
    	   	
    	password = new String(passwordField.getPassword());
    	if (password.length() == 0) { // password empty
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty", 
                "Warning",
                JOptionPane.WARNING_MESSAGE);

            return false;        	
        }
   	
    	return true;
    }

    private void okPressed()
    {
        if (checkControls()) {
            closeDialog();
        }
    }

    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	username = null;
    	password = null;
        closeDialog();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }

	public void setUsername(String username) {
		this.username = username;
		usernameField.setText(username);
	}

	public void setPassword(String password) {
		this.password = password;
		passwordField.setText(password);
	}
}

