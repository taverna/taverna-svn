package org.embl.ebi.escience.scuflworkers.gt4;

/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Author: Wei Tan
 */

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;

/**
 * a dialog for helping create scavengers for GT4 registries that are not the default registry.
 *
 */
public class GT4ScavengerDialog extends JPanel {

	private static final long serialVersionUID = -57047613557546678L;
	private JTextField indexServiceURL= new JTextField("http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService");
	//private JTextField queryCriteria = new JTextField("");
	private JTextField queryValue = new JTextField("");
	private String[] queryStrings = { "None", "Search String", "Point Of Contact", "Service Name", "Operation Name", "Operation Input",
			"Operation Output","Operation Class", "Research Center","Concept Code",
			"Domain Model for Data Services"};

	//Create the combo box, select item at index 0.
	private JComboBox  queryList = new JComboBox(queryStrings);



    /**
     * Default constructor.
     *
     */
    public GT4ScavengerDialog() {
        super();
        GridLayout layout = new GridLayout(4, 2);
        setLayout(layout);
        add(new ShadedLabel("Location (URL) of the index service: ", ShadedLabel.TAVERNA_BLUE, true));
        indexServiceURL.setToolTipText("caGrid Services will be retrieved from the index service whose URL you specify here!");
        add(indexServiceURL);
        add(new ShadedLabel("Service Query Criteria: ", ShadedLabel.TAVERNA_BLUE, true));
        
        
        //queryCriteria.setToolTipText("Service Query will use the query criteria you specify here!");
        add(queryList);
        add(new ShadedLabel("Service Query Value: ", ShadedLabel.TAVERNA_BLUE, true));
        queryValue.setToolTipText("Service Query will use the query value you specify here!");
        add(queryValue);
        //add(Box.createHorizontalGlue());add(Box.createHorizontalGlue());
        setPreferredSize(this.getPreferredSize());
        setMinimumSize(this.getPreferredSize());
        setMaximumSize(this.getPreferredSize());
    }
    
    /**
     * 
     * @return the string representation of the IndexServiceURL
     */
    public String getIndexServiceURL() {
        return indexServiceURL.getText();
    }

    /**
     * 
     * @return the string representation of the QueryCriteria
     */
    public String getQueryCriteria() {
        return (String) queryList.getSelectedItem();
    }
    
    /**
     * 
     * @return the string representation of the QueryValue
     */
    public String getQueryValue() {
        return queryValue.getText();
    }
}


