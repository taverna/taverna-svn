package uk.ac.manchester.cs.owl.semspreadsheets.ui;

import javax.swing.JFrame;

import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.semspreadsheets.model.WorkbookManager;

public class WorkbookFrame extends JFrame {

	private OWLOntology ontology;
	private final WorkbookManager manager;
	
	public WorkbookFrame(WorkbookManager manager) {
		this.manager = manager;
		
	}

	public void setSelectedOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}

	public void removeOntology(OWLOntology ontology2) {
		// TODO Auto-generated method stub
		
	}

	public WorkbookManager getWorkbookManager() {
		return manager;
	}

}
