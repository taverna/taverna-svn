package net.sf.taverna.t2.plugin;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.sf.taverna.t2.provenance.ProvenanceConnector;

public class ProvenanceFrame extends JFrame {

	private JTextArea textArea;
	private JPanel panel;
	private ProvenanceConnector provenanceConnector;

	public ProvenanceFrame() {
		System.out.println("provenance frame created");

		panel = new JPanel();
		textArea = new JTextArea();
		panel.add(textArea);
		add(panel);
		panel.setVisible(true);
		textArea.setVisible(true);
	}

	public void setProvenanceConnector(ProvenanceConnector provenanceConnector) {
		this.provenanceConnector = provenanceConnector;
	}

	public void updateProvenance() {
		System.out.println("Provenance frame updated");
		panel.removeAll();
		try {
			String provenance = provenanceConnector.getProvenance();
			System.out.println("latest provenance is: " + provenance);
			textArea.setText(provenance);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println(e1);
		} 
	}

}
