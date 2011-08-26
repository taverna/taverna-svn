/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflui.workbench.*;
import org.embl.ebi.escience.scuflworkers.*;

/**
 * Helper for creating Biomart scavengers
 * @author Tom Oinn
 */
public class BiomartScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new Biomart instance...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    final JDialog dialog = new JDialog(s.getContainingFrame(),
						       "Configure Biomart Datasource",
						       true);
		    final MartSpecificationPanel msp = new MartSpecificationPanel();
		    dialog.getContentPane().add(msp);
		    JButton accept = new JButton("Okay");
		    JButton cancel = new JButton("Cancel");
		    msp.add(accept);
		    msp.add(cancel);
		    accept.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae2) {
				//
				if (dialog.isVisible()) {
				    try {
					BiomartScavenger bs = new BiomartScavenger(msp.getInfoBean());
					s.addScavenger(bs);
				    }
				    catch (Exception ex) {
					ex.printStackTrace();
				    }
				    finally {
					dialog.setVisible(false);
				    }
				}
			    }
			});
		    cancel.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae2) {
				if (dialog.isVisible()) {
				    dialog.setVisible(false);
				}
			    }
			});
		    dialog.getContentPane().add(msp);
		    dialog.pack();
		    dialog.setVisible(true);
		}		
	    };
	
	
    }
}


class MartSpecificationPanel extends JPanel {
    
    String[] standardDBChoices = new String[]{"mysql"};
    JComboBox dbType = new JComboBox(standardDBChoices);
    
    String[] standardDBDrivers = new String[]{"com.mysql.jdbc.Driver"};
    JComboBox dbDriver = new JComboBox(standardDBDrivers);
    
    String[] sampleInstances = new String[]{"ensembl_mart_22_1","msd_mart_2","uniprot_mart_9"};
    JComboBox dbInstance = new JComboBox(sampleInstances);
    
    JTextField dbHost = new JTextField("martdb.ebi.ac.uk");
    JTextField dbPort = new JTextField("3306");
    //JTextField dbInstance = new JTextField("ensembl_mart_22_1");
    JTextField dbUser = new JTextField("anonymous");
    JPasswordField dbPassword = new JPasswordField();
    JTextField dbSchema = new JTextField("...");
    
    public MartSpecificationPanel() {
	super();
	dbType.setEditable(true);
	dbDriver.setEditable(true);
	dbInstance.setEditable(true);
	GridLayout layout = new GridLayout(9,2);
	setLayout(layout);
	add(new ShadedLabel("Database Type", ShadedLabel.TAVERNA_GREEN, true));
	add(dbType);
	add(new ShadedLabel("Database Driver", ShadedLabel.TAVERNA_GREEN, true));
	add(dbDriver);
	add(new ShadedLabel("Host", ShadedLabel.TAVERNA_ORANGE, true));
	add(dbHost);
	add(new ShadedLabel("Port", ShadedLabel.TAVERNA_ORANGE, true));
	add(dbPort);
	add(new ShadedLabel("Database", ShadedLabel.TAVERNA_ORANGE, true));
	add(dbInstance);
	add(new ShadedLabel("User", ShadedLabel.TAVERNA_BLUE, true));
	add(dbUser);
	add(new ShadedLabel("Password", ShadedLabel.TAVERNA_BLUE, true));
	add(dbPassword);
	add(new ShadedLabel("Schema", ShadedLabel.TAVERNA_GREEN, true));
	add(dbSchema);
	setPreferredSize(new Dimension(400,200));
    }

    public String getDBType() {
	return (String)dbType.getSelectedItem();
    }
    
    public String getDBDriver() {
	return (String)dbDriver.getSelectedItem();
    }
    
    public String getDBHost() {
	return dbHost.getText();
    }

    public String getDBPort() {
	return dbPort.getText();
    }
    
    public String getDBInstance() {
	return (String)dbInstance.getSelectedItem();
    }

    public String getDBUser() {
	return dbUser.getText();
    }

    public String getDBPassword() {
	return new String(dbPassword.getPassword());
    }
    public String getDBSchema() {
	return dbSchema.getText();
    }
    
    public BiomartConfigBean getInfoBean() {
	return new BiomartConfigBean(getDBType(),
				     getDBDriver(),
				     getDBHost(),
				     getDBPort(),
				     getDBInstance(),
				     getDBUser(),
				     getDBPassword(),
				     getDBSchema());
    }

}
