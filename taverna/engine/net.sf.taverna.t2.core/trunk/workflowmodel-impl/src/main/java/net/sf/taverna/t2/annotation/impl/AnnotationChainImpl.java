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
package net.sf.taverna.t2.annotation.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;

public class AnnotationChainImpl implements AnnotationChain{

	private List<AnnotationAssertion<?>> annotationAssertions = new ArrayList<AnnotationAssertion<?>>();
	
	public List<AnnotationAssertion<?>> getAssertions() {
		return new ArrayList<AnnotationAssertion<?>>(annotationAssertions);
	}
	
	/**
	 * Add an annotation to the chain Added because without the edits stuff how
	 * else can we do it?
	 * 
	 * @param annotationAssertion
	 */
	@SuppressWarnings("unchecked")
	public void addAnnotationAssertion(AnnotationAssertion annotationAssertion) {
		annotationAssertions.add(annotationAssertion);
	}
	
	@SuppressWarnings("unchecked")
	public void removeAnnotationAssertion(AnnotationAssertion annotationAssertion) {
		annotationAssertions.remove(annotationAssertion);
	}

}
