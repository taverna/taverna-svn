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
/**
 * An {@link net.sf.taverna.t2.cloudone.identifier.EntityIdentifier} is
 * identifying an {@link net.sf.taverna.t2.cloudone.entity.Entity}. There is
 * one identifier class for each Entity class, such as
 * {@link net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier},
 * {@link net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier} and
 * {@link net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier},
 * with the exception of {@link net.sf.taverna.t2.cloudone.entity.Literal},
 * which is its own identifier.
 * <p>
 * All identifiers are serialisable as URI strings. The
 * {@link net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers} utility
 * class can help reconstruct the right instance from an URI.
 *
 */
package net.sf.taverna.t2.cloudone.identifier;

