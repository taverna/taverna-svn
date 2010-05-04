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
package net.sf.taverna.t2.renderers;

import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

//import org.apache.log4j.Logger;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Viewer to display XML as a tree.
 * 
 * @author Matthew Pocock
 * @auhor Ian Dunlop
 */
public class TextXMLRenderer implements Renderer {

	private Pattern pattern;

	private float MEGABYTE = 1024 * 1024;

	//private int meg = 1048576;

	//private Logger logger = Logger.getLogger(TextXMLRenderer.class);

	public TextXMLRenderer() {
		pattern = Pattern.compile(".*text/xml.*");
	}

	public boolean isTerminal() {
		return true;
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "XML tree";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		String resolve = null;
		byte[] resolvedBytes = null;
		try {
			
			// We know the result is a string but resolving it fails if string is too big, 
			// try with byte array first?
			resolvedBytes = (byte[]) referenceService.renderIdentifier(reference,
					byte[].class, null); 

			if (resolvedBytes.length > MEGABYTE) {

				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Result is approximately "
										+ bytesToMeg(resolvedBytes.length)
										+ " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
								"Render this as xml?",
								JOptionPane.YES_NO_OPTION);

				if (response != JOptionPane.YES_OPTION) {
					return new JTextArea(
							"Rendering cancelled due to size of file. Try saving and viewing in an external application");
				}
			}

		} catch (Exception e) {
			throw new RendererException("Could not render T2 Reference " + reference, e);
		}
		try {
			// Resolve it as a string
			//resolve = (String) referenceService.renderIdentifier(reference,
			//		String.class, null);
			resolve = new String(resolvedBytes, "UTF-8");
			return new XMLTree(resolve);
		} catch (Exception ex) {
			throw new RendererException(
					"Unable to create text/xml renderer", ex);
		}
	}

	/**
	 * Work out size of file in megabytes to 1 decimal place
	 * 
	 * @param bytes
	 * @return
	 */
	private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		Math.round(f);
		return Math.round(f);
	}
}
