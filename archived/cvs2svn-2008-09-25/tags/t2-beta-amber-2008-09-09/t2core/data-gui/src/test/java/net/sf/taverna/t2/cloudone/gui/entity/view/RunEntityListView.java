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
package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

public class RunEntityListView {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RunEntityListView.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EntityListModel model = new EntityListModel(null);
		EntityListView view = new EntityListView(model);
		model.addObserver(new Observer<EntityListModelEvent>() {

			public void notify(Observable<EntityListModelEvent> sender,
					EntityListModelEvent message) {
				System.out.println(message.getEventType() + " "
						+ message.getModel() + " in " + sender);
			}
		});
		
		JFrame frame = new JFrame("Entity List view");
		frame.setLayout(new GridBagLayout());

		JPanel scrollPane = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		frame.add(new JScrollPane(scrollPane), c);
		
		scrollPane.setLayout(new GridBagLayout());
		GridBagConstraints paneC = new GridBagConstraints();
		paneC.gridx = 0;
		paneC.gridy = 0;
		scrollPane.add(view, paneC);
		
		paneC.gridx = 1;
		paneC.gridy = 1;
		paneC.weightx = 0.1;
		paneC.weighty = 0.1;
		paneC.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		//filler.setBackground(Color.BLUE);
		scrollPane.add(filler, paneC); 
		
		frame.setSize(new Dimension(500, 400));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
