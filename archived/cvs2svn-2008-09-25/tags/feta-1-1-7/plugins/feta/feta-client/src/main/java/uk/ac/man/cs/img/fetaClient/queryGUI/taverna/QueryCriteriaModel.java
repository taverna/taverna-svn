/*
 * Created on Feb 25, 2004
 *
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
 */
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * @author alperp
 * 
 * 
 * 
 */
public class QueryCriteriaModel extends AbstractMonitorableModel {

	private QueryCriteriaType criteriaType;

	private Object value;

	// Can be of type FetaOntologyTermModel or String, so we do not type it here

	/**
	 * 
	 */
	public QueryCriteriaModel() {
		super();
		criteriaType = QueryCriteriaType.NAME_CRITERIA_TYPE;
		value = "";

	}

	/**
	 * @return
	 */
	public QueryCriteriaType getCriteriaType() {
		return criteriaType;
	}

	/**
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param i
	 */
	public void setCriteriaType(QueryCriteriaType i) {
		criteriaType = i;
		fireChange();
	}

	/**
	 * @param string
	 */
	public void setValue(Object obj) {
		if (obj != null) {
			// System.out.println("Debug in Query Criteria Model set value,
			// object type is -->" + obj.getClass().toString());
		} else {
			// System.out.println("Debug in Query Criteria Model set value ,
			// object is NULL");
		}
		value = obj;
		fireChange();
	}

	/**
	 * 
	 */
	public String valueDisplayString() {
		if (value == null) {
			return "";
		} else {
			return value.toString();
		}

	}

	/**
	 * 
	 */
	public String toString() {
		return criteriaType.toString() + value;

	}

}
