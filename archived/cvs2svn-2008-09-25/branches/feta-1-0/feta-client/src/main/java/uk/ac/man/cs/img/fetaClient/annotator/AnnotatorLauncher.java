/*
 *
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
 */

package uk.ac.man.cs.img.fetaClient.annotator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;

import pedro.PedroMyGridEvent;
import pedro.PedroMyGridEventListener;
import pedro.PedroService;
import pedro.util.HelpEnabledMenuItem;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.TavernaFetaGUI;
import uk.ac.man.cs.img.fetaClient.resource.FetaResources;

import uk.ac.man.cs.img.fetaClient.publisher.FetaEnginePublishManager;

/**
 * @author alperp
 *
 */
public class AnnotatorLauncher extends JPanel implements ScuflUIComponent,
		ActionListener, PropertyChangeListener , PedroMyGridEventListener {

	private ScuflModel model;

	private static AnnotatorLauncher instance;

	private PedroService pedroAnnotator;

	private DialogCloser closer;

	private InputStream fetaDescriptionStream;

	// This constructor is used by Taverna
	public AnnotatorLauncher() {
		super();
		closer = new DialogCloser();
		fetaDescriptionStream = null;

	}

	// Both are used by resultPanel
	public AnnotatorLauncher(InputStream fStream) {
		super();
		closer = new DialogCloser();
		fetaDescriptionStream = fStream;

	}

	private void launchPedro() {

		try {
			pedroAnnotator = new PedroService(new PedroProperties()
					.getPropertiesFile(), fetaDescriptionStream, this, closer,
					this);
			pedroAnnotator.start();

		} catch (pedro.system.PedroException pex) {

			System.out.println("Pedro Exception Occured: " + pex);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void attachToModel(ScuflModel theModel) {

		this.model = theModel;
		launchPedro();
		System.out.println("Debug in Attach to  model");
		((JInternalFrame) (this.getRootPane().getParent()))
				.addPropertyChangeListener(this);

	}

	public ScuflModel getModel() {

		return this.model;

	}

	/**
	 * When unbound from a model, set internal model field to null
	 */
	public void detachFromModel() {
		System.out.println("Debug in detach from  model");
		this.model = null;

		pedroAnnotator.stop();
		((JInternalFrame) (this.getRootPane().getParent())).dispose();
		AnnotatorLauncher.instance = null;

	}

	/**
	 * Method we need to implement..Should always return the same value
	 */

	public String getName() {
		return "Pedro Annotator";
	}

	/**
	 * Get the cheesy icon
	 */
	public ImageIcon getIcon() {
		try {

			return FetaResources.getPedroIcon();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("Debug in Pedro action handler");
		Object eventGenerator = e.getSource();
		if (eventGenerator instanceof HelpEnabledMenuItem) {
			HelpEnabledMenuItem menuItem = (HelpEnabledMenuItem) e.getSource();
			System.out.println(menuItem.getLabel());
			if (menuItem.getLabel().equalsIgnoreCase("exit")) {
				detachFromModel();
			}
		}
	}

	public void propertyChange(java.beans.PropertyChangeEvent evt) {

		if (evt.getPropertyName().toString().equalsIgnoreCase("selected")) {
			((JInternalFrame) (this.getRootPane().getParent()))
					.setVisible(false);
		}

	}

	public void receiveModelEvent(PedroMyGridEvent event) {
		System.out.println("Submit event received from Pedro Dialog");
		if (event.getEventType() == event.SUBMIT) {
			TavernaFetaGUI feta = TavernaFetaGUI.getInstance();
			try{
			if (feta != null) {
				//do a publish to the engine here
System.out.println("Publishing description: \n"+ event.getMessage() + "\n to Feta Registry");
				FetaEnginePublishManager publisher = new FetaEnginePublishManager(TavernaFetaGUI.getProperties().getProperty("fetaClient.fetaEngineLocation"));
				publisher.publish(event.getMessage());
				//feta.checkCacheValidity(event.getMessage());
			}
			}catch(Exception exp){
				exp.printStackTrace();

			}
		}
	}

	class DialogCloser extends WindowAdapter {

		public void windowClosing(WindowEvent event) {

			detachFromModel();

		}

	}

}
