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
 *****************************************************************/
package net.sf.taverna.perspectives;

import java.io.InputStream;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * Perspective for enacting a workflow and keeping the results
 * 
 * @author Stuart Owens
 * 
 */
public class EnactPerspective extends AbstractPerspective {

    public ImageIcon getButtonIcon() {
        return TavernaIcons.runIcon;
    }

    public String getText() {
        return "Results";
    }

    @Override
    protected InputStream getLayoutResourceStream() {
        return EnactPerspective.class
                .getResourceAsStream("/perspective-enact.xml");
    }

	@Override
	public int positionHint() {
		return 2;
	}
    
}

