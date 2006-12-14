/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: BiomartConfigPanel.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-14 14:10:36 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.utils.MyGridConfiguration;

import org.biomart.martservice.MartDataset;
import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.config.QueryConfigController;
import org.biomart.martservice.config.ui.MartServiceQueryConfigUIFactory;
import org.biomart.martservice.config.ui.QueryConfigUIFactory;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * A panel containing a the Biomart configuration UI.
 * 
 * @author David Withers
 */
public class BiomartConfigPanel extends JPanel implements UIComponentSPI {

	private BiomartProcessor biomartProcessor;

	/**
	 * Creates a component for configuring a Biomart query.
	 * 
	 * @param biomartProcessor
	 * @throws MartServiceException
	 */
	public BiomartConfigPanel(BiomartProcessor biomartProcessor)
			throws MartServiceException {
		this.biomartProcessor = biomartProcessor;
		MartQuery martQuery = biomartProcessor.getQuery();
		MartDataset dataset = martQuery.getMartDataset();
		MartService martService = martQuery.getMartService();
		martService.setRequestId("taverna");
		martService.setCacheDirectory(new File(MyGridConfiguration
				.getUserDir("taverna-biomart-processor"), "cache"));
		QueryConfigController controller = new QueryConfigController(martQuery);

		QueryConfigUIFactory queryConfigUIFactory;
		queryConfigUIFactory = new MartServiceQueryConfigUIFactory(martService,
				controller, dataset);
		setLayout(new BorderLayout());
		add(queryConfigUIFactory.getDatasetConfigUI(), BorderLayout.CENTER);
	}

	/**
	 * For testing the martservice without the workbench
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MartService martService = MartService
					.getMartService("http://www.biomart.org/biomart/martservice");
			martService.setCacheDirectory(new File(MyGridConfiguration
					.getUserDir("taverna-biomart-processor"), "cache"));
			MartDataset dataset = null;
			MartDataset[] datasets = martService.getDatasets();
			for (int i = 0; i < datasets.length; i++) {
				if (datasets[i].getName().equals("hsapiens_gene_ensembl")) {
					dataset = datasets[i];
					break;
				}
			}

			MartQuery query = new MartQuery(martService, dataset, "taverna");
			QueryConfigController controller = new QueryConfigController(query);

			QueryConfigUIFactory queryConfigUIFactory = new MartServiceQueryConfigUIFactory(
					martService, controller, dataset);

			JFrame frame = new JFrame("test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(queryConfigUIFactory.getDatasetConfigUI());
			frame.pack();
			frame.setSize(new Dimension(800, 600));
			frame.setVisible(true);
		} catch (MartServiceException e) {
			JOptionPane.showMessageDialog(null,
					"Unable to create biomart query editor\n" + e.getMessage(),
					"Problem creating biomart query editor",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
	 */
	public ImageIcon getIcon() {
		return ProcessorHelper.getPreferredIcon(biomartProcessor);
	}

	public String getName() {
		try {
			return biomartProcessor.getQuery().getMartDataset()
					.getDisplayName();
		} catch (NullPointerException ex) {
			return "Unconfigured Biomart processor";
		}
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

}
