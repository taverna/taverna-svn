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
 * Filename           $RCSfile: BytesSelection.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-04 16:44:38 $
 *               by   $Author: dturi $
 * Created on 4 Dec 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.shared;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * {@link Transferable} bytes used for clipboard.
 * 
 * @author dturi
 * @version $Id: BytesSelection.java,v 1.1 2006-12-04 16:44:38 dturi Exp $
 */
public class BytesSelection implements Transferable, ClipboardOwner {

    private byte[] bytes;

    private static final int BYTES = 0;

    // private static final int IMAGE = 1;

    public static final DataFlavor bytesFlavor = new DataFlavor(
            "application/octet-stream; class=java.io.InputStream", "Bytes");

    private static final DataFlavor[] flavors = { bytesFlavor
    // , DataFlavor.imageFlavor
    };

    public BytesSelection(byte[] bytes) {
        this.bytes = bytes;
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (flavor.equals(flavors[BYTES])) {
            return bytes;
            // } else if (flavor.equals(flavors[IMAGE])) {
            // return bytes;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public DataFlavor[] getTransferDataFlavors() {
        // returning flavors itself would allow client code to modify
        // our internal behavior
        return (DataFlavor[]) flavors.clone();
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // do nothing
    }

}
