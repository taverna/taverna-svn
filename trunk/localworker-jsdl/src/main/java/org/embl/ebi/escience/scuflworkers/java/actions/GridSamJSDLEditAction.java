package org.embl.ebi.escience.scuflworkers.java.actions;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;

import uk.org.mygrid.taverna.processors.GridSamJSDLWorker;


public class GridSamJSDLEditAction extends AbstractProcessorAction{

	
	
	private static Logger logger =
		Logger.getLogger(GridSamJSDLEditAction.class);

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
		
		String help = "<html>Constants:<br>" +		
			"ftpServer = ftp://rpc326.cs.man.ac.uk:19245" +
			"myproxyServer=myproxy.grid-support.ac.uk" +
			"myproxyServerDN=/C=UK/O=eScience/OU=CLRC/L=DL/CN=host/myproxy.grid-support.ac.uk/E=a.j.richards@dl.ac.uk" +
			"proxyPort=7512" + "proxyLifetime=7512";
		return help;
	}


	@Override
	public JComponent getComponent(Processor processor) {
		
		JPanel panel = new JPanel(new GridBagLayout());
		if (!(processor instanceof LocalServiceProcessor)) {
			logger.error("Processor not a LocalServiceProcessor");
			return panel;
		}
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.gridx = 0;
		c.gridy = 0;

		LocalServiceProcessor proc = (LocalServiceProcessor) processor;
	//	final FlattenList flattener = (FlattenList) proc.getWorker();

		panel.add(new JLabel("ftpServer: "), c);
		c.gridx = 1;
		JTextField ftpServer = new JTextField();
		ftpServer.setText("ftp://rpc326.cs.man.ac.uk:19245 ");
		panel.add(ftpServer, c);
		
		
		
		String help = makeHelp();
		final JLabel helpLabel = new JLabel(help);
		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 2;
		panel.add(helpLabel, c);
		
		return panel;
	}
		
}	
