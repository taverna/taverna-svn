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
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Renderer for mime type text/rtf
 * 
 * @author Ian Dunlop
 */
public class TextRtfRenderer implements Renderer {

	private float MEGABYTE = 1024 * 1024;

	private int meg = 1048576;

	private Pattern pattern;

	public TextRtfRenderer() {
		pattern = Pattern.compile(".*text/rtf.*");
	}

	public boolean isTerminal() {
		return true;
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public String getType() {
		return "Text/Rtf";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		try {
			JEditorPane editorPane = null;
			
			// We know the result is a string but resolving it fails if string is too big, 
			// try with byte array first?
			byte[] resolvedBytes = (byte[]) referenceService.renderIdentifier(reference,
					byte[].class, null); 

			if (resolvedBytes.length > meg) {
				int response = JOptionPane
						.showConfirmDialog(
								null,
								"Result is approximately "
										+ bytesToMeg(resolvedBytes.length)
										+ " Mb in size, there could be issues with rendering this inside Taverna\nDo you want to continue?",
								"Render as rtf?", JOptionPane.YES_NO_OPTION);

				if (response != JOptionPane.YES_OPTION) {
					return new JTextArea(
							"Rendering cancelled due to size of file.  Try saving and viewing in an external application");
				}
			}

			try {
				// Resolve it as a string
//				String resolve = (String) referenceService.renderIdentifier(
//						reference, String.class, null);
				String resolve = new String(resolvedBytes, "UTF-8");

				editorPane = new JEditorPane("text/rtf", resolve);
				return editorPane;
			} catch (Exception e) {
				throw new RendererException(
						"Unable to create text/rtf renderer", e);
			}

		} catch (Exception e) {
			throw new RendererException("Could not render T2 Reference " + reference, e);
		}
	}
/**
 * Work out size of file in megabytes to 1 decimal place
 * @param bytes
 * @return
 */
	private int bytesToMeg(long bytes) {
		float f = bytes / MEGABYTE;
		Math.round(f);
		return Math.round(f);
	}
}
