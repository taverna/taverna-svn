/*
 * Created on June 17, 2004, 1:49 PM
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

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.ProcessorSpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import uk.ac.man.cs.img.fetaClient.importer.BioMobyToSkeletonConverter;
import uk.ac.man.cs.img.fetaClient.importer.DummySkeletonGenerator;
import uk.ac.man.cs.img.fetaClient.importer.SCUFLToSkeletonConverter;
import uk.ac.man.cs.img.fetaClient.importer.SoaplabToSkeletonConverter;
import uk.ac.man.cs.img.fetaClient.importer.WSDLToSkeletonConverter;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.BIOMOBYFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.SOAPLABFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.WORKFLOWFragment;
import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.dnd.WSDLFragment;
import uk.ac.man.cs.img.fetaClient.resource.FetaResources;
import uk.ac.man.cs.img.fetaClient.util.GUIUtil;


/**
 * @author alperp
 * @author Stuart Owen
 * 
 * 
 * 
 */
@SuppressWarnings("serial")
public class TavernaFetaGUI extends JTabbedPane implements WorkflowModelViewSPI,
DropTargetListener {
	
	private static Logger logger = Logger.getLogger(TavernaFetaGUI.class);
	
	private static TavernaFetaGUI instance;
	
	private ScuflModel model;
	
	private ResultPanel resultPanel;
	
	private QueryPanel queryPanel;
	
	private AdminPanel adminPanel;
	
	private QueryHelper helper;	
	
	private TavernaFetaGUI() {
		super();
		
		this.setMaximumSize(new Dimension(500, 700));
		instance = this;
		
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		try {
			getProperties();
		} catch (java.io.IOException e) {
			logger.error("Problem reading Feta Properties File : ",e);
		}
		initialise();		
	}
		
	public void onDisplay() {
		// do nothing		
	}

	public void onDispose() {
		// do nothing	
	}

	public static TavernaFetaGUI getInstance() {
		if (instance == null) {
			instance = new TavernaFetaGUI();
		}
		return instance;
	}
	
	/**
	 * Listen for model bind requests to set the internal ScuflModel field
	 */
	public void attachToModel(ScuflModel theModel) {		
		logger.debug("Attach to model called, model:"+theModel);				
		this.model = theModel;							
	}

	/**
	 * Initialises the UI
	 *
	 */
	private void initialise() {
		boolean annotator = false;
		String loc = "";
		String loc2 = "";
		try {
			
			loc = getProperties().getProperty("fetaClient.fetaEngineLocation");
			URL fetaLoc = new URL(loc);
			fetaLoc.openConnection();
			
			loc2 = getProperties().getProperty("fetaClient.fetaAdminLocation");
			URL fetaADMLoc = new URL(loc2);
			
			helper = new QueryHelper(fetaLoc, fetaADMLoc);
			queryPanel = new QueryPanel(helper);
			
			resultPanel = new ResultPanel(helper);
			
			annotator = FetaClientProperties.isAnnotator();
			if (annotator) {
				adminPanel = new AdminPanel(helper);
			}
			
			this.add("Search Services", queryPanel);
			// setBackgroundAt(0, ShadedLabel.TAVERNA_GREEN);
			this.add("Result", resultPanel);
			// setBackgroundAt(1, ShadedLabel.TAVERNA_GREEN);
			
			/* if (annotator) {
			 this.add("Administer", adminPanel);
			 }
			 */
			
		} catch (java.net.ConnectException eConn) {
			eConn.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Unable to connect to the Feta Engine located at: \n" + loc
					+ "Error message is :" + eConn.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			detachFromModel();
			((JInternalFrame) (this.getRootPane().getParent())).dispose();
			return;
			
		} catch (java.io.IOException eIO) {
			eIO.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Unable to read FetaClient properties File. Error message is :"
					+ eIO.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			detachFromModel();
			((JInternalFrame) (this.getRootPane().getParent())).dispose();
			return;
			
		} catch (Exception exp) {
			exp.printStackTrace();
			detachFromModel();
			((JInternalFrame) (this.getRootPane().getParent())).dispose();
			return;
			
		}
	}
	
	/**
	 * When unbound from a model, set internal model field to null
	 */
	public void detachFromModel() {		
		this.model = null;
		//TavernaFetaGUI.instance = null;
		logger.debug("Detach from model called");	
	}
	
	/**
	 * Method we need to implement..Should always return the same value
	 */
	
	public String getName() {
		return "TavernaFetaGUI";
	}
	
	public ScuflModel getScuflModel() {
		return model;
	}
	
	/**
	 * Get the cheesy icon
	 */
	public ImageIcon getIcon() {
		try {
			return FetaResources.getFetaIcon();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public QueryHelper getHelper() {
		return this.helper;
	}
	
	public void checkCacheValidity(String publishLocation) {
		
		helper.isInEngineScope(publishLocation);
		
	}
	
	public static void main(String[] args) {
		try {
			TavernaFetaGUI gui = TavernaFetaGUI.getInstance();
			JFrame frame = new JFrame();
			frame.setTitle("Smart Service Search");
			frame.getContentPane().add("Center", gui);
			frame.pack();
			frame.setSize(800, 600);
			frame.show();
			gui.attachToModel(null);
		} catch (Throwable t) {
			System.out.println("uncaught exception: " + t);
			t.printStackTrace();
		}		
	}
	
	public void switchToResultPanel() {
		this.setSelectedComponent(resultPanel);
		this.repaint();
		
	}
	
	public void clearFormPanel() {
		this.resultPanel.clearForm();
	}
	
	public static Properties getProperties() throws IOException {
		return FetaClientProperties.getProperties();
	}
	
	public void drop(DropTargetDropEvent e) {
		// I've commented out the whole import logic
		// as this functionality will now be delivered by
		// the WEB based annotation envirnment
		
		try {
			boolean annotator = FetaClientProperties.isAnnotator();
			if (annotator){
				XMLOutputter xo = new XMLOutputter();
				
				org.jdom.Element droppedProcessorElement = null;
				setCursorBusy();
				DataFlavor f = SpecFragmentTransferable.factorySpecFragmentFlavor;
				DataFlavor p = SpecFragmentTransferable.processorSpecFragmentFlavor;
				
				boolean attemptToRecognize = false;
				Transferable t = e.getTransferable();
				
				if (e.isDataFlavorSupported(f)) {
					FactorySpecFragment fsf = (FactorySpecFragment)t.getTransferData(f);
					System.out.println("Found factory spec fragment");
					
					System.out.println(xo.outputString(fsf.getElement()));
					String validName = model.getValidProcessorName(fsf.getFactoryNodeName());
					attemptToRecognize = true;
					
					droppedProcessorElement =  fsf.getElement();
					
				} else if (e.isDataFlavorSupported(p)) {
					
					attemptToRecognize = true;
					ProcessorSpecFragment psf = (ProcessorSpecFragment)t.getTransferData(p);
					// Remove the various fault tolerance etc attributes
					Element processorElement = psf.getElement();
					List attributes = processorElement.getAttributes();
					for (Iterator i = attributes.iterator(); i.hasNext();) {
						Attribute att = (Attribute)i.next();
						processorElement.removeAttribute(att);
					}
					
					System.out.println("Found processor spec fragment");
					
					System.out.println(xo.outputString(processorElement));
					
					droppedProcessorElement  = 	processorElement;
				}//else-if
				
				if (attemptToRecognize==true){
					List fetaDescURIs = helper.reverseLookup(droppedProcessorElement);
					
					if (fetaDescURIs != null){
						
						int returnVal = JOptionPane.showConfirmDialog(this,
								"There already exists a Feta description for the service you want to annotate.\n " +
								"Do you want the annotator to be launched with the existing annotation ",
								"Annotation Exists ",
								JOptionPane.INFORMATION_MESSAGE);
						
						if (returnVal == JOptionPane.YES_OPTION) {
							
							//we get the first one for now
							GUIUtil.launchAnnotator((String)fetaDescURIs.get(0));
							
						} else if (returnVal == JOptionPane.NO_OPTION) {
							//setCursor (new Cursor (Cursor.WAIT_CURSOR));
							GUIUtil.launchAnnotator((org.w3c.dom.Document)invokeImporter(droppedProcessorElement));
							
						} else {
							//User pressed the cancel button
							e.rejectDrop();
						}
						
					}else {
						GUIUtil.launchAnnotator((org.w3c.dom.Document)invokeImporter(droppedProcessorElement));
						
					}//else
				}//if
				
				e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				
			}
			
			
		}//try
		catch (Exception ex) {
			e.rejectDrop();
			
		}
		unsetCursorBusy();
		
		
	}
	
	public void dragEnter(java.awt.dnd.DropTargetDragEvent dtde) {
	}
	
	public void dragExit(DropTargetEvent dte) {
	}
	
	public void dragOver(java.awt.dnd.DropTargetDragEvent dtde) {
	}
	
	public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dtde) {
	}
	
	private org.w3c.dom.Document invokeImporter(org.jdom.Element specElement) {
		
		org.w3c.dom.Document doc = null;
		Object[] results;
		XMLOutputter xo = new XMLOutputter();
		
		try {
			if (specElement.getName().equalsIgnoreCase("soaplabwsdl")) {
				
				SoaplabToSkeletonConverter soapImporter = new SoaplabToSkeletonConverter(
						new SOAPLABFragment(specElement));
				results = soapImporter.convert().values().toArray();
				doc = (org.w3c.dom.Document) (results.length > 0 ? results[0]
				                                                           : null);
			} else if (specElement.getName().equalsIgnoreCase("biomobywsdl")) {
				BioMobyToSkeletonConverter mobyImporter = new BioMobyToSkeletonConverter(
						new BIOMOBYFragment(specElement));
				results = mobyImporter.convert().values().toArray();
				doc = (org.w3c.dom.Document) (results.length > 0 ? results[0]
				                                                           : null);
			} else if (specElement.getName().equalsIgnoreCase("arbitrarywsdl")) {
				WSDLToSkeletonConverter wsdlImporter = new WSDLToSkeletonConverter(
						new WSDLFragment(specElement));
				results = wsdlImporter.convert().values().toArray();
				doc = (org.w3c.dom.Document) (results.length > 0 ? results[0]
				                                                           : null);
			} else if (specElement.getName().equalsIgnoreCase("workflow")) {
				SCUFLToSkeletonConverter scuflImporter = new SCUFLToSkeletonConverter(
						new WORKFLOWFragment(specElement), model);
				results = scuflImporter.convert().values().toArray();
				doc = (org.w3c.dom.Document) (results.length > 0 ? results[0]
				                                                           : null);
			} else {
				// for every other processor type for which there are no
				// importers
				// create a dummy skeleton.
				
				DummySkeletonGenerator dummyGen = new DummySkeletonGenerator(xo
						.outputString(specElement));
				// xo.escapeAttributeEntities(xo.outputString(specElement))
				doc = dummyGen.generate();
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			doc = null;
		}
		
		return doc;
	}
	
	private void setCursorBusy() {
		((JInternalFrame) (this.getRootPane().getParent()))
		.setCursor(FetaResources.getBusyCursor());
	}
	
	private void unsetCursorBusy() {
		((JInternalFrame) (this.getRootPane().getParent())).setCursor(Cursor
				.getDefaultCursor());
	}
	
}
