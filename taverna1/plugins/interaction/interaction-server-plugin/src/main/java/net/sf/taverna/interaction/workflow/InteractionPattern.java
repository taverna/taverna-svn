/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow;

/**
 * Defines metadata for an interaction pattern
 * 
 * @author Tom Oinn
 */
public interface InteractionPattern {

	/**
	 * Return a name for this interaction pattern. If the name contains a '.'
	 * character this may be interpreted by a browser interface as representing
	 * categories, so for example the name 'edit.sequence.Artemis' could be
	 * placed in an 'edit' category with subcategory 'sequence'. Names MUST be
	 * unique within a given interaction server.
	 */
	public String getName();

	/**
	 * Return a free text description
	 */
	public String getDescription();

	/**
	 * Return an array of Taverna style syntactic type strings corresponding to
	 * the input data for this interaction pattern
	 */
	public String[] getInputTypes();

	/**
	 * Return an array of Taverna style syntactic type strings corresponding to
	 * the output data for this interaction pattern
	 */
	public String[] getOutputTypes();

	/**
	 * Return an array of names for the inputs defined in getInputTypes
	 */
	public String[] getInputNames();

	/**
	 * Return an array of output names
	 */
	public String[] getOutputNames();

}
