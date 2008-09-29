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
 * CloudOne is the data layer designed for use by Taverna 2. Although this
 * is its primary purpose it is intended to be a generic and extensible
 * framework defining a lightweight peer to peer datagrid with explicit
 * handling and migration of data by reference.
 * <p>
 * The main entrance point is the
 * {@link net.sf.taverna.t2.cloudone.datamanager.DataManager}, which can store and
 * retrieve {@link net.sf.taverna.t2.cloudone.entity.Entity}s identified
 * using {@link net.sf.taverna.t2.cloudone.identifier.EntityIdentifier}. There
 * are several implementations of DataManager, mainly
 * {@link net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager}
 * and {@link net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager}.
 * <p>
 * The DataManager also has a higher level interface
 * {@link net.sf.taverna.t2.cloudone.datamanager.DataFacade} that
 * is useful for converting back and forth between Java structures and
 * stored {@link net.sf.taverna.t2.cloudone.entity.Entity}s and blobs in
 * a {@link net.sf.taverna.t2.cloudone.datamanager.BlobStore}.
 */
package net.sf.taverna.t2.cloudone;

