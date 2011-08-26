package org.embl.ebi.escience.scuflworkers.java.actions;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;

import uk.org.mygrid.taverna.processors.GridSamJSDLWorker;


public class GridSamJSDLEditAction extends AbstractProcessorAction{
	
	private static Logger logger = 	Logger.getLogger(GridSamJSDLEditAction.class);
	
	JTextField ftpServerTextField =null;
	JTextField myproxyServerTextField = null;
	JTextField myproxyServerDNTextField = null;
	JTextField proxyPortTextField = null;
	JTextField proxyLifetimeField = null;
	
	JPanel panel = new JPanel();
	

	/**
	 * Only handles {@link LocalServiceProcessor} based on {@link FlattenList}
	 */
	public boolean canHandle(Processor processor) {
		if (!(processor instanceof LocalServiceProcessor)) {
			return false;
		}
		LocalServiceProcessor proc = (LocalServiceProcessor) processor;
		return proc.getWorker() instanceof GridSamJSDLWorker;
	}

	public String getDescription() {
		return "Set constants";
	}

	public ImageIcon getIcon() {
		return null;
	}

	public Dimension getFrameSize() {
		// Big enough to show the makeHelp() at up to 20 lines or so
		return new Dimension(280, 340);
	}

	
	/**
	 * Generate the help text, with example if the current level is less than 10
	 * 
	 * @param levels The current level
	 * @return A HTML help string with example
	 */
	private String makeHelp() {
		
		String help = "<html> <small> <br>The above values can be changed <br> ftpserver value can be obtained from GridSAM administrator <br>"+
					  "default myproxyserver value refer NGS myproxyserver, that supports e-Science Certificates<br>"+
					  "proxylifetime is in seconds</small><html>";
					  
		return help;
	}


	@Override
	public JComponent getComponent(Processor processor) {
		
		if (!(processor instanceof LocalServiceProcessor)) {
			logger.error("Processor not a LocalServiceProcessor");
			return panel;
		}
		
		JLabel ftpServerLabel = new JLabel("FtpServer:");
		JLabel myproxyServerLabel = new JLabel("MyProxyServer:");
		JLabel myproxyServerDNLabel =  new JLabel("MyProxyServerDN:");
		JLabel proxyPortLabel =  new JLabel("ProxyPort:");
		JLabel proxyLifetimeLabel = new JLabel("ProxyLifetime:");
		
		LocalServiceProcessor proc = (LocalServiceProcessor) processor;
		final GridSamJSDLWorker jsdlWorker = (GridSamJSDLWorker) proc.getWorker();

		DocumentListener documentListener = new JSDLDocumentListener(jsdlWorker);
		ftpServerTextField = new JTextField("ftp://");
		ftpServerTextField.getDocument().addDocumentListener(documentListener);
		ftpServerTextField.getDocument().putProperty("name", "ftpserver");
		
		myproxyServerTextField = new JTextField(GridSamJSDLWorker.myproxyServer);
		myproxyServerTextField.getDocument().addDocumentListener(documentListener);
		myproxyServerTextField.getDocument().putProperty("name", "myproxyserver");
		
		myproxyServerDNTextField =  new JTextField(GridSamJSDLWorker.myproxyServerDN);
		myproxyServerDNTextField.getDocument().addDocumentListener(documentListener);
		myproxyServerDNTextField.getDocument().putProperty("name", "myproxyserverdn");
		
		proxyPortTextField = new JTextField(GridSamJSDLWorker.proxyPort);
		proxyPortTextField.getDocument().addDocumentListener(documentListener);
		proxyPortTextField.getDocument().putProperty("name", "proxyport");
		
		proxyLifetimeField = new JTextField(GridSamJSDLWorker.proxyLifetime);
		proxyLifetimeField.getDocument().addDocumentListener(documentListener);
		proxyLifetimeField.getDocument().putProperty("name", "proxylifetime");
		
		JLabel[] labels = {ftpServerLabel, myproxyServerLabel, myproxyServerDNLabel, proxyPortLabel, proxyLifetimeLabel};
        JTextField[] textFields = {ftpServerTextField, myproxyServerTextField, myproxyServerDNTextField, proxyPortTextField, proxyLifetimeField};
        
        this.addToPanel(labels, textFields, panel);
		
		return panel;
	}
		
	
	private void addToPanel(JLabel[] labels,JTextField[] textFields,JPanel panel){
		GridBagLayout gridBagLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gridBagLayout);
		c.anchor = GridBagConstraints.NORTH;
		int numLabels = labels.length;

		for (int i = 0; i < numLabels; i++) {
			
				c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
				c.fill = GridBagConstraints.NONE;      //reset to default
				c.weightx = 0.0;                       //reset to default
				panel.add(labels[i]);
				
				c.gridwidth = GridBagConstraints.REMAINDER;     //end row
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 1.0;
				gridBagLayout.setConstraints(textFields[i], c);
				panel.add(textFields[i]);
		}
				JLabel help = new JLabel(this.makeHelp());
				c.gridheight=GridBagConstraints.PAGE_END;
				gridBagLayout.setConstraints(help,c);
				panel.add(help);
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		GridSamJSDLEditAction.logger = logger;
	}

	
	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}
	
	private class JSDLDocumentListener implements DocumentListener{
		
		final GridSamJSDLWorker worker ;
		JSDLDocumentListener(GridSamJSDLWorker worker){		
			this.worker = worker;
		}
		
		public void changedUpdate(DocumentEvent e) {
			this.valuechange(e);
		}

		public void insertUpdate(DocumentEvent e) {
			this.valuechange(e);
		}

		public void removeUpdate(DocumentEvent e) {
			this.valuechange(e);
		}
		
		private void valuechange(DocumentEvent e){
			Document document = e.getDocument();
			if (document.getProperty("name").equals("ftpserver")){
				worker.setUserFtpServer(ftpServerTextField.getText());
			}
			if (document.getProperty("name").equals("myproxyserver")){
				worker.setUserMyproxyServer(myproxyServerTextField.getText());
			}
			if (document.getProperty("name").equals("myproxyserverdn")){
				worker.setUsermyproxyServerDN(myproxyServerDNTextField.getText());
			}
			if (document.getProperty("name").equals("proxyport")){
				worker.setUserProxyPort(proxyPortTextField.getText());
			}
			if (document.getProperty("name").equals("proxylifetime")){
				worker.setUserProxyLifetime(proxyLifetimeField.getText());
			}
		}
	}
	
}	
