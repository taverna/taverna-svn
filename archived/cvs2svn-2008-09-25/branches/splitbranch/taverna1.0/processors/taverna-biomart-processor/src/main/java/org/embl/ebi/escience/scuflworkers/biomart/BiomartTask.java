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
 * Filename           $RCSfile: BiomartTask.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:40:57 $
 *               by   $Author: davidwithers $
 * Created on 17-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.biomart;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.martservice.MartQuery;
import org.biomart.martservice.config.QueryConfigUtils;
import org.biomart.martservice.query.Attribute;
import org.biomart.martservice.query.Filter;
import org.biomart.martservice.query.Query;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Task to invoke a search over a Biomart data warehouse
 * 
 * @author Tom Oinn
 * @author David Withers
 */
public class BiomartTask implements ProcessorTaskWorker {

	private BiomartProcessor processor;

	private MartQuery biomartQuery;

	/**
	 * Constructs a BiomartTask.
	 * 
	 * @param p
	 */
	public BiomartTask(Processor p) {
		this.processor = (BiomartProcessor) p;
		biomartQuery = processor.getQuery();
	}

	/*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker#execute(java.util.Map,
     *      uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask)
     */
	public Map execute(Map inputMap, IProcessorTask parentTask)
			throws TaskExecutionException {
		try {
			// Get a query including data source etc, creating
			// a copy so that any filter value settings are not
			// overwritten by input values
			biomartQuery.calculateLinks();
			Query query = new Query(biomartQuery.getQuery());

			// Configure any filters
			List filters = query.getFilters();
			for (Iterator iter = filters.iterator(); iter.hasNext();) {
				Filter filter = (Filter) iter.next();
				String name = filter.getQualifiedName();
				if (inputMap.containsKey(name + "_filter")) {
					DataThing filterThing = (DataThing) inputMap.get(name
							+ "_filter");
					Object filterValue = filterThing.getDataObject();
					if (filterValue instanceof String) {
						filter.setValue((String) filterValue);
					} else if (filterValue instanceof List) {
						List idList = (List) filterValue;
						filter.setValue(QueryConfigUtils.listToCsv(idList));
					}
				}
			}

			Map results = new HashMap();
			List[] resultList = biomartQuery.getMartService().executeQuery(
					query);
			// shouldn't need to reorder attributes for MartJ 0.5
			List attributes = biomartQuery.getAttributesInLinkOrder();
			assert resultList.length == attributes.size();
			for (int i = 0; i < resultList.length; i++) {
				Attribute attribute = (Attribute) attributes.get(i);
				results.put(attribute.getQualifiedName(), new DataThing(
						resultList[i]));
			}

			return results;
		} catch (Exception ex) {
			TaskExecutionException tee = new TaskExecutionException(
					"Failure calling biomart");
			tee.initCause(ex);
			throw tee;
		}
	}

}
