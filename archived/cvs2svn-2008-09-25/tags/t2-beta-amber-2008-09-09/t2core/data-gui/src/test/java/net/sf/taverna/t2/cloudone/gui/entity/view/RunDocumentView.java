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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.FileRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

public class RunDocumentView {

	public static void main(String[] args) throws InterruptedException,
			IOException {
		DataDocumentModel model = new DataDocumentModel(null);
		DataDocumentEditView view = new DataDocumentEditView(model, null);

		model.addObserver(new Observer<DataDocumentModelEvent>() {
			public void notify(Observable<DataDocumentModelEvent> sender,
					DataDocumentModelEvent message) {
				System.out.println(message.getEventType() + " "
						+ message.getModel() + " in " + sender);
			}
		});

		JFrame frame = new JFrame("Document view");
		frame.add(new JPanel(), BorderLayout.LINE_START);
		frame.add(new JPanel(), BorderLayout.PAGE_START);
		frame.add(view, BorderLayout.CENTER);
		
		frame.setSize(new Dimension(500, 400));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Thread.sleep(3 * 1000);
		FileRefSchemeModel fileModel = new FileRefSchemeModel(model);
		model.addReferenceScheme(fileModel);

		Thread.sleep(2 * 1000);

		HttpRefSchemeModel httpModel = new HttpRefSchemeModel(model);
		model.addReferenceScheme(httpModel);
		Thread.sleep(2 * 1000);
		fileModel.setFile(File.createTempFile("some", "test"));
		Thread.sleep(2 * 1000);
		httpModel.setURL("http://www.google.co.uk/");
		
		Thread.sleep(2 * 60 * 1000);
		System.exit(0);
	}
}
