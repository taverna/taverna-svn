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
package net.sf.taverna.t2.cloudone.refscheme.http;

import net.sf.taverna.t2.cloudone.refscheme.AbstractReferenceSchemeInfo;

/**
 * Reference scheme info for the {@link HttpReferenceScheme}. Defines the following
 * context keys through metadata file:
 * <p>
 * MachineName = name
 * <p>
 * NetworkName = subnet, mask, name
 * <p>
 * 'name' tokens are free strings, subnet and mask are specified as in IPv4 i.e.
 * in the form a.b.c.d where a,b,c,d are decimal numbers in the range 0-255
 * inclusive.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public class HttpReferenceSchemeInfo extends
		AbstractReferenceSchemeInfo<HttpReferenceScheme> {

	private static HttpReferenceSchemeInfo urlRefSchemeFactory;

	public static HttpReferenceSchemeInfo getInstance() {
		if (urlRefSchemeFactory == null) {
			urlRefSchemeFactory = new HttpReferenceSchemeInfo();
		}
		return urlRefSchemeFactory;
	}

	protected HttpReferenceSchemeInfo() {
		super();
	}

}
