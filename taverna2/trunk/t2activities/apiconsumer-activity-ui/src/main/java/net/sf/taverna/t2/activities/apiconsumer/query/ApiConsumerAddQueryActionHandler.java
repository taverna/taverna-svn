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
package net.sf.taverna.t2.activities.apiconsumer.query;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.lang.ui.ExtensionFileFilter;
import net.sf.taverna.t2.partition.AddQueryActionHandler;

/**
 * Action for adding a new API consumer.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ApiConsumerAddQueryActionHandler extends AddQueryActionHandler{

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		JFileChooser fc = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fc.resetChoosableFileFilters();
		fc.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
		fc.setCurrentDirectory(new File(curDir));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			File file = fc.getSelectedFile();
			ApiConsumerQuery query = new ApiConsumerQuery(file
					.getAbsolutePath());
			addQuery(query);
			JOptionPane
					.showMessageDialog(
							null,
							"Make sure you also copy the API consumer and its depending jars to " +  ApiConsumerActivity.libDir +"!",
							"Information message",
							JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	protected Icon getIcon() {
		return new ImageIcon(ApiConsumerAddQueryActionHandler.class.getResource("/apiconsumer.png"));
	}

	@Override
	protected String getText() {
		return "API Consumer...";
	}

}
