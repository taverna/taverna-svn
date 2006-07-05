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
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:57 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.biomart.martservice.MartDataset;
import org.biomart.martservice.MartQuery;
import org.biomart.martservice.MartService;
import org.biomart.martservice.MartServiceException;
import org.biomart.martservice.config.QueryConfigController;
import org.biomart.martservice.config.ui.MartServiceQueryConfigUIFactory;
import org.biomart.martservice.config.ui.QueryConfigUIFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * A panel containing a the Biomart configuration UI.
 * 
 * @author David Withers
 */
public class BiomartConfigPanel extends JPanel implements ScuflUIComponent {

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
		QueryConfigController controller = new QueryConfigController(martQuery);

		setLayout(new BorderLayout());

		QueryConfigUIFactory queryConfigUIFactory;
		queryConfigUIFactory = new MartServiceQueryConfigUIFactory(martService,
				controller, dataset);
		add(queryConfigUIFactory.getDatasetConfigUI(), BorderLayout.NORTH);
	}

	/**
     * For testing the martservice without the workbench
     * 
     * @param args
     */
	public static void main(String[] args) {
		try {
			MartService martService = new MartService(
					"http://www.biomart.org/biomart/martservice");
			MartDataset dataset = null;
			MartDataset[] datasets = martService.getDatasets();
			for (int i = 0; i < datasets.length; i++) {
				if (datasets[i].getName().equals("hsapiens_gene_ensembl")) {
					dataset = datasets[i];
					break;
				}
			}

			MartQuery query = new MartQuery(martService, dataset);
			QueryConfigController controller = new QueryConfigController(query);

			QueryConfigUIFactory queryConfigUIFactory = new MartServiceQueryConfigUIFactory(
					martService, controller, dataset);

			JPanel panel = new JPanel(new BorderLayout());
			panel.add(queryConfigUIFactory.getDatasetConfigUI(),
					BorderLayout.NORTH);

			JFrame frame = new JFrame("test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JScrollPane scrollPane = new JScrollPane(panel);
			scrollPane.getVerticalScrollBar().setUnitIncrement(10);
			frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
			frame.pack();
			frame.setSize(new Dimension(600, 800));
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
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
     */
	public void attachToModel(ScuflModel model) {
		// nothing to do
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
     */
	public void detachFromModel() {
		// nothing to do
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
			return biomartProcessor.getQuery().getMartDataset().getDisplayName();
		} catch (NullPointerException ex) {
			return "Unconfigured Biomart processor";
		}
	}

}
