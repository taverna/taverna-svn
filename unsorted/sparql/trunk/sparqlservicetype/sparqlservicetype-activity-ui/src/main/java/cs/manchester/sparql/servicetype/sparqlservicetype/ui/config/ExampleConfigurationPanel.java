package cs.manchester.sparql.servicetype.sparqlservicetype.ui.config;

import java.awt.GridLayout;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationPanel;

import cs.manchester.sparql.servicetype.sparqlservicetype.ExampleActivity;
import cs.manchester.sparql.servicetype.sparqlservicetype.ExampleActivityConfigurationBean;
import cs.manchester.sparql.servicetype.sparqlservicetype.QueryVariable;

@SuppressWarnings("serial")
public class ExampleConfigurationPanel
		extends
		ActivityConfigurationPanel<ExampleActivity, 
        ExampleActivityConfigurationBean> {

	private ExampleActivity activity;
	private ExampleActivityConfigurationBean configBean;
	
	private JTextField fieldSparqlLocation;
	private JTextField fieldSparqlQuery;

	public ExampleConfigurationPanel(ExampleActivity activity) {
		this.activity = activity;
		initGui();
	}

	protected void initGui() {
		removeAll();
		setLayout(new GridLayout(0, 2));

		// FIXME: Create GUI depending on activity configuration bean
		
		// location
		JLabel labelSparqlLocation = new JLabel("Sparql Service Location : ");
		add(labelSparqlLocation);
		fieldSparqlLocation = new JTextField(25);
		add(fieldSparqlLocation);
		labelSparqlLocation.setLabelFor(fieldSparqlLocation);
		
		JLabel labelSparqlQuery = new JLabel("Query : ");
		add(labelSparqlQuery);
		fieldSparqlQuery = new JTextField(25);
		fieldSparqlQuery.setSize(50, 300);
		add(fieldSparqlQuery);
		labelSparqlQuery.setLabelFor(fieldSparqlQuery);

		// Populate fields from activity configuration bean
		refreshConfiguration();
	}

	/**
	 * Check that user values in UI are valid
	 */
	@Override
	public boolean checkValues() {
		/*
		try {
			URI.create(fieldURI.getText());
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getCause().getMessage(),
					"Invalid URI", JOptionPane.ERROR_MESSAGE);
			// Not valid, return false
			return false;
		}
		// All valid, return true
		 */
		 
		return true;
	}

	/**
	 * Return configuration bean generated from user interface last time
	 * noteConfiguration() was called.
	 */
	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		// Should already have been made by noteConfiguration()
		return configBean;
	}

	/**
	 * Check if the user has changed the configuration from the original
	 */
	@Override
	public boolean isConfigurationChanged() {
		
		String originalSparqlLocation = configBean.getSparqlServiceLocation();
		String originalSparqlQuery = configBean.getSparqlQuery();
		
		System.out.println("  CRASH TEST  *** " + fieldSparqlLocation);
		
		// true (changed) unless all fields match the originals
		return !(originalSparqlLocation.equals(fieldSparqlLocation)
				
				&& originalSparqlQuery.equals(fieldSparqlQuery)
				);
	}

	/**
	 * Prepare a new configuration bean from the UI, to be returned with
	 * getConfiguration()
	 */
	@Override
	public void noteConfiguration() {
		configBean = new ExampleActivityConfigurationBean();
		
		// FIXME: Update bean fields from your UI elements
				
		configBean.setSparqlServiceLocation(fieldSparqlLocation.getText());
		configBean.setSparqlQuery(fieldSparqlQuery.getText());
		
		// for now, assume that all the query variables are exposed as ports
		
		String sparqlQuery = fieldSparqlQuery.getText();
		
		if (!sparqlQuery.isEmpty()) {
			
			// Get the query variables using Jena
			Query query = QueryFactory.create(sparqlQuery);
			System.out.println(" The result variables are : " + query.getResultVars());
			
			// add the QueryVariables to a list 
			List<QueryVariable> listOfVariables =  new ArrayList<QueryVariable>();
			for (String var : query.getResultVars()) {
				
				QueryVariable queryVar = new QueryVariable();
				queryVar.setVariableName(var);
				queryVar.setExposeAsPort(true);
				listOfVariables.add(queryVar);
				
			}
			
			configBean.setQueryVariables(listOfVariables);
			
		}
		
		
	}

	/**
	 * Update GUI from a changed configuration bean (perhaps by undo/redo).
	 * 
	 */
	@Override
	public void refreshConfiguration() {
		configBean = activity.getConfiguration();
		
		// FIXME: Update UI elements from your bean fields
				
		fieldSparqlQuery.setText(configBean.getSparqlQuery());
		fieldSparqlLocation.setText(configBean.getSparqlServiceLocation());
	}
}
