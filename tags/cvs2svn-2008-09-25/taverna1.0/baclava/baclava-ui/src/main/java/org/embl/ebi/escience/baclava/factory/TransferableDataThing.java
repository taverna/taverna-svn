/*
 * Created on Mar 18, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.embl.ebi.escience.baclava.factory;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.embl.ebi.escience.baclava.DataThing;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author <a href="mailto:bleh">Kevin Glover</a>
 */
public class TransferableDataThing implements Transferable {
	private String lsid;

	private String dataThingElement;

	private static final DataFlavor[] flavours = new DataFlavor[] {
			Flavours.DATATHING_FLAVOUR, Flavours.LSID_FLAVOUR };

	public TransferableDataThing(DataThing thing) {
		System.err.println(thing);
		lsid = thing.getLSID(thing.getDataObject());
		// System.err.println(thing.getElement());
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		dataThingElement = outputter.outputString(thing.getElement());
		System.err.println(dataThingElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavours;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavour) {
		for (int index = 0; index < flavours.length; index++) {
			if (flavours[index].equals(flavour)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavour)
			throws UnsupportedFlavorException {
		if (flavour.equals(Flavours.DATATHING_FLAVOUR)) {
			return dataThingElement;
		}
		if (flavour.equals(Flavours.LSID_FLAVOUR)) {
			return lsid;
		}
		throw new UnsupportedFlavorException(flavour);
	}
}
